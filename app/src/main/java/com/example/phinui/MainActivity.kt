package com.example.phinui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.phinui.components.SideMenu
import com.example.phinui.notifications.NotificationPermissionRequest
import com.example.phinui.ui.components.CustomBottomBar
import com.example.phinui.ui.navigation.PhinNavHost
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.HeaderRed
import com.example.phinui.ui.theme.HeaderText
import com.example.phinui.ui.theme.PhinUITheme
import kotlinx.coroutines.launch
import com.example.phinui.ui.navigation.Routes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun PhinUIApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route

    var topBarTitle by remember { mutableStateOf("") }
    var isMessagesScreen by remember { mutableStateOf(false) }

    val hideNavigationUi =
        currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER

    LaunchedEffect(currentRoute) {
        drawerState.close()

        if (currentRoute != Routes.MESSAGES) {
            topBarTitle = ""
            isMessagesScreen = false
        }
    }

    NotificationPermissionRequest()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            if (!hideNavigationUi) {
                ModalDrawerSheet(
                    drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                    drawerContainerColor = HeaderRed,
                    drawerContentColor = HeaderText
                ) {
                    SideMenu(
                        navController = navController,
                        onItemClick = { scope.launch { drawerState.close() } }
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!hideNavigationUi) {
                    TopAppBar(
                        modifier = Modifier.height(65.dp),
                        title = {
                            Box (
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    topBarTitle,
                                    fontSize = if (isMessagesScreen) 18.sp
                                        else MaterialTheme.typography.titleLarge.fontSize,
                                    fontWeight = if (isMessagesScreen) FontWeight.Bold
                                        else FontWeight.Normal,
                                            color = HeaderText
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Background
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { navController.popBackStack() }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Background
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderRed)
                    )
                }
            },
            containerColor = Background,
            bottomBar = {
                if (!hideNavigationUi) {
                    CustomBottomBar(navController = navController)
                }
            }
        ) { innerPadding ->
            PhinNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                setTopBarTitle = { title, messagesScreen ->
                    topBarTitle = title
                    isMessagesScreen = messagesScreen
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PhinUIAppPreview() {
    PhinUITheme {
        PhinUIApp()
    }
}