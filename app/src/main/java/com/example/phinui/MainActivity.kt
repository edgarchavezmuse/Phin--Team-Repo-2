package com.example.phinui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.phinui.components.SideMenu
import com.example.phinui.notifications.NotificationPermissionRequest
import com.example.phinui.ui.components.AnimatedWaves
import com.example.phinui.ui.components.ChosenBottomBar
import com.example.phinui.ui.components.rememberWaveAnimationState
import com.example.phinui.ui.navigation.PhinNavHost
import com.example.phinui.ui.navigation.Routes
import com.example.phinui.ui.theme.PhinUITheme
import com.example.phinui.viewmodel.MainActivityViewModel
import com.example.phinui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val context = LocalContext.current
            val settingsViewModel = remember { SettingsViewModel(context) }

            // dark mode state
            val darkModeEnabled by settingsViewModel.darkModeEnabled.collectAsState()

            PhinUITheme(darkMode = darkModeEnabled) {
                PhinUIApp(settingsViewModel, darkModeEnabled)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
//@PreviewScreenSizes
@Composable
fun PhinUIApp(
    settingsViewModel: SettingsViewModel,
    darkModeEnabled: Boolean
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route
    val mainActivityViewModel: MainActivityViewModel = viewModel()

    val topBarTitle = mainActivityViewModel.topBarTitle
    val isMessages = mainActivityViewModel.isMessagesScreen

    val hideNavigationUi =
        currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER
    val isInMessages = currentRoute?.startsWith(Routes.MESSAGES) == true

    val showAuthWaves = true // turns waves and ship on and off in Login and Register screens
    val waveAnimation = rememberWaveAnimationState(
        frontDurationMillis = 4200, // speed of wave
        backDurationMillis = 3500, // speed of wave
        shipSpeedDpPerSecond = 43f, // speed of ship
        bobDurationMillis = 2000
    )

    val bottomBarType by settingsViewModel.bottomBarType.collectAsState()

    LaunchedEffect(currentRoute) {
        drawerState.close()

        if (!isInMessages) {
            mainActivityViewModel.setTitle("", false)
        }
    }

    NotificationPermissionRequest()

    if (hideNavigationUi) {
        PhinUITheme(darkMode = false) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (showAuthWaves) {
                        AnimatedWaves(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            waveColor = MaterialTheme.colorScheme.primary,
                            height = 110.dp,
                            showShip = true,
                            shipSize = 20.dp,
                            shipVerticalOffset = 18f,
                            frontPhase = waveAnimation.frontPhase,
                            backPhase = waveAnimation.backPhase,
                            shipProgress = waveAnimation.shipProgress,
                            bobPhase = waveAnimation.bobPhase
                        )
                    }

                    PhinNavHost(
                        navController = navController,
                        modifier = Modifier.fillMaxSize(),
                        darkModeEnabled = darkModeEnabled,
                        setTopBarTitle = { title, isMessages ->
                            mainActivityViewModel.setTitle(title, isMessages)
                        }
                    )
                }
            }
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet(
                    drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.primary,
                    drawerContentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    SideMenu(
                        navController = navController,
                        onItemClick = { scope.launch { drawerState.close() } }
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        modifier = Modifier.height(65.dp),
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    topBarTitle,
                                    fontSize = if (isMessages) 18.sp
                                    else MaterialTheme.typography.titleLarge.fontSize,
                                    fontWeight = if (isMessages) FontWeight.Bold
                                    else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onPrimary
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
                                    tint = MaterialTheme.colorScheme.onPrimary
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
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                bottomBar = {
                    when (bottomBarType) {

                        null -> {
                            // empty spacer equal to bottom bar height to prevent jump
                            Spacer(modifier = Modifier.height(80.dp))
                        }

                        else -> {
                            ChosenBottomBar(
                                navController,
                                waveAnimation,
                                bottomBarType!!
                            )
                        }
                    }
                }
            ) { innerPadding ->
                PhinNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                    darkModeEnabled = darkModeEnabled,
                    setTopBarTitle = { title, isMessages ->
                        mainActivityViewModel.setTitle(title, isMessages)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PhinUIAppPreview() {
    val context = LocalContext.current
    val settingsViewModel = remember { SettingsViewModel(context) }

    PhinUIApp(
        settingsViewModel = settingsViewModel,
        darkModeEnabled = false
    )
}