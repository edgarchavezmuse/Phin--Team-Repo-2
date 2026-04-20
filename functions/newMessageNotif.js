const {onDocumentUpdated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

exports.sendNewMessageNotification = onDocumentUpdated(
    {
      region: "us-central1",
      document: "chats/{requestId}",
    },
    async (event) => {
      const before = event.data.before.data();
      const after = event.data.after.data();

      if (!before || !after) return null;

      // only send if timestamp has changed
      if (before.lastTimestamp == after.lastTimestamp) {
        return null;
      }

      // only send if new message is not blank
      if (after.lastMessage == "") {
        return null;
      }

      const senderId = after.participants[0]; // who sent the message
      const accepterId = after.participants[1]; // who is receiving the notif

      if (!senderId) {
        console.log("Missing fromUid");
        return null;
      }

      try {
        const db = admin.firestore();

        // data for who sent the message
        const senderDoc = await db.collection("users").doc(senderId).get();
        const senderData = senderDoc.data();
        const senderName = (senderData && senderData.name) ?
                senderData.name : "Someone";

        // data for who received the message
        const accepterDoc = await db.collection("users").doc(accepterId).get();
        const accepterData = accepterDoc.data();
        const fcmToken = accepterData && accepterData.fcmToken ?
                accepterData.fcmToken : null;

        if (!fcmToken) {
          console.log("No FCM token for user: ", accepterId);
          return null;
        } else {
          console.log("FCM token for messaging: ", fcmToken);
        }

        await admin.messaging().send({
          token: fcmToken,
          data: {
            type: "NEW_MESSAGE",
            title: "New Message",
            fromUid: senderId,
            body: `${senderName} sent you a message`,
            uri: `phin://messages/${senderId}`, // need to update this to route properly
          },
        });

        console.log("New message notification sent!");
        return null;
      } catch (error) {
        console.error("Error sending new message notification: ", error);
        return null;
      }
    },
);
