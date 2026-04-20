const {onDocumentUpdated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

console.log("ACCEPT MESSAGE REQUEST FUNCTION STARTED");

exports.sendAcceptMessageRequestNotification = onDocumentUpdated(
    {
      region: "us-central1",
      document: "chats/{requestId}",
    },
    async (event) => {
      const before = event.data.before.data();
      const after = event.data.after.data();

      if (!before) {
        console.log("Missing before");
        return null;
      } else if (!after) {
        console.log("Missing after");
        return null;
      }

      // only send if messageRequestApproved changes TO accepted
      if (before.messageRequestApproved || !after.messageRequestApproved) {
        return null;
      }

      const senderId = after.participants[1];
      const receiverId = after.participants[0];

      if (!senderId) {
        console.log("Missing senderId");
        return null;
      } else if (!receiverId) {
        console.log("Missing receiverId");
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
            type: "ACCEPT_MESSAGE_REQUEST",
            title: "Message Request Accepted",
            fromUid: senderId,
            body: `${senderName} accepted your message request`,
            uri: `phin://userlist`,
          },
        });

        console.log("Accept message request notification sent!");
        return null;
      } catch (error) {
        console.error(
            "Error sending accept message request notification: ",
            error);
        return null;
      }
    },
);
