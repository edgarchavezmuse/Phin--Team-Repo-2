const admin = require("firebase-admin");

admin.initializeApp();

// import functions file
const friendRequest = require("./friendRequestNotif");
const acceptRequest = require("./acceptFriendRequestNotif");
const newMessage = require("./newMessageNotif");
const messageRequest = require("./messageRequestNotif");
const acceptMessageRequest = require("./acceptMessageRequestNotif");

// exports to Firebase
exports.sendFriendRequestNotification =
friendRequest.sendFriendRequestNotification;

exports.sendAcceptNotification =
acceptRequest.sendAcceptNotification;

exports.sendNewMessageNotification =
newMessage.sendNewMessageNotification;

exports.sendMessageRequestNotification =
messageRequest.sendMessageRequestNotification;

exports.sendAcceptMessageRequestNotification =
acceptMessageRequest.sendAcceptMessageRequestNotification;

