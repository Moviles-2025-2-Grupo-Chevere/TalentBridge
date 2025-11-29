import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';

import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/comments_local_helper.dart';

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
        // comment_id: usamos el id del doc si lo necesitas luego
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
  void dispose() {
    _commentCtrl.dispose();
    super.dispose();
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

                // 📝 Si hay datos de Firestore, los mostramos y además los cacheamos localmente
                if (docs.isNotEmpty) {
                  // Guardamos versión simple en local (máx 50)
                  final toCache = docs.take(50).map((doc) {
                    final data = doc.data();
                    final ts = data['createdAt'];
                    int? millis;
                    if (ts is Timestamp) {
                      millis = ts.millisecondsSinceEpoch;
                    }
                    return <String, dynamic>{
                      'text': data['text'] ?? '',
                      'authorName': data['authorName'] ?? 'Unknown',
                      'createdAtMillis': millis,
                    };
                  }).toList();

                  // No bloqueamos el build: lo mandamos a microtask
                  Future.microtask(
                    () => CommentsLocalHelper.saveProjectComments(
                      projectId,
                      toCache,
                    ),
                  );

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
                }

                // 🧊 Si Firestore viene vacío, intentamos local storage
                return FutureBuilder<List<Map<String, dynamic>>>(
                  future: CommentsLocalHelper.getProjectComments(projectId),
                  builder: (context, snapLocal) {
                    if (snapLocal.connectionState == ConnectionState.waiting) {
                      return const Center(child: CircularProgressIndicator());
                    }

                    final localComments = snapLocal.data ?? [];
                    if (localComments.isEmpty) {
                      return const Center(
                        child: Text(
                          'No comments yet.\nBe the first to write one!',
                          textAlign: TextAlign.center,
                        ),
                      );
                    }

                    return ListView.builder(
                      itemCount: localComments.length,
                      itemBuilder: (context, index) {
                        final c = localComments[index];
                        final text = (c['text'] ?? '') as String;
                        final authorName =
                            (c['authorName'] ?? 'Unknown') as String;
                        final millis = c['createdAtMillis'] as int?;
                        String dateLabel = '';
                        if (millis != null) {
                          final dt = DateTime.fromMillisecondsSinceEpoch(
                            millis,
                          );
                          dateLabel =
                              '${dt.toLocal().toString().split(".")[0]} · ';
                        }

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
                            '$dateLabel$text (local cached)',
                            style: const TextStyle(fontSize: 12),
                          ),
                        );
                      },
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
                      inputFormatters: [
                        LengthLimitingTextInputFormatter(200),
                      ],
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
