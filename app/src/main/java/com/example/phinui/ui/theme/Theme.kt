package com.example.phinui.ui.theme

//import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = HeaderRed,
    secondary = NavText,
    tertiary = SelectedPill,
    background = Background,
    surface = Background,
    onPrimary = HeaderText,
    onSecondary = DarkTextMuted,
    onTertiary = NavText,
    onBackground = DeletedMessageColor,
    onSurface = NavText,
    surfaceVariant = LightPink,
    primaryContainer = DarkHeaderRed,
    tertiaryContainer = SenderUserColor
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkHeaderRed,
    secondary = DarkTextSecondary,
    tertiary = DarkSelectedPill,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = TextMuted,
    onTertiary = DarkTextPrimary,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkLightPink,
    primaryContainer = DarkHeaderRed,
    tertiaryContainer = SenderUserColor
)

@Composable
fun PhinUITheme(
    darkMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkMode) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}