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
import com.example.phinui.components.people.PendingFriendRequestDialog

@Composable fun SendFriendRequestDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
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
                Text("Send", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onTertiary)
            }
        }
    )
}

@Composable
fun PendingFriendRequestDialog(
    name: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Friend request pending")
        },
        text = {
            Text(
                buildAnnotatedString {
                    append("You already have a friend request pending with ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(name)
                    }
                    append(".")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("OK", color = MaterialTheme.colorScheme.primary)
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                Text("Ok", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable fun ActiveChatDialog(
    name: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Active Chat")
        },
        text = {
            Text(
                buildAnnotatedString {
                    append("You already have an active chat with ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(name)
                    }
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Ok", color = MaterialTheme.colorScheme.primary)
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                Text("Remove", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onTertiary)
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                Text("Block", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onTertiary)
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                Text("Block", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onTertiary)
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                Text("Unblock", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onTertiary)
            }
        }
    )
}