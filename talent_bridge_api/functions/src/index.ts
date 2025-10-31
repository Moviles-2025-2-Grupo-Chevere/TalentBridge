/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import * as admin from "firebase-admin";
import { getMessaging } from "firebase-admin/messaging";
// import { getMessaging } from "firebase-admin/messaging";
import { setGlobalOptions, firestore } from "firebase-functions";
import {
  Change,
  FirestoreEvent,
  QueryDocumentSnapshot,
} from "firebase-functions/firestore";
import { onRequest } from "firebase-functions/https";
import * as logger from "firebase-functions/logger";

admin.initializeApp();
// const db = getDatabase();
const messaging = getMessaging();

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

export const helloWorld = onRequest((request, response) => {
  logger.info("Hello logs!", { structuredData: true });
  response.send("Hello from Firebase!");
});

export const sendApplicationNotification = firestore.onDocumentUpdated(
  "/users/{uid}",
  async (
    event: FirestoreEvent<Change<QueryDocumentSnapshot> | undefined>
  ): Promise<void> => {
    const uid = event.params.uid;
    const before = event.data.before.data();
    const after = event.data.after.data();
    const beforeApplications: Array<string | object> = before.applications;
    const afterApplications: Array<string | object> = after.applications;
    if (afterApplications.length > beforeApplications.length) {
      const newItem = afterApplications[afterApplications.length - 1];
      logger.info(
        `New application added for uid ${uid}: ${JSON.stringify(newItem)}`
      );
      try {
        const createdById: string = newItem["createdById"];
        if (createdById.length != 0) {
          logger.info(`Sending notification to uid ${createdById}`);
          const createdBy = (
            await admin.firestore().collection("users").doc(createdById).get()
          ).data();
          logger.info(`User to notify: ${JSON.stringify(createdBy)}`);
          const fcm: string = createdBy["fcm"] ?? "";
          if (fcm.length != 0) {
            const notification = {
              title: "Someone applied to your project!",
            };
            const mid = await messaging.send({ token: fcm, notification });
            logger.info(`User ${createdById} notified. mid = ${mid}`);
          }
        }
      } catch (error) {
        logger.error(error);
      }
    }
    logger.info(`User document with id ${uid} was updated. 
        Applications before: ${beforeApplications};
        Applications after: ${afterApplications};
        `);
    return;
  }
);
