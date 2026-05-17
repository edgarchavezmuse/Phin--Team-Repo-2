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

      const {senderID, deleted, type} = message;
      const chatId = event.params.chatId;

      // check if it's a study session invite
      if (type !== "invitation" || !senderID) return null;

      // ignore deleted messages
      if (deleted) return null;

      try {
        const db = admin.firestore();

        const chatDoc = await db.collection("chats").doc(chatId).get();
        const chatData = chatDoc.data();

        if (!chatData) return null;

        const mutedBy = chatData.mutedBy || [];
        const participants = chatData.participants || [];

        const receiverIds = participants.filter(
            (uid) => uid !== senderID,
        );

        if (!receiverIds.length) return null;

        const isGroupChat = chatData.type === "group";
        const uri = isGroupChat ?
        `phin://group_messages/${chatId}/${chatData.groupName}` :
        `phin://messages/${senderID}`;

        const senderDoc = await db.collection("users").doc(senderID).get();
        const senderName = senderDoc.data() ? senderDoc.data().name : "Someone";

        // send notif to every receiver
        await Promise.all(
            receiverIds.map(async (receiverId) => {
              try {
              // skip muted users
                if (mutedBy.includes(receiverId)) {
                  console.log(
                      `Chat is muted by ${receiverId}. Skipping notification`,
                  );
                  return;
                }

                const receiverDoc = await db
                    .collection("users")
                    .doc(receiverId)
                    .get();

                const receiverData = receiverDoc.data();

                if (!receiverData) return;

                const fcmToken = receiverData.fcmToken;
                const activeChatID = receiverData.activeChatID;
                const lastActive = receiverData.lastActive;

                if (!fcmToken) {
                  console.log(`No FCM token for user ${receiverId}`);
                  return;
                } else {
                  console.log(
                      `FCM token for user ${receiverId} is ${fcmToken}`,
                  );
                }

                const now = Date.now();

                const lastActiveTime =
              lastActive &&
              typeof lastActive.toMillis === "function" ?
              lastActive.toMillis() :
              0;

                const isInChat = activeChatID === chatId;
                const isActive = (now - lastActiveTime) < 120000;

                // skip notif if currently viewing chat
                if (isInChat && isActive) {
                  console.log(
                      `${receiverId} is currently in chat. Skipping`,
                  );
                  return;
                }

                await admin.messaging().send({
                  token: fcmToken,
                  data: {
                    type: "INVITE",
                    title: "Study Session Invite",
                    body: `${senderName} sent you a study session invite`,
                    uri: uri,
                  },
                });

                console.log(`Study session notif sent to ${receiverId}`);
              } catch (err) {
                console.error(
                    `Error sending study session notif to ${receiverId}: `,
                    err,
                );
              }
            }),
        );

        return null;
      } catch (error) {
        console.error(
            "Error sending study session notifications: ",
            error,
        );
        return null;
      }
    },
);
