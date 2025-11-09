import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/domain/member_entity.dart';

// ---- Tokens ----
const kBg = Color(0xFFFEF7E6); // cream
const kAmber = Color(0xFFFFC107); // title & names
const kBrandGreen = Color(0xFF568C73); // brand tint / fallback
const kShadowCol = Color(0x33000000); // soft shadow
const kPillRadius = 26.0;
const kHomeBtn = Color(0xFF2E6674);

class Credits extends StatefulWidget {
  const Credits({super.key});

  @override
  State<Credits> createState() => _CreditsState();
}

class _CreditsState extends State<Credits> {
  final firebaseService = FirebaseService();
  List<MemberEntity> _members = [];
  Map<String, String?> _memberPhotoUrls = {};
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadMembers();
  }

  Future<void> _loadMembers() async {
    try {
      final members = await firebaseService.getAllMembers();

      // Load profile pictures for each member
      for (var member in members) {
        debugPrint('Loading photo URL for member: ${member.id}');
        final photoUrl = await firebaseService.getMemberUrlByUid(member.id);
        debugPrint('Loaded photo URL for ${member.id}: $photoUrl');
        _memberPhotoUrls[member.id] = photoUrl;
      }

      setState(() {
        _members = members;
        _isLoading = false;
      });
    } catch (e) {
      debugPrint('Error loading members: $e');
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Credits'),
      ),
      backgroundColor: kBg,
      body: SafeArea(
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : Column(
                children: [
                  // ---------- Header ----------
                  Padding(
                    padding: const EdgeInsets.fromLTRB(16, 20, 16, 16),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        Expanded(
                          child: Center(
                            child: Image.asset(
                              'assets/images/talent_bridge_logo.png',
                              height: 100,
                              errorBuilder: (_, __, ___) => const Icon(
                                Icons.account_balance,
                                size: 100,
                                color: kBrandGreen,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Text(
                            'The\nTeam',
                            style: Theme.of(context).textTheme.headlineLarge
                                ?.copyWith(
                                  color: kAmber,
                                  height: 1.0,
                                  fontWeight: FontWeight.w700,
                                ),
                          ),
                        ),
                      ],
                    ),
                  ),

                  // ---------- Team Cards ----------
                  Expanded(
                    child: ListView.builder(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 8,
                      ),
                      itemCount: _members.length,
                      itemBuilder: (context, index) {
                        final member = _members[index];
                        return _buildMemberCard(member);
                      },
                    ),
                  ),
                ],
              ),
      ),
    );
  }

  Widget _buildMemberCard(MemberEntity member) {
    final photoUrl = _memberPhotoUrls[member.id];

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: kShadowCol,
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Profile Picture
            CircleAvatar(
              radius: 40,
              backgroundColor: kBrandGreen.withOpacity(0.2),
              backgroundImage: photoUrl != null ? NetworkImage(photoUrl) : null,
              child: photoUrl == null
                  ? const Icon(
                      Icons.person,
                      size: 40,
                      color: kBrandGreen,
                    )
                  : null,
            ),
            const SizedBox(width: 16),

            // Name and Description
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    member.name,
                    style: const TextStyle(
                      color: kAmber,
                      fontSize: 20,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    member.description.isNotEmpty
                        ? member.description
                        : 'Team Member',
                    style: const TextStyle(
                      color: Colors.black87,
                      fontSize: 14,
                      height: 1.4,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
