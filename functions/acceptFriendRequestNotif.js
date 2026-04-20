const {onDocumentUpdated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

exports.sendAcceptNotification = onDocumentUpdated(
    {
      region: "us-central1",
      document: "friend_requests/{requestId}",
    },
    async (event) => {
      const before = event.data.before.data();
      const after = event.data.after.data();

      if (!before || !after) return null;

      // only send if status changes TO accepted
      if (before.status === "accepted" || after.status !== "accepted") {
        return null;
      }

      const senderId = after.fromUid;
      const accepterId = after.toUid;

      if (!senderId) {
        console.log("Missing fromUid");
        return null;
      }

      try {
        const db = admin.firestore();

        // data for who accepted the friend request
        const accepterDoc = await db.collection("users").doc(accepterId).get();
        const accepterData = accepterDoc.data();
        const accepterName = (accepterData && accepterData.name) ?
            accepterData.name : "Someone";

        // data for who is receiving the notif
        const senderDoc = await db.collection("users").doc(senderId).get();
        const senderData = senderDoc.data();
        const fcmToken = senderData && senderData.fcmToken ?
            senderData.fcmToken : null;

        if (!fcmToken) {
          console.log("No FCM token for user: ", senderId);
          return null;
        }

        await admin.messaging().send({
          token: fcmToken,
          data: {
            type: "FRIEND_ACCEPTED",
            title: "Friend Request Accepted",
            fromUid: accepterId,
            body: `${accepterName} accepted your friend request`,
            uri: "phin://friends",
          },
        });

        console.log("Friend accepted notification sent!");
        return null;
      } catch (error) {
        console.error("Error sending notification: ", error);
        return null;
      }
    },
);
