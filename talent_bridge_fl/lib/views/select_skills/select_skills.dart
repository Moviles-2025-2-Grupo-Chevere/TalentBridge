import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/skill_entity.dart';

class SelectSkills extends StatefulWidget {
  SelectSkills({
    super.key,
    required List<SkillEntity> skills,
    required this.selectedSkills,
  }) : sortedSkills = List.of(skills)
         ..sort(
           (a, b) => a.label.compareTo(b.label),
         );

  final List<SkillEntity> sortedSkills;
  final Set<SkillEntity> selectedSkills;

  @override
  State<SelectSkills> createState() => _SelectSkillsState();
}

class _SelectSkillsState extends State<SelectSkills> {
  final _searchController = TextEditingController();
  List<SkillEntity> filteredSkills = [];

  void _onQueryChanged() {
    final query = _searchController.text.trim().toLowerCase();
    setState(() {
      filteredSkills = widget.sortedSkills
          .where(
            (element) =>
                element.label.toLowerCase().contains(query.toLowerCase()),
          )
          .toList();
    });
  }

  @override
  void initState() {
    super.initState();
    filteredSkills = widget.sortedSkills;
    _searchController.addListener(_onQueryChanged);
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        children: [
          TextField(
            decoration: InputDecoration(label: const Text('Search')),
            controller: _searchController,
          ),
          SizedBox(height: 12),
          Expanded(
            child: ListView.builder(
              itemCount: filteredSkills.length,
              itemBuilder: (context, index) => CheckboxListTile(
                title: Text(filteredSkills[index].label),
                key: ValueKey(filteredSkills[index]),
                value: widget.selectedSkills.contains(filteredSkills[index]),
                onChanged: (value) {
                  if (value == null) return;
                  setState(() {
                    if (value) {
                      widget.selectedSkills.add(filteredSkills[index]);
                    } else {
                      widget.selectedSkills.remove(filteredSkills[index]);
                    }
                  });
                },
              ),
            ),
          ),
        ],
      ),
    );
  }
}
