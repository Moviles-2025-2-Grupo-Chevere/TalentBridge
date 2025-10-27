package com.example.talent_bridge_kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.talent_bridge_kt.core.conectivity.AndroidConnectivityObserver

import com.example.talent_bridge_kt.core.navegation.Routes
import com.example.talent_bridge_kt.presentation.ui.screens.CreateAccountScreen
import com.example.talent_bridge_kt.presentation.ui.screens.InitiativeProfileSceen
import com.example.talent_bridge_kt.presentation.ui.screens.LeaderFeedScreen
import com.example.talent_bridge_kt.presentation.ui.screens.LoginScreen
import com.example.talent_bridge_kt.presentation.ui.screens.SavedProjectsScreen
import com.example.talent_bridge_kt.presentation.ui.screens.SearchScreen
import com.example.talent_bridge_kt.presentation.ui.screens.StudentFeedScreen
import com.example.talent_bridge_kt.presentation.ui.screens.StudentProfileScreen
import com.example.talent_bridge_kt.presentation.ui.screens.SomeElseProfileScreen
import com.example.talent_bridge_kt.presentation.ui.screens.CreditsScreen
import com.example.talent_bridge_kt.presentation.ui.screens.NavegationScreen
import com.example.talent_bridge_kt.presentation.ui.screens.InitiativeDetailScreen
import com.example.talent_bridge_kt.ui.theme.Talent_bridge_ktTheme


import com.google.firebase.firestore.FirebaseFirestore
import com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository
import com.example.talent_bridge_kt.presentation.ui.screens.SearchViewModel
import com.example.talent_bridge_kt.presentation.ui.screens.SearchViewModelFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.initializer
import com.example.talent_bridge_kt.presentation.ui.components.HomeWithDrawer
import androidx.compose.runtime.*
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.talent_bridge_kt.data.AnalyticsManager
import com.example.talent_bridge_kt.data.repository.ProfileRepository
import kotlin.system.measureTimeMillis




class MainActivity : ComponentActivity() {

