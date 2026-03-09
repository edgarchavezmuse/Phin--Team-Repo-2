package com.example.phinui.ui.theme

//import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
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
    onSecondary = HeaderText,
    onTertiary = NavText,
    onBackground = NavText,
    onSurface = NavText
)

@Composable
fun PhinUITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}