package com.example.phinui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.compose.rememberNavController
import com.example.phinui.ui.components.CustomBottomBar
import com.example.phinui.ui.navigation.PhinNavHost
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.PhinUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhinUITheme {
                PhinUIApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun PhinUIApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        bottomBar = {
            CustomBottomBar(navController = navController)
        }
    ) { innerPadding ->
        PhinNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PhinUIAppPreview() {
    PhinUITheme {
        PhinUIApp()
    }
}