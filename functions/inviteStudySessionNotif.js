const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

exports.sendInviteNotification = onDocumentCreated(
    {
      region: "us-central1",
      document: "chats/{chatId}/messages/{messageId}",
    },
    async (event) => {
      const message = event.data.data();
      if (!message) return null;

      const {deleted, type, participants} = message;
      const chatId = event.params.chatId;

      // check if it's a study session invite
      if (type !== "invitation" || !participants) return null;

      // ignore deleted messages
      if (deleted) return null;

      const senderId = Object.keys(participants).find(
          (userId) => participants[userId] === "ACCEPTED",
      );

      const receiverId = Object.keys(participants).find(
          (userId) => participants[userId] === "PENDING",
      );

      if (!senderId) {
        console.log("Missing senderId");
        return null;
      } else if (!receiverId) {
        console.log("Missing receiverId");
        return null;
      }


      try {
        const db = admin.firestore();

        const chatDoc = await db.collection("chats").doc(chatId).get();
        const chatData = chatDoc.data();

        if (!chatData || !chatData.participants) return null;

        const receiverDoc = await db.collection("users").doc(receiverId).get();
        const receiverData = receiverDoc.data();

        const fcmToken = receiverData ? receiverData.fcmToken : null;
        const activeChatID = receiverData ? receiverData.activeChatID : null;
        const lastActive = receiverData ? receiverData.lastActive : null;

        if (!fcmToken) {
          console.log("No FCM token for receiving user");
          return null;
        } else {
          console.log("FCM token for invite: ", fcmToken);
        }

        const senderDoc = await db.collection("users").doc(senderId).get();
        const senderName = senderDoc.data() ? senderDoc.data().name : "Someone";

        const now = Date.now();
        const lastActiveTime =
            lastActive && typeof lastActive.toMillis === "function" ?
            lastActive.toMillis() : 0;

        const isInChat = activeChatID === chatId;
        const isActive = (now - lastActiveTime) < 120000;

        // only send if receiver is not on chat screen
        if (isInChat && isActive) {
          console.log("User is currently in this chat. Skipping notification");
          return null;
        }

        await admin.messaging().send({
          token: fcmToken,
          data: {
            type: "INVITE",
            title: "Study Session Invite",
            body: `${senderName} sent you a study session invite`,
            uri: `phin://messages/${senderId}`,
          },
        });

        console.log("Study session invite notification sent!");
        return null;
      } catch (error) {
        console.error(
            "Error sending study session invite notification: ", error);
        return null;
      }
    },
);
