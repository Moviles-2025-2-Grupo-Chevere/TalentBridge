import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/views/splash_screen.dart';
import 'services/connectivity_service.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'firebase_options.dart';

// Global navigator key
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // await deleteDatabase(join(await getDatabasesPath(), 'talent_bridge.db'));
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );
  runApp(ProviderScope(child: const TalentBridge()));
}

class TalentBridge extends StatefulWidget {
  const TalentBridge({super.key});
  @override
  State<TalentBridge> createState() => _TalentBridgeState();
}

class _TalentBridgeState extends State<TalentBridge> {
  final ConnectivityService _connectivityService = ConnectivityService();

  Future<void> _setupNotifications() async {
    var messaging = FirebaseMessaging.instance;
    final notificationSettings = await messaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
      provisional: true,
    );
    final token = await messaging.getToken();
    print("Token: $token");
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _connectivityService.initialize(this.context);
    });
    _setupNotifications();
  }

  @override
  void dispose() {
    _connectivityService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Talent Bridge',
      navigatorKey: navigatorKey,
      navigatorObservers: [
        FirebaseAnalyticsObserver(analytics: FirebaseAnalytics.instance),
      ],
      home: const SplashScreen(),
    );
  }
}