    private val viewmodel by viewModels<ConnectivityViewModel> {
        viewModelFactory {
            initializer {
                ConnectivityViewModel(
                    connectivityObserver = AndroidConnectivityObserver(
                        context = applicationContext
                    )
                )
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Talent_bridge_ktTheme {
                val isConnected by viewmodel.isConnected.collectAsState()
                // Snackbar + popup control
                val snack = remember { SnackbarHostState() }
                var showNoNetDialog by remember { mutableStateOf(false) }

                LaunchedEffect(isConnected) {
                    if (!isConnected) {
                        showNoNetDialog = true
                        snack.showSnackbar(
                            message = "Sin conexión a Internet",
                            withDismissAction = true,
                            duration = SnackbarDuration.Indefinite
                        )
                    } else {
                        snack.currentSnackbarData?.dismiss()
                        showNoNetDialog = false
                    }
                }

                val navController = rememberNavController()

                var currentRoute by remember { mutableStateOf<String?>(null) }
                var screenStartMs by remember { mutableStateOf(0L) }

                val backStackEntry by navController.currentBackStackEntryAsState()

                LaunchedEffect(backStackEntry) {
                    val newRoute = backStackEntry?.destination?.route ?: return@LaunchedEffect
                    val now = System.currentTimeMillis()

                    currentRoute?.let { prev ->
                        val duration = now - screenStartMs
                        if (duration > 0) {
                            AnalyticsManager.logScreenDuration(prev, duration)
                        }
                    }

                    currentRoute = newRoute
                    screenStartMs = now

                    AnalyticsManager.logScreenView(newRoute)
                }

                Scaffold(Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snack) }
                ) { inner ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Login,
                        modifier = Modifier.padding(inner)
                    ) {
                        composable(Routes.Login) {
                            LoginScreen(
                                onCreateAccount = { navController.navigate(Routes.CreateAccount) },
                                onStudentFeed = { navController.navigate(Routes.StudentFeed) },
                            )
                        }
                        composable(Routes.CreateAccount) {
                            CreateAccountScreen(
                                onBack = { navController.popBackStack() },
                            )
                        }

                        composable(Routes.InitiativeProfile) {
                            HomeWithDrawer(navController = navController) { openDrawer ->
                                InitiativeProfileSceen(
                                    onBack = { navController.popBackStack() },
                                    onOpenDrawer = { openDrawer() }
                                )
                            }
                        }
                        composable(Routes.LeaderFeed) {
                            HomeWithDrawer(navController = navController) { openDrawer ->
                                LeaderFeedScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenDrawer = { openDrawer() },
                                    onStudentClick = { uid ->
                                        navController.navigate(Routes.someoneElse(uid))
                                    }
                                )
                            }
                        }
                        composable(Routes.SavedProjects) {
                            HomeWithDrawer(navController = navController) { openDrawer ->
                                SavedProjectsScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenDrawer = { openDrawer() }
                                )
                            }
                        }

                        composable(Routes.Search) {

                            val repo = FirestoreSearchRepository(FirebaseFirestore.getInstance())
                            val vm: SearchViewModel = viewModel(
                                factory = SearchViewModelFactory(repo)
                            )
                                SearchScreen(
                                    vm = vm,
                                    onBack = { navController.popBackStack() },
                                    onSearch = { navController.navigate(Routes.Search) },
                                    onProfile = { navController.navigate(Routes.StudentProfile) },
                                    onHome = { navController.navigate(Routes.StudentFeed) },
                                    onStudentClick = { uid ->
                                        navController.navigate(Routes.someoneElse(uid))
                                    }


                                )
                            }


                        composable(Routes.StudentProfile) {
                            HomeWithDrawer(navController = navController) { openDrawer ->
                                StudentProfileScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenDrawer = { openDrawer() }
                                )
                            }
                        }
                        composable(Routes.SomeOneElseProfile) { backStack ->
                            val uid = backStack.arguments?.getString("uid") ?: return@composable
                            HomeWithDrawer(navController = navController) { openDrawer ->
                                val repo = remember { ProfileRepository(FirebaseFirestore.getInstance()) }
                                SomeElseProfileScreen(
                                    uid = uid,
                                    repo = repo,
                                    onBack = { navController.popBackStack() },
                                    onOpenDrawer = { openDrawer() }
                                )
                            }
                        }
                        composable(Routes.Credits) {
                            CreditsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.StudentFeed) {
                            HomeWithDrawer(navController = navController) { openDrawer ->
                                StudentFeedScreen(
                                    onBack = { navController.popBackStack() },
                                    onExploreStudents = { navController.navigate(Routes.LeaderFeed) },
                                    onSearch = { navController.navigate(Routes.Search) },
                                    onProfile = { navController.navigate(Routes.StudentProfile) },

                                    onOpenDrawer = { openDrawer() },
                                    onFav = {  navController.navigate(Routes.SavedProjects) }
                                )
                            }
                        }
                        composable(Routes.InitiativeDetail) {
                            HomeWithDrawer(navController = navController) { openDrawer ->
                                InitiativeDetailScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenDrawer = { openDrawer() }
                                )
                            }
                        }
                    }
                }
                if (showNoNetDialog) {
                    AlertDialog(
                        onDismissRequest = { showNoNetDialog = false }, // permite cerrarlo
                        title = { Text("Sin conexión a Internet") },
                        text = { Text("No estás conectado a internet") },
                        confirmButton = {
                            TextButton(onClick = { showNoNetDialog = false }) {
                                Text("Entendido")
                            }
                        }
                    )
                }
                DisposableEffect(Unit) {
                    onDispose {
                        val now = System.currentTimeMillis()
                        currentRoute?.let { route ->
                            val duration = now - screenStartMs
                            if (duration > 0) {
                                AnalyticsManager.logScreenDuration(route, duration)
                            }
                        }
                    }
                }


            }
        }
    }
}
