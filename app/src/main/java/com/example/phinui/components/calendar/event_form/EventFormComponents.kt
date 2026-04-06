package com.example.phinui.components.calendar.event_form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun EventInputRow(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    trailingIcon: ImageVector? = null
) {
    val accentRed = Color(0xFFFF1F1F)
    val cardColor = Color(0xFFFDFDFD)
    val labelColor = Color(0xFF7A766F)
    val textColor = Color(0xFF1F1F1F)
    val placeholderColor = Color(0xFF9A948C)
    val indicatorColor = if (isActive) accentRed else Color(0xFFD8D2CB)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentRed,
                        shape = RoundedCornerShape(999.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = textColor
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (value.isBlank()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = placeholderColor
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = accentRed.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            color = indicatorColor,
                            shape = RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun EventPickerRow(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val accentRed = Color(0xFFFF1F1F)
    val cardColor = Color(0xFFFDFDFD)
    val labelColor = Color(0xFF7A766F)
    val textColor = Color(0xFF1F1F1F)
    val placeholderColor = Color(0xFF9A948C)
    val indicatorColor = if (isActive) accentRed else Color(0xFFD8D2CB)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentRed,
                        shape = RoundedCornerShape(999.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (value.isBlank()) placeholder else value,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = if (value.isBlank()) placeholderColor else textColor,
                            fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentRed.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            color = indicatorColor,
                            shape = RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun EventTextArea(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val accentRed = Color(0xFFFF1F1F)
    val cardColor = Color(0xFFFDFDFD)
    val labelColor = Color(0xFF7A766F)
    val textColor = Color(0xFF1F1F1F)
    val placeholderColor = Color(0xFF9A948C)
    val indicatorColor = if (isActive) accentRed else Color(0xFFD8D2CB)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentRed,
                        shape = RoundedCornerShape(999.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = false,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = textColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    decorationBox = { innerTextField ->
                        if (value.isBlank()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = placeholderColor
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            color = indicatorColor,
                            shape = RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    }
}