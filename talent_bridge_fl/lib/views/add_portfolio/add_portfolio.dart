import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:image_picker/image_picker.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

const darkBlue = Color(0xFF3E6990);

class AddPortfolio extends StatefulWidget {
  const AddPortfolio({super.key});

  @override
  State<AddPortfolio> createState() => _AddPortfolioState();
}

class _AddPortfolioState extends State<AddPortfolio> {
  final _connectivity = Connectivity();
  StreamSubscription<List<ConnectivityResult>>? _connSuscription;
  bool _connected = false;

  final _titleController = TextEditingController();

  final _urlController = TextEditingController();

  String portfolioType = "";

  final picker = ImagePicker();

  final fb = FirebaseService();

  final store = FirebaseFirestore.instance;

  final hiveBoxName = "portfolio_types";
  final hiveKey = "labels";

  bool isValidUrl(String value) {
    final uri = Uri.tryParse(value);
    return uri != null &&
        (uri.isScheme("http") || uri.isScheme("https")) &&
        uri.host.isNotEmpty;
  }

  Future<void> _submitData(BuildContext context) async {
    final title = _titleController.text;
    final url = _urlController.text;
    final uid = fb.currentUid();

    if (title.isEmpty || url.isEmpty || uid == null) {
      showDialog(
        context: context,
        builder: (ctx) => AlertDialog(
          title: const Text('Invalid input'),
          content: const Text(
            'Please make sure a title and url are entered',
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(ctx);
              },
              child: const Text('Okay'),
            ),
          ],
        ),
      );
      return;
    } else if (!isValidUrl(url)) {
      showDialog(
        context: context,
        builder: (ctx) => AlertDialog(
          title: const Text('Invalid url'),
          content: const Text(
            'An invalid url was entered',
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(ctx);
              },
              child: const Text('Okay'),
            ),
          ],
        ),
      );
      return;
    }

    final result = await store.collection("flPortfolios").add({
      "title": title,
      "url": url,
      "uid": uid,
      "type": portfolioType,
    });
    if (context.mounted) {
      Navigator.pop(context);
    }
  }

  Future<void> _getAnImage() async {
    final XFile? image = await picker.pickImage(source: ImageSource.gallery);

    if (image != null) {
      print('Picked file path: ${image.path}');
    }
  }

  @override
  void initState() {
    super.initState();
    _connectivity.checkConnectivity().then(
      (value) {
        setState(() {
          _connected = value[0] != ConnectivityResult.none;
        });
      },
    );
    final suscription = _connectivity.onConnectivityChanged.listen(
      (status) {
        setState(() {
          _connected = status[0] != ConnectivityResult.none;
        });
      },
    );
    _connSuscription = suscription;
  }

  Future<List<String>> getPortfolioLabels() async {
    final box = await Hive.openBox(hiveBoxName);
    final labels = box.get(hiveKey);
    if (labels is List) {
      print("Got data from hive");
      print(labels);
      // await box.close();
      return List<String>.from(labels);
    } else {
      print("no data");
      final docs = (await store.collection("portfolioTypes").get()).docs;
      final labels = docs.map((e) => e.data()["label"]).toList();
      print("obtained data");
      print(labels);
      if (labels.isEmpty) return [];
      await box.put(hiveKey, labels);
      // await box.close();
      return List<String>.from(labels);
    }
  }

  @override
  void dispose() {
    super.dispose();
    _connSuscription?.cancel();
  }

  @override
  Widget build(BuildContext context) {
    var titleField = TextField(
      controller: _titleController,
      enabled: _connected,
      maxLength: 50,
      decoration: const InputDecoration(label: Text("Title")),
    );
    var urlField = TextField(
      controller: _urlController,
      enabled: _connected,
      decoration: const InputDecoration(label: Text("Url of portfolio")),
    );

    var portfolioTypeMenu = FutureBuilder(
      future: getPortfolioLabels(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const CircularProgressIndicator();
        }
        if (snapshot.hasError) {
          return Text('Error: ${snapshot.error}');
        }
        final labels = snapshot.data ?? [];
        if (labels.isEmpty) {
          return const Text('No portfolio types available');
        }
        return DropdownButtonFormField(
          isExpanded: true,
          value: "",
          decoration: const InputDecoration(label: Text("Portfolio Type")),
          items: [
            DropdownMenuItem(
              value: '',
              child: Text('N/A'),
            ),
            ...labels.map(
              (label) => DropdownMenuItem(
                value: label,
                child: Text(label),
              ),
            ),
          ],
          onChanged: (value) {
            portfolioType = value ?? '';
          },
        );
      },
    );

    var optionalImageButton = OutlinedButton.icon(
      onPressed: _connected ? _getAnImage : null,
      label: Text("Add image (optional)"),
      icon: Icon(Icons.image_search),
    );

    var noConnDisclaimer = Card.outlined(
      child: Padding(
        padding: EdgeInsetsGeometry.all(8),
        child: Row(
          children: [
            Icon(Icons.warning),
            SizedBox(
              width: 8,
            ),
            Expanded(
              child: const Text(
                "Portfolios cannot be uploaded without a reliable connection",
              ),
            ),
          ],
        ),
      ),
    );
    var cancelButton = FilledButton.icon(
      onPressed: _connected
          ? () {
              Navigator.pop(context);
            }
          : null,
      label: const Text("Cancel"),
      style: FilledButton.styleFrom(backgroundColor: Colors.red),
    );
    var submitButton = FilledButton.icon(
      onPressed: _connected ? () => _submitData(context) : null,
      label: Text('Save'),
      style: FilledButton.styleFrom(
        backgroundColor: darkBlue,
      ),
    );
    return SizedBox(
      height: double.infinity,
      child: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              Text(
                "Add portfolio entry",
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              if (!_connected) noConnDisclaimer,
              SizedBox(
                height: 16,
              ),
              titleField,
              SizedBox(
                height: 16,
              ),
              urlField,
              SizedBox(
                height: 16,
              ),
              portfolioTypeMenu,
              // optionalImageButton,
              SizedBox(
                height: 16,
              ),
              Row(
                children: [
                  cancelButton,
                  SizedBox(
                    width: 16,
                  ),
                  submitButton,
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
