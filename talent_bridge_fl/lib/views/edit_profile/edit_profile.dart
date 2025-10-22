import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/data/major_service.dart';
import 'package:talent_bridge_fl/services/skills_service.dart';
import 'package:talent_bridge_fl/views/select_skills/select_skills.dart';

const darkBlue = Color(0xFF3E6990);

class EditProfile extends StatefulWidget {
  const EditProfile({super.key});

  @override
  State<EditProfile> createState() => _EditProfileState();
}

class _EditProfileState extends State<EditProfile> {
  final _formKey = GlobalKey<FormState>();

  _submitData() {}

  void _openSkillsView(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => Scaffold(
          appBar: AppBar(
            title: const Text('Select Skills'),
          ),
          body: SelectSkills(
            skills: SkillsService.getSkills(),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: double.infinity,
      child: Form(
        key: _formKey,
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                Text(
                  'Edit profile',
                  style: Theme.of(context).textTheme.headlineMedium,
                ),
                TextFormField(
                  decoration: const InputDecoration(
                    label: Text('Display Name'),
                  ),
                ),
                SizedBox(height: 16),
                TextFormField(
                  decoration: const InputDecoration(
                    label: Text('Headline'),
                  ),
                ),
                SizedBox(height: 16),
                TextFormField(
                  decoration: const InputDecoration(
                    label: Text('Linkedin'),
                  ),
                ),
                SizedBox(height: 16),
                TextFormField(
                  decoration: const InputDecoration(
                    label: Text('Mobile Number'),
                  ),
                  keyboardType: TextInputType.phone,
                ),
                SizedBox(height: 16),
                TextFormField(
                  decoration: const InputDecoration(
                    label: Text('Description'),
                  ),
                ),
                SizedBox(height: 16),
                DropdownButtonFormField(
                  items: MajorService.getMajors()
                      .map(
                        (e) => DropdownMenuItem(
                          value: e,
                          child: Text(e),
                        ),
                      )
                      .toList(),
                  onChanged: (value) {},
                  decoration: const InputDecoration(
                    label: Text('Major'),
                  ),
                ),
                SizedBox(height: 16),
                Row(
                  children: [
                    Text("Skills and Topics"),
                    SizedBox(
                      width: 12,
                    ),
                    IconButton(
                      onPressed: () => _openSkillsView(context),
                      icon: Icon(Icons.add),
                    ),
                  ],
                ),
                SizedBox(height: 16),
                Row(
                  children: [
                    FilledButton.icon(
                      onPressed: () {
                        Navigator.pop(context);
                      },
                      label: Text('Cancel'),
                      style: FilledButton.styleFrom(
                        backgroundColor: Colors.red,
                      ),
                    ),
                    SizedBox(
                      width: 16,
                    ),
                    FilledButton.icon(
                      onPressed: _submitData,
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
      ),
    );
  }
}
