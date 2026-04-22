const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

console.log("MESSAGE REQUEST FUNCTION STARTED");

exports.sendMessageRequestNotification = onDocumentCreated(
    {
      region: "us-central1",
      document: "chats/{requestId}",
    },
    async (event) => {
      const snapshot = event.data;
      if (!snapshot) return;

      const data = snapshot.data();
      const receiverId = data.participants[1];
      const senderId = data.participants[0];

      if (!receiverId) {
        console.log("Missing toUid");
        return null;
      }

      if (!senderId) {
        console.log("Missing fromUid");
        return null;
      }

      try {
        const db = admin.firestore();

        // sender data
        const senderDoc = await db.collection("users").doc(senderId).get();
        const senderData = senderDoc.data();
        const senderName = (senderData && senderData.name) ?
                senderData.name : "Someone";

        // receiver data
        const receiverDoc = await db.collection("users").doc(receiverId).get();
        const receiverData = receiverDoc.data();
        const fcmToken = receiverData && receiverData.fcmToken ?
                receiverData.fcmToken : null;


        if (!fcmToken) {
          console.log("No FCM token found for user: ", receiverId);
          return null;
        } else {
          console.log("FCM token for messaging: ", fcmToken);
        }

        // send notification
        await admin.messaging().send({
          token: fcmToken,
          data: {
            type: "MESSAGE_REQUEST",
            title: "New Message Request",
            fromUid: senderId,
            body: `${senderName} sent you a message request`,
            uri: `phin://userlist?tab=2`,
          },
        });

        console.log("Message request notification sent!");
        return null;
      } catch (error) {
        console.error("Error sending message request notification: ", error);
        return null;
      }
    },
);
