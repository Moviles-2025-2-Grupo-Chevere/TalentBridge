import 'package:flutter/material.dart';

const darkBlue = Color(0xFF3E6990);

class AddPortfolio extends StatelessWidget {
  AddPortfolio({super.key});

  final _titleController = TextEditingController();
  final _urlController = TextEditingController();

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
      onPressed: () {},
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
                    onPressed: () {},
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
