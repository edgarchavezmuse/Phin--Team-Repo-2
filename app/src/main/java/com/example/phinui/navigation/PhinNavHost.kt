package com.example.phinui.ui.navigation

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.screens.CalendarScreen
import com.example.phinui.ui.screens.AddEventScreen
import com.example.phinui.ui.screens.EventsScreen
import com.example.phinui.ui.screens.MessagesScreen
import com.example.phinui.ui.screens.HomeScreen
import com.example.phinui.ui.screens.ProfileScreen
import kotlinx.coroutines.launch
import com.example.phinui.ui.screens.MapScreen
import com.example.phinui.notifications.ReminderScheduler
import com.example.phinui.viewmodel.CalendarViewModel
import com.example.phinui.viewmodel.CalendarViewModelFactory
import com.example.phinui.viewmodel.AddEventResult
import com.example.phinui.ui.screens.ScheduleScreen
import com.example.phinui.data.calendar.CalendarSource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.phinui.data.authorization.GoogleAuthManager
import com.example.phinui.notifications.NotificationHelper.createNotificationChannels
import com.example.phinui.screens.UserListScreen
import com.example.phinui.ui.screens.LoginScreen
import com.example.phinui.ui.screens.RegisterScreen
import com.example.phinui.viewmodel.EventsRepository
import com.example.phinui.viewmodel.EventsViewModel
import com.example.phinui.viewmodel.EventsViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.example.phinui.ui.screens.FriendsScreen
import com.example.phinui.ui.screens.PeopleScreen
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.phinui.screens.SettingsScreen

