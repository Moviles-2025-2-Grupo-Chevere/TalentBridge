import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import 'package:talent_bridge_fl/providers/fcm_provider.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/views/splash_screen.dart';
import 'services/connectivity_service.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'firebase_options.dart';
import 'package:hive_flutter/hive_flutter.dart';

// Global navigator key
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // await deleteDatabase(join(await getDatabasesPath(), 'talent_bridge.db'));

  await Hive.initFlutter(); // Initialize Hive for local storage
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );

  runApp(ProviderScope(child: const TalentBridge()));
}

class TalentBridge extends ConsumerStatefulWidget {
  const TalentBridge({super.key});
  @override
  ConsumerState<TalentBridge> createState() => _TalentBridgeState();
}

class _TalentBridgeState extends ConsumerState<TalentBridge> {
  final ConnectivityService _connectivityService = ConnectivityService();
  final _fb = FirebaseService();

  Future setUpInteractedMessage() async {
    RemoteMessage? initialMessage = await FirebaseMessaging.instance
        .getInitialMessage();
    if (initialMessage != null) {
      _handleMessage(initialMessage);
    }
    FirebaseMessaging.onMessageOpenedApp.listen(_handleMessage);
  }

  void _handleMessage(RemoteMessage message) {
    print("Opened app from notification: ${message.notification?.title}");
    FirebaseAnalytics.instance.logAppOpen();
    FirebaseAnalytics.instance.logEvent(
      name: 'app_open_from_notification',
      parameters: {'notification_title': message.notification?.title ?? ''},
    );
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _connectivityService.initialize(this.context);
    });
    _fb.setupNotifications();
    setUpInteractedMessage();
  }

  @override
  void dispose() {
    _connectivityService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    ref.listen(fcmTokenProvider, (previous, next) => _fb.sendFCMToken());
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
