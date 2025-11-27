import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';

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
  // Colección: projects/<projectId>/comments
  CollectionReference<Map<String, dynamic>> get _commentsRef {
    final projectId = widget.project.id ?? '';
    return FirebaseFirestore.instance
        .collection('projects')
        .doc(projectId)
        .collection('comments');
  }

  @override
  void dispose() {
    _commentCtrl.dispose();
    super.dispose();
  }

  Future<void> _sendComment() async {
    final text = _commentCtrl.text.trim();
    if (text.isEmpty) return;

    final projectId = widget.project.id ?? '';
    if (projectId.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Error: project without ID')),
      );
      return;
    }

    final user = FirebaseAuth.instance.currentUser;
    final authorId = user?.uid ?? 'anon';
    final authorName = user?.displayName ?? 'Unknown user';

    try {
      await _commentsRef.add({
        'text': text,
        'authorId': authorId,
        'authorName': authorName,
        'createdAt': FieldValue.serverTimestamp(),
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
    final p = widget.project;

    return Scaffold(
      appBar: AppBar(
        title: Text('Comments · ${p.title}'),
      ),
      body: Column(
        children: [
          Expanded(
            child: StreamBuilder<QuerySnapshot<Map<String, dynamic>>>(
              stream: _commentsRef
                  .orderBy('createdAt', descending: false)
                  .snapshots(),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (snapshot.hasError) {
                  return Center(
                    child: Text('Error loading comments: ${snapshot.error}'),
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
                  padding: const EdgeInsets.all(16),
                  itemCount: docs.length,
                  itemBuilder: (context, index) {
                    final data = docs[index].data();
                    final text = (data['text'] ?? '') as String;
                    final authorName =
                        (data['authorName'] ?? 'Unknown') as String;
                    final ts = data['createdAt'];
                    DateTime? createdAt;
                    if (ts is Timestamp) {
                      createdAt = ts.toDate();
                    }

                    final subtitle = createdAt == null
                        ? text
                        : '${createdAt.toLocal()} · $text';

                    return ListTile(
                      leading: const CircleAvatar(
                        child: Icon(Icons.person),
                      ),
                      title: Text(authorName),
                      subtitle: Text(subtitle),
                    );
                  },
                );
              },
            ),
          ),

          // 🔹 Caja de texto para escribir el comentario
          SafeArea(
            top: false,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
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
                      maxLength: 200, // límite que dijiste
                      maxLines: null,
                      decoration: const InputDecoration(
                        hintText: 'Write a comment...',
                        counterText: '', // oculta "0/200" si molesta
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton(
                    onPressed: _sendComment,
                    icon: const Icon(Icons.send),
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