@Composable
fun PhinNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    setTopBarTitle: (String, Boolean) -> Unit
) {
    // variables for ensuring events get passed to calendar
    val context = LocalContext.current
    val savedEvents = remember { mutableStateListOf<CalendarEvent>() }
    val allEvents = remember { mutableStateListOf<CalendarEvent>() }
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUserId by remember { mutableStateOf(auth.currentUser?.uid) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUserId = firebaseAuth.currentUser?.uid
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    val startDestination = if (currentUserId != null) Routes.HOME else Routes.LOGIN

    createNotificationChannels(context)


    val activity = context as ComponentActivity

    val eventFactory = remember {
        EventsViewModelFactory(
            repository = EventsRepository()
        )
    }

    val eventsViewModel: EventsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = eventFactory
    )

    val reminderScheduler = remember {
        ReminderScheduler(context.applicationContext)
    }

    val calendarFactory = remember {
        CalendarViewModelFactory(
            context = context.applicationContext,
            reminderScheduler = reminderScheduler
        )
    }

    val calendarViewModel: CalendarViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = calendarFactory
    )

    val isLoading by eventsViewModel.isLoading.collectAsState()
    val schoolEvents by eventsViewModel.events.collectAsState()

    val authorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val tokenFromResult = GoogleAuthManager.handleAuthorizationResult(activity, result)
        if (tokenFromResult.isNullOrBlank()) {
            calendarViewModel.onGoogleSessionRestoreFailed("Authorization canceled or failed.")
        } else {
            calendarViewModel.onAuthorizationSuccess(tokenFromResult)
        }
    }

    LaunchedEffect(currentUserId) {
        savedEvents.clear()
        allEvents.clear()
        calendarViewModel.clearInMemoryState()
        calendarViewModel.restoreConnectionFromFirebase()
        calendarViewModel.refreshEvents()
    }

    LaunchedEffect(calendarViewModel.eventsGroupedByDate) {
        val localEvents = calendarViewModel.eventsGroupedByDate
            .values
            .flatten()
            .filter { it.source == CalendarSource.LOCAL }

        savedEvents.clear()
        savedEvents.addAll(localEvents)
    }

    LaunchedEffect(
        calendarViewModel.isGoogleCalendarConnected,
        calendarViewModel.googleAccessToken,
        calendarViewModel.isRestoringGoogleSession
    ) {
        if (
            calendarViewModel.isGoogleCalendarConnected &&
            calendarViewModel.googleAccessToken == null &&
            !calendarViewModel.isRestoringGoogleSession
        ) {
            calendarViewModel.beginGoogleSessionRestore()

            GoogleAuthManager.startAuthorization(
                activity = activity,
                launcher = authorizationLauncher,
                onAccessToken = { token ->
                    calendarViewModel.onAuthorizationSuccess(token)
                },
                onError = { exception ->
                    calendarViewModel.onGoogleSessionRestoreFailed(
                        exception.message ?: "Failed to restore Google Calendar session."
                    )
                }
            )
        }
    }

    NavHost(
        navController = navController,
        //startDestination = Routes.HOME,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenRegister = {
                    navController.navigate(Routes.REGISTER) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenLogin = {
                    navController.popBackStack()
                }
            )
        }

        // navigate to x screen
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                events = schoolEvents,
                isLoading = isLoading
            )
        }

        composable(
            route = Routes.MESSAGES + "/{receiverID}",
            arguments = listOf(
                navArgument("receiverID") {
                    type = NavType.StringType
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "phin://messages/{receiverID}"
                }
            )
        ) { backStackEntry ->
            val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val receiverID = backStackEntry.arguments?.getString("receiverID") ?: ""

            MessagesScreen(
                senderUserID = currentUserID,
                receiverUserID = receiverID,
                setTopBarTitle = { title -> setTopBarTitle(title, true)}
            )
        }

        composable(
            route = "${Routes.USERLIST}?tab={tab}",
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "phin://userlist?tab={tab}"
                }
            )
        ) { backStackEntry ->

            val tab = backStackEntry.arguments?.getInt("tab") ?: 0

            UserListScreen(
                navController = navController,
                initialTab = tab
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(Routes.PEOPLE) {
            PeopleScreen()
        }

        composable(
            route = "${Routes.FRIENDS}?tab={tab}",
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "phin://friends?tab={tab}"
                }
            )
        ) { backStackEntry ->

            val tab = backStackEntry.arguments?.getInt("tab") ?: 0

            FriendsScreen(
                navController = navController,
                initialTab = tab
                )
        }

        composable(Routes.SCHEDULE) {
            ScheduleScreen()
        }

        composable(Routes.EVENTS) {

            EventsScreen(
                //events = allEvents,
                events = schoolEvents,
                onEventClick = { event ->
                    val allCalendarEvents = calendarViewModel.eventsGroupedByDate.values.flatten()
                    val googleEvents = allCalendarEvents.filter { it.source == CalendarSource.GOOGLE }
                    val localEvents = allCalendarEvents.filter { it.source == CalendarSource.LOCAL }

                    val existsLocally = localEvents.any { sameCalendarEvent(it, event) }
                    val existsInGoogle = googleEvents.any { sameCalendarEvent(it, event) }

                    if (existsInGoogle) {
                        Toast.makeText(
                            context,
                            "${event.title} already in your Google Calendar.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (existsLocally) {
                        Toast.makeText(
                            context,
                            "${event.title} already in your local calendar.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        coroutineScope.launch {
                            try {
                                when (val result = calendarViewModel.addEventToAppropriateCalendar(event)) {
                                    is AddEventResult.ShouldSaveLocally -> {
                                        val localEvent = event.copy(source = CalendarSource.LOCAL)

                                        calendarViewModel.saveLocalEventToFirebase(localEvent)

                                        Toast.makeText(
                                            context,
                                            "${event.title} added to your local calendar.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    is AddEventResult.AddedToGoogle -> {
                                        Toast.makeText(
                                            context,
                                            "${event.title} added to your Google Calendar.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    e.message ?: "Failed to add event.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            )
        }

        composable(
            Routes.CALENDAR,
            deepLinks = listOf(
                navDeepLink {
                    // deep link allows notification to navigate to this screen
                    uriPattern = "phin://calendar"
                }
            )
        ) {

            val selectedEvent = remember { mutableStateOf<CalendarEvent?>(null) }
            val showRemoveDialog = remember { mutableStateOf(false) }
            CalendarScreen(
                savedEvents = savedEvents,
                calendarViewModel = calendarViewModel,
                onClick = { event ->
                    selectedEvent.value = event
                    showRemoveDialog.value = true
                },
                onAddEventClick = {
                    navController.navigate(Routes.ADD_EVENT)
                },
                onConnectClick = {
                    calendarViewModel.setError(null)

                    val credentialManager = CredentialManager.create(activity)

                    coroutineScope.launch {
                        try {
                            credentialManager.clearCredentialState(
                                ClearCredentialStateRequest()
                            )
                        } catch (_: Exception) {
                        }

                        GoogleAuthManager.startAuthorization(
                            activity = activity,
                            launcher = authorizationLauncher,
                            onAccessToken = { token ->
                                calendarViewModel.onAuthorizationSuccess(token)
                            },
                            onError = { exception ->
                                calendarViewModel.onGoogleSessionRestoreFailed(
                                    exception.message ?: "Authorization error."
                                )
                            }
                        )
                    }
                },
                selectedEvent = selectedEvent,
                showRemoveDialog = showRemoveDialog,
                reminderScheduler = reminderScheduler
            )
        }

        composable(Routes.ADD_EVENT) {

            AddEventScreen(
                onSaveEvent = { newEvent ->
                    coroutineScope.launch {
                        try {
                            when (val result = calendarViewModel.addEventToAppropriateCalendar(newEvent)) {
                                is AddEventResult.ShouldSaveLocally -> {
                                    if (allEvents.none { it.title == newEvent.title && it.start == newEvent.start }) {
                                        allEvents.add(newEvent)
                                    }

                                    val localEvent = newEvent.copy(source = CalendarSource.LOCAL)

                                    calendarViewModel.saveLocalEventToFirebase(localEvent)

                                    Toast.makeText(
                                        context,
                                        "${newEvent.title} added to your local calendar.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.popBackStack()
                                }

                                is AddEventResult.AddedToGoogle -> {

                                    Toast.makeText(
                                        context,
                                        "${newEvent.title} added to your Google Calendar.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.popBackStack()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                e.message ?: "Failed to save event.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MAP) {
            MapScreen()
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(navController)
        }
    }
}

// Checks if two events are the same (based on title + start time)
private fun sameCalendarEvent(a: CalendarEvent, b: CalendarEvent): Boolean {
    return a.title.trim().equals(b.title.trim(), ignoreCase = true) &&
            a.start.take(16) == b.start.take(16)
}