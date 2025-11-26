import 'package:flutter/material.dart';
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

  @override
  void dispose() {
    _commentCtrl.dispose();
    super.dispose();
  }

  void _sendComment() {
    final text = _commentCtrl.text.trim();
    if (text.isEmpty) return;

    // 👇 Por ahora solo lo mostramos en consola.
    // Más adelante aquí vamos a guardar en Firestore.
    debugPrint(
      '[COMMENTS] send "${widget.project.id}" -> "$text"',
    );

    _commentCtrl.clear();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Comment sent (mock, sin backend aún)')),
    );
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
          // 🔹 Más adelante aquí pondremos la lista real de comentarios (Firestore)
          Expanded(
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: const [
                Text(
                  'Here will appear the comments for this project.\n'
                  '(Next step: connect to Firestore.)',
                ),
              ],
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
