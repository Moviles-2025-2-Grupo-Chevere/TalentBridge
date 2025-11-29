import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
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

  final picker = ImagePicker();

  final fb = FirebaseService();

  final store = FirebaseFirestore.instance;

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
    });
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
              optionalImageButton,
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
