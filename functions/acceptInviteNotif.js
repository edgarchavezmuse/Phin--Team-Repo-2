const {onDocumentUpdated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

exports.sendAcceptInviteNotification = onDocumentUpdated(
    {
      region: "us-central1",
      document: "chats/{chatId}/messages/{messageId}",
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

      const {senderID, type, participants: beforeParticipants} = before;
      const {deleted, participants: afterParticipants} = after;
      const chatId = event.params.chatId;

      // check if it's a study session invite
      if (type !== "invitation" || !afterParticipants) return null;

      // ignore deleted messages
      if (deleted) return null;

      const receiverId = senderID;

      let senderId = null;

      for (const userId of Object.keys(afterParticipants)) {
        const beforeStatus =
            beforeParticipants ? beforeParticipants[userId] : null;
        const afterStatus = afterParticipants[userId];

        if (beforeStatus === "PENDING" && afterStatus === "ACCEPTED") {
          senderId = userId;
          break;
        }
      }

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

        const mutedBy = chatData.mutedBy || [];

        if (mutedBy.includes(receiverId)) {
          console.log(
              `Chat is muted by ${receiverId}. Skipping notification`,
          );
          return null;
        }

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

        const isGroupChat = chatData.type === "group";

        const uri = isGroupChat ?
        `phin://group_messages/${chatId}/${chatData.groupName}` :
        `phin://messages/${senderID}`;

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
            type: "ACCEPT_INVITE",
            title: "Study Session Invite Accepted",
            fromUid: senderId,
            body: `${senderName} accepted your study session invite`,
            uri: uri,
          },
        });

        console.log("Accepted invite notification sent!");
        return null;
      } catch (error) {
        console.error(
            "Error sending accepted invite notification: ", error);
        return null;
      }
    },
);
