import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/data/major_service.dart';
import 'package:talent_bridge_fl/domain/update_user_dto.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/services/skills_service.dart';
import 'package:talent_bridge_fl/views/select_skills/select_skills.dart';

const darkBlue = Color(0xFF3E6990);

class EditProfile extends StatefulWidget {
  const EditProfile({super.key});

  @override
  State<EditProfile> createState() => _EditProfileState();
}

class _EditProfileState extends State<EditProfile> {
  final _fb = FirebaseService();
  final _formKey = GlobalKey<FormState>();
  final _skills = SkillsService.getSkills();
  var _formIsValid = false;
  // Form values
  String displayName = '';
  String headline = '';
  String linkedinUrl = '';
  String mobileNumber = '';
  String description = '';
  String major = '';
  final _selectedSkills = HashSet<String>();

  _submitData() async {
    if (_formKey.currentState!.validate()) {
      _formKey.currentState!.save();
      await _fb.updateUserProfile(
        UpdateUserDto(
          displayName: displayName,
          headline: headline,
          linkedin: linkedinUrl,
          mobileNumber: mobileNumber,
          skillsOrTopics: _selectedSkills.toList(),
          description: description,
          major: major,
        ),
      );
      debugPrint("Updated user document");
      if (mounted) {
        Navigator.of(context).pop();
      }
    }
  }

  void _removeSelectedSkill(String skill) {
    setState(() {
      _selectedSkills.remove(skill);
    });
  }

  void _openSkillsView(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) {
          return PopScope(
            onPopInvokedWithResult: (didPop, result) {
              debugPrint('Popped from skills view!');
              setState(() {});
            },
            child: Scaffold(
              appBar: AppBar(
                title: const Text('Select Skills'),
              ),
              body: SelectSkills(
                skills: _skills,
                selectedSkills: _selectedSkills,
              ),
            ),
          );
        },
      ),
    );
  }

  // Validators
  // Display name
  String? validateDisplayName(String? value) {
    if (value == null) {
      return 'Not Initialized';
    }
    if (value.length > 30) {
      return 'Too long';
    }
    if (value.isNotEmpty && value.length < 5) {
      return 'Too short';
    }
    return null;
  }

  // Headline
  String? validateHeadline(String? value) {
    if (value == null) {
      return 'Not Initialized';
    }
    if (value.length > 80) {
      return 'Too long';
    }

    return null;
  }

  // Linkedin
  String? validateLinkedin(String? value) {
    if (value == null) {
      return 'Not Initialized';
    }
    final pattern =
        r'^(https?:\/\/)?(www\.)?linkedin\.com\/(in|pub)\/[A-z0-9_-]+\/?$';
    final regex = RegExp(pattern);

    if (value.isNotEmpty && !regex.hasMatch(value.trim())) {
      return 'Enter a valid LinkedIn profile URL';
    }
    return null;
  }

  // Mobile number
  String? validateMobileNumber(String? value) {
    if (value == null) {
      return 'Not Initialized';
    }
    if (value.isNotEmpty && value.length != 10) {
      return 'Invalid mobile length';
    }
    final regex = RegExp(r'^[0-9]{10}$');
    if (value.isNotEmpty && !regex.hasMatch(value.trim())) {
      return 'Enter a valid 10-digit phone number';
    }
    return null;
  }

  // Description
  String? validateDescription(String? value) {
    if (value == null) {
      return 'Not Initialized';
    }
    if (value.length > 1000) {
      return 'Description too long';
    }
    return null;
  }

  void onFormChange() {
    final isValid = _formKey.currentState!.validate();
    setState(() {
      _formIsValid = isValid;
    });
  }

  @override
  void initState() {
    super.initState();

    // 🪄 Schedule validation after first frame
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final currentState = _formKey.currentState;
      if (currentState != null) {
        setState(() {
          _formIsValid = currentState.validate();
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    // Form fields
    var displayNameField = TextFormField(
      autovalidateMode: AutovalidateMode.onUserInteraction,
      maxLength: 30,
      decoration: const InputDecoration(
        label: Text('Display Name'),
      ),
      validator: validateDisplayName,
      onSaved: (newValue) => displayName = newValue ?? '',
    );

    var headlineField = TextFormField(
      autovalidateMode: AutovalidateMode.onUserInteraction,
      maxLength: 80,
      decoration: const InputDecoration(
        label: Text('Headline'),
      ),
      validator: validateHeadline,
      onSaved: (newValue) => headline = newValue ?? '',
    );
    var linkedinField = TextFormField(
      autovalidateMode: AutovalidateMode.onUserInteraction,
      maxLength: 150,
      decoration: const InputDecoration(
        label: Text('Linkedin'),
      ),
      validator: validateLinkedin,
      onSaved: (newValue) => linkedinUrl = newValue ?? '',
    );
    var mobileNumberField = TextFormField(
      autovalidateMode: AutovalidateMode.onUserInteraction,
      maxLength: 10,
      decoration: const InputDecoration(
        label: Text('Mobile Number'),
      ),
      keyboardType: TextInputType.phone,
      validator: validateMobileNumber,
      onSaved: (newValue) => mobileNumber = newValue ?? '',
    );
    var descriptionField = TextFormField(
      autovalidateMode: AutovalidateMode.onUserInteraction,
      maxLength: 1000,
      decoration: const InputDecoration(
        label: Text('Description'),
        border: OutlineInputBorder(),
      ),
      keyboardType: TextInputType.multiline,
      minLines: 3,
      maxLines: null,
      validator: validateDescription,
      onSaved: (newValue) => description = newValue ?? '',
    );
    var majorField = DropdownButtonFormField(
      items: [
        DropdownMenuItem(
          value: null,
          child: Text('None'),
        ),
        ...MajorService.getMajors().map(
          (e) => DropdownMenuItem(
            value: e,
            child: Text(e),
          ),
        ),
      ],
      onChanged: (value) {},
      onSaved: (newValue) => major = newValue ?? '',
      decoration: const InputDecoration(
        label: Text('Major'),
      ),
    );
    return SizedBox(
      height: double.infinity,
      child: Form(
        onChanged: onFormChange,
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
                displayNameField,
                SizedBox(height: 16),
                headlineField,
                SizedBox(height: 16),
                linkedinField,
                SizedBox(height: 16),
                mobileNumberField,
                SizedBox(height: 16),
                descriptionField,
                SizedBox(height: 16),
                majorField,
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
                Wrap(
                  spacing: 8,
                  children: _selectedSkills
                      .map(
                        (e) => InputChip(
                          label: Text(e),
                          onDeleted: () => _removeSelectedSkill(e),
                        ),
                      )
                      .toList(),
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
                      onPressed: _formIsValid ? _submitData : null,
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
