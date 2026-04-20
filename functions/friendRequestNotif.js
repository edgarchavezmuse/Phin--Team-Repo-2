const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

console.log("FUNCTION STARTED");

exports.sendFriendRequestNotification = onDocumentCreated(
    {
      region: "us-central1",
      document: "friend_requests/{requestId}",
    },
    async (event) => {
      const snapshot = event.data;
      if (!snapshot) return;

      const data = snapshot.data();
      const receiverId = data.toUid;
      const senderName = data.fromName;

      if (!receiverId) {
        console.log("Missing toUid");
        return null;
      }

      try {
        // get receiver user doc
        const userDoc = await admin.firestore()
            .collection("users")
            .doc(receiverId)
            .get();

        const userData = userDoc.data();
        const fcmToken = userData ? userData.fcmToken : null;


        if (!fcmToken) {
          console.log("No FCM token found for user:", receiverId);
          return null;
        }

        // send notification
        await admin.messaging().send({
          token: fcmToken,
          data: {
            type: "FRIEND_REQUEST",
            title: "New Friend Request",
            fromUid: data.fromUid,
            body: `${senderName} sent you a friend request`,
            uri: "phin://friends",
          },
        });

        console.log("Friend request notification sent!");
        return null;
      } catch (error) {
        console.error("Error sending notification:", error);
        return null;
      }
    });
