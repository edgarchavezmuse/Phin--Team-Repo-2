const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

exports.sendNewMessageNotification = onDocumentCreated(
    {
      region: "us-central1",
      document: "chats/{chatId}/messages/{messageId}",
    },
    async (event) => {
      // const before = event.data.before.data();
      // const after = event.data.after.data();

      const message = event.data.data();
      if (!message) return null;

      const {senderID, text, deleted} = message;
      const chatId = event.params.chatId;

      // ignore deleted messages
      if (deleted) return null;

      if (!senderID || !text) return null;

      // if (!before || !after) return null;

      // only send if timestamp has changed
      // if (before.lastTimestamp == after.lastTimestamp) {
      //  return null;
      // }

      // only send if new message is not blank
      // if (after.lastMessage == "") {
      //   return null;
      // }

      // const senderId = after.participants[0]; // who sent the message
      // const accepterId = after.participants[1]; // who is receiving the notif

      // if (!senderId) {
      //   console.log("Missing fromUid");
      //   return null;
      // }

      try {
        const db = admin.firestore();

        const chatDoc = await db.collection("chats").doc(chatId).get();
        const chatData = chatDoc.data();

        if (!chatData || !chatData.participants) return null;

        const receiverId = chatData.participants.find(
            (uid) => uid !== senderID,
        );

        if (!receiverId) return null;

        const receiverDoc = await db.collection("users").doc(receiverId).get();
        const receiverData = receiverDoc.data();

        const fcmToken = receiverData ? receiverData.fcmToken : null;
        const activeChatID = receiverData ? receiverData.activeChatID : null;
        const lastActive = receiverData ? receiverData.lastActive : null;

        // data for who sent the message
        // const senderDoc = await db.collection("users").doc(senderId).get();
        // const senderData = senderDoc.data();
        // const senderName = (senderData && senderData.name) ?
        //        senderData.name : "Someone";

        // data for who received the message
        // const accepterDoc = await db.collection("users")
        // .doc(accepterId).get();
        // const accepterData = accepterDoc.data();
        // const fcmToken = accepterData && accepterData.fcmToken ?
        //         accepterData.fcmToken : null;

        // const activeChatID = accepterData ? accepterData.activeChatID : null;
        // const lastActive = accepterData ? accepterData.lastActive : null;

        if (!fcmToken) {
          console.log("No FCM token for receiving user");
          return null;
        } else {
          console.log("FCM token for messaging: ", fcmToken);
        }

        const senderDoc = await db.collection("users").doc(senderID).get();
        const senderName = senderDoc.data() ? senderDoc.data().name : "Someone";

        // const chatId = event.params.requestId;
        const now = Date.now();
        const lastActiveTime =
          lastActive && typeof lastActive.toMillis === "function" ?
          lastActive.toMillis() : 0;

        const isInChat = activeChatID === chatId;
        const isActive = (now - lastActiveTime) < 120000;

        // only send if user is not on the chat screen
        if (isInChat && isActive) {
          console.log("User is currently in this chat. Skipping notification");
          return null;
        }

        await admin.messaging().send({
          token: fcmToken,
          data: {
            type: "NEW_MESSAGE",
            title: "New Message",
            // fromUid: senderId,
            fromUid: senderID,
            body: `${senderName} sent you a message`,
            uri: `phin://messages/${senderID}`, // need to update this to route properly
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
