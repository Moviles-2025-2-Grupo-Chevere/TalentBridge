import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

// ---- Tokens ----
const kBg = Color(0xFFFEF7E6); // cream
const kAmber = Color(0xFFFFC107); // amber accents
const kBrandGreen = Color(0xFF568C73); // brand green (logo fallback)
const kShadowCol = Color(0x33000000); // 20% black shadow
const kPillRadius = 26.0; // pill look

class Search extends StatefulWidget {
  const Search({super.key});

  @override
  State<Search> createState() => _SearchState();
}

class _SearchState extends State<Search> {
  // Search state
  final _queryCtrl = TextEditingController();

  // Fake “recent” items for the UI
  final _recents = const <String>[
    'Daniel Triviño',
    'ROBOCOL',
    'Proyectos Inteligencia Artificial',
  ];

  @override
  void initState() {
    super.initState();
    // En caso de autofill/pegado después del build
    WidgetsBinding.instance.addPostFrameCallback((_) {});
  }

  @override
  void dispose() {
    _queryCtrl.dispose();
    super.dispose();
  }

  void _applySearch() {
    final q = _queryCtrl.text.trim();
    if (q.isEmpty) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Buscando: "$q"')),
    );
    // TODO: hook real search here
  }

  @override
  Widget build(BuildContext context) {
    final labelStyle = Theme.of(
      context,
    ).textTheme.bodyMedium?.copyWith(color: kAmber);

    return Scaffold(
      backgroundColor: kBg,
      body: SafeArea(
        child: Column(
          children: [
            // ---------- Body ----------
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Label “Buscar”
                    Text('Search', style: labelStyle),
                    const SizedBox(height: 8),

                    // Fila: campo pill + botón de filtros (sin lupa)
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Campo de texto con borde pill y sombra
                        Expanded(
                          child: _shadowWrap(
                            TextField(
                              controller: _queryCtrl,
                              textInputAction: TextInputAction.search,
                              onSubmitted: (_) => _applySearch(),
                              inputFormatters: [
                                LengthLimitingTextInputFormatter(100),
                              ],
                              decoration: _pillInput(), // sin icono de lupa
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),

                        // Botón redondo de filtros
                        _shadowWrap(
                          Material(
                            color: Colors.white,
                            shape: const CircleBorder(),
                            child: InkWell(
                              customBorder: const CircleBorder(),
                              onTap: () =>
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    const SnackBar(
                                      content: Text('Filtros (pendiente)'),
                                    ),
                                  ),
                              child: const Padding(
                                padding: EdgeInsets.all(12),
                                child: Icon(Icons.tune, color: Colors.black87),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(height: 24),

                    // “Recientes”
                    Text('Recent searches', style: labelStyle),
                    const SizedBox(height: 12),

                    // Lista de recientes (icono reloj + tarjeta pill)
                    ..._recents.map(
                      (title) => Padding(
                        padding: const EdgeInsets.only(bottom: 12),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            const Icon(
                              Icons.access_time,
                              color: Colors.black45,
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Material(
                                color: Colors.white,
                                elevation: 4,
                                shadowColor: kShadowCol,
                                borderRadius: BorderRadius.circular(12),
                                child: InkWell(
                                  borderRadius: BorderRadius.circular(12),
                                  onTap: () => ScaffoldMessenger.of(context)
                                      .showSnackBar(
                                        SnackBar(
                                          content: Text('Abrir "$title"'),
                                        ),
                                      ),
                                  child: Padding(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 12,
                                      vertical: 12,
                                    ),
                                    child: Row(
                                      children: [
                                        CircleAvatar(
                                          radius: 16,
                                          backgroundColor:
                                              Colors.purple.shade200,
                                          child: const Icon(
                                            Icons.blur_on,
                                            size: 18,
                                            color: Colors.white,
                                          ),
                                        ),
                                        const SizedBox(width: 12),
                                        Expanded(
                                          child: Text(
                                            title,
                                            maxLines: 1,
                                            overflow: TextOverflow.ellipsis,
                                            style: const TextStyle(
                                              fontSize: 14,
                                            ),
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ---- UI helpers ----
InputDecoration _pillInput() {
  return InputDecoration(
    filled: true,
    fillColor: Colors.white,
    isDense: true,
    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kPillRadius),
      borderSide: const BorderSide(color: kAmber, width: 2),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kPillRadius),
      borderSide: const BorderSide(color: kAmber, width: 3),
    ),
  );
}

Widget _shadowWrap(Widget child) {
  return Container(
    decoration: BoxDecoration(
      borderRadius: BorderRadius.circular(kPillRadius),
      boxShadow: const [
        BoxShadow(
          color: kShadowCol,
          offset: Offset(0, 6),
          blurRadius: 12,
        ),
      ],
    ),
    child: child,
  );
}
