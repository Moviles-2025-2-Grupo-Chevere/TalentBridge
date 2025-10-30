import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/providers/profile_provider.dart';
import 'package:talent_bridge_fl/util/string_utils.dart';

class DownloadProjects extends ConsumerWidget {
  const DownloadProjects({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(profileProvider);
    return user == null
        ? Center(child: CircularProgressIndicator())
        : Padding(
            padding: const EdgeInsets.all(8.0),
            child: ListView(
              children: [
                ...user.projects!.map(
                  (e) => ListTile(
                    title: Text(capitalize(e.title)),
                    onTap: () {},
                  ),
                ),
              ],
            ),
          );
  }
}
