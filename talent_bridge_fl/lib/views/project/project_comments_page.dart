import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/services.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';

class ProjectCommentsPage extends StatefulWidget {
  const ProjectCommentsPage({
    super.key,
    required this.project,
  });

  final ProjectEntity project;

  @override
  State<ProjectCommentsPage> createState() => _ProjectCommentsPageState();
}

class _ProjectCommentsPageState extends State<ProjectCommentsPage> {
  final _commentCtrl = TextEditingController();

  Future<void> _sendComment() async {
    final text = _commentCtrl.text.trim();
    if (text.isEmpty) return;

    final user = FirebaseAuth.instance.currentUser;
    final authorId = user?.uid ?? 'anon';
    final authorName = user?.displayName ?? 'Unknown user';
    final projectId = widget.project.id ?? '';
    final projectUserId = widget.project.createdById ?? '';

    if (projectId.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Error: project without ID')),
      );
      return;
    }

    try {
      await FirebaseFirestore.instance.collection('comments').add({
        'comment_id': '', // Firestore generará el ID real
        'authorId': authorId,
        'authorName': authorName,
        'createdAt': FieldValue.serverTimestamp(),
        'text': text,
        'project_id': projectId,
        'project_user_id': projectUserId,
      });

      _commentCtrl.clear();

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Comment sent')),
      );
    } catch (e) {
      debugPrint('Error sending comment: $e');
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Error sending comment')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final projectId = widget.project.id ?? '';

    return Scaffold(
      appBar: AppBar(
        title: Text('Comments · ${widget.project.title}'),
      ),
      body: Column(
        children: [
          // 📜 Lista de comentarios filtrados por proyecto
          Expanded(
            child: StreamBuilder<QuerySnapshot<Map<String, dynamic>>>(
              stream: FirebaseFirestore.instance
                  .collection('comments')
                  .where('project_id', isEqualTo: projectId)
                  .snapshots(includeMetadataChanges: true),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (snapshot.hasError) {
                  return Center(
                    child: Text(
                      'Error loading comments: ${snapshot.error}',
                      textAlign: TextAlign.center,
                    ),
                  );
                }

                final docs = snapshot.data?.docs ?? [];
                if (docs.isEmpty) {
                  return const Center(
                    child: Text(
                      'No comments yet.\nBe the first to write one!',
                      textAlign: TextAlign.center,
                    ),
                  );
                }

                return ListView.builder(
                  itemCount: docs.length,
                  itemBuilder: (context, index) {
                    final doc = docs[index];
                    final data = doc.data();
                    final text = (data['text'] ?? '') as String;
                    final authorName =
                        (data['authorName'] ?? 'Unknown') as String;
                    final ts = data['createdAt'];
                    DateTime? createdAt;
                    if (ts is Timestamp) createdAt = ts.toDate();

                    final isPending = doc.metadata.hasPendingWrites;
                    final dateLabel = createdAt == null
                        ? ''
                        : '${createdAt.toLocal().toString().split(".")[0]} · ';

                    return ListTile(
                      leading: const CircleAvatar(
                        child: Icon(Icons.person),
                      ),
                      title: Text(
                        authorName,
                        style: const TextStyle(
                          fontWeight: FontWeight.w500,
                          fontSize: 14,
                        ),
                      ),
                      subtitle: Text(
                        '$dateLabel$text${isPending ? " (pending sync...)" : ""}',
                        style: const TextStyle(fontSize: 12),
                      ),
                    );
                  },
                );
              },
            ),
          ),

          // 💬 Input para añadir un comentario nuevo
          SafeArea(
            top: false,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: const BoxDecoration(
                border: Border(
                  top: BorderSide(color: Colors.black12),
                ),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _commentCtrl,
                      inputFormatters: [LengthLimitingTextInputFormatter(200)],
                      decoration: InputDecoration(
                        filled: true,
                        fillColor: Colors.white,
                        hintText: 'Write a comment...',
                        isDense: true,
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 14,
                          vertical: 10,
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: const BorderSide(
                            color: Colors.black12,
                            width: 1.2,
                          ),
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: const BorderSide(
                            color: Colors.black26,
                            width: 1.6,
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 6),
                  IconButton(
                    onPressed: _sendComment,
                    icon: const Icon(Icons.send, size: 20),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
