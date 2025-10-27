import 'dart:async';

import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final notificationProvider = NotifierProvider(NotificationNotifier.new);

class NotificationNotifier extends Notifier {
  final messaging = FirebaseMessaging.instance;

  late StreamSubscription<RemoteMessage> _msgSuscription;
  // final token = await messaging.getToken();
  @override
  build() {
    state = null;
    _msgSuscription = FirebaseMessaging.onMessage.listen((
      RemoteMessage message,
    ) {
      // Handle when the app is in foreground
      state = message.notification?.title;
      print('Notification title: $state');
    });
    ref.onDispose(() => _msgSuscription.cancel());
    return state;
  }
}
