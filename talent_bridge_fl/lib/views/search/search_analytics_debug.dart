import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';

class SearchAnalyticsDebugPage extends StatelessWidget {
  const SearchAnalyticsDebugPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Search Analytics (debug)'),
      ),
      body: FutureBuilder<QuerySnapshot>(
        future: FirebaseFirestore.instance
            .collection('search_logs')
            .orderBy('timestamp', descending: true)
            .get(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          }

          final docs = snapshot.data?.docs ?? [];
          if (docs.isEmpty) {
            return const Center(
              child: Text('No search logs yet'),
            );
          }

          final totalSearches = docs.length;
          int zeroResultSearches = 0;
          final Map<String, int> zeroKeywords = {};

          for (final d in docs) {
            final data = d.data() as Map<String, dynamic>;
            final query = (data['query'] ?? '') as String;
            final resultsCount = (data['resultsCount'] ?? 0) as int;

            if (resultsCount == 0) {
              zeroResultSearches++;
              zeroKeywords.update(query, (v) => v + 1, ifAbsent: () => 1);
            }
          }

          final zeroRate = zeroResultSearches == 0
              ? 0.0
              : (zeroResultSearches / totalSearches) * 100;

          // Ordenamos keywords por frecuencia
          final sortedKeywords = zeroKeywords.entries.toList()
            ..sort((a, b) => b.value.compareTo(a.value));

          return Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Total searches: $totalSearches'),
                Text('Zero-result searches: $zeroResultSearches'),
                Text('Zero-result rate: ${zeroRate.toStringAsFixed(1)} %'),
                const SizedBox(height: 16),
                const Text(
                  'Top zero-result keywords:',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                Expanded(
                  child: ListView.builder(
                    itemCount: sortedKeywords.length,
                    itemBuilder: (context, index) {
                      final e = sortedKeywords[index];
                      final keyword = e.key;
                      final count = e.value;

                      final percOfZero = zeroResultSearches == 0
                          ? 0.0
                          : (count / zeroResultSearches) * 100.0;

                      return ListTile(
                        title: Text(
                          keyword.isEmpty ? '(empty query)' : keyword,
                        ),
                        subtitle: Text(
                          'Times: $count · '
                          '${percOfZero.toStringAsFixed(1)}% of zero-result searches',
                        ),
                      );
                    },
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
