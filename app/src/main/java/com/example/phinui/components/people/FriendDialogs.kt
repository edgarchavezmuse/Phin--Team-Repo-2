package com.example.phinui.components.people

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.HeaderRed
import com.example.phinui.ui.theme.NavText

@Composable fun SendFriendRequestDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background,
        title = {
            Text("Send friend request?")
        },
        text = {
            Text(
                buildAnnotatedString {
                    append("Do you want to send a friend request to ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(name)
                    }
                    append("?")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Send", color = HeaderRed)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = NavText)
            }
        }
    )
}

@Composable fun AlreadyFriendDialog(
    name: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background,
        title = {
            Text("Already friends")
        },
        text = {
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(name)
                    }
                    append(" is already your friend.")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Ok", color = HeaderRed)
            }
        }
    )
}

@Composable fun RemoveFriendDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background,
        title = {
            Text("Remove Friend?")
        },
        text = {
            Text(
                buildAnnotatedString {
                    append("Are you sure you want to remove ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(name)
                    }
                    append(" from your friends?")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Remove", color = HeaderRed)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = NavText)
            }
        }
    )
}

@Composable fun BlockUserDialog(
    name: String,
    isFriend: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background,
        title = {
            Text("Block user?")
        },
        text = {
            Text(
                buildAnnotatedString {
                    append("Are you sure you want to block ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(name)
                    }

                    if (isFriend) {
                        append("? This will also remove them from your friends.")
                    }
                    else {
                        append("?")
                    }
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Block", color = HeaderRed)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = NavText)
            }
        }
    )
}

@Composable fun BlockFriendDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background,
        title = {
            Text("Block user?")
        },
        text = {
            Text(
                buildAnnotatedString {
                    append("Are you sure you want to block ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(name)
                    }
                    append("? This will also remove them from your friends")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Block", color = HeaderRed)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = NavText)
            }
        }
    )
}

@Composable fun UnblockUserDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background,
        title = {
            Text("Unblock user?")
        },
        text = {
            Text(
                buildAnnotatedString {
                    append("Are you sure you want to unblock ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(name)
                    }
                    append("?")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Unblock", color = HeaderRed)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = NavText)
            }
        }
    )
}