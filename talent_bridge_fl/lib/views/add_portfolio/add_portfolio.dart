import 'package:cloud_firestore/cloud_firestore.dart';
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
  Widget build(BuildContext context) {
    var titleField = TextField(
      controller: _titleController,
      maxLength: 50,
      decoration: const InputDecoration(label: Text("Title")),
    );
    var urlField = TextField(
      controller: _urlController,
      decoration: const InputDecoration(label: Text("Url of portfolio")),
    );

    var optionalImageButton = OutlinedButton.icon(
      onPressed: _getAnImage,
      label: Text("Add image (optional)"),
      icon: Icon(Icons.image_search),
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
                  FilledButton.icon(
                    onPressed: () {},
                    label: const Text("Cancel"),
                    style: FilledButton.styleFrom(backgroundColor: Colors.red),
                  ),
                  SizedBox(
                    width: 16,
                  ),
                  FilledButton.icon(
                    onPressed: () => _submitData(context),
                    label: Text('Save'),
                    style: FilledButton.styleFrom(
                      backgroundColor: darkBlue,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
