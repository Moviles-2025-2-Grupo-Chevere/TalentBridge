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
                        composable(Routes.Navegation) {
                            NavegationScreen(
                                onBack = { navController.popBackStack() },
                                onInitiativeProfile = { navController.navigate(Routes.InitiativeProfile) },
                                onLeaderFeed = { navController.navigate(Routes.LeaderFeed) },
                                onSavedProjects = { navController.navigate(Routes.SavedProjects) },
                                onSearch = { navController.navigate(Routes.Search) },
                                onStudentProfile = { navController.navigate(Routes.StudentProfile) },
                                onSomeoneElseProfile = { navController.navigate(Routes.SomeOneElseProfile) },
                                onCredits = { navController.navigate(Routes.Credits) },
                                onStudentFeed = { navController.navigate(Routes.StudentFeed) },
                                onInitiativeDetail = { navController.navigate(Routes.InitiativeDetail) }
                            )
                        }
                        composable(Routes.InitiativeProfile) {
                            InitiativeProfileSceen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.LeaderFeed) {
                            LeaderFeedScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.SavedProjects) {
                            SavedProjectsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.Search) {
                            val repo = FirestoreSearchRepository(FirebaseFirestore.getInstance())
                            val vm: SearchViewModel = viewModel(
                                factory = SearchViewModelFactory(repo)
                            )
                            SearchScreen(
                                vm = vm,
                                onBack = { navController.popBackStack() },
                                onSearch = { navController.navigate(Routes.Search)},
                                onProfile = { navController.navigate(Routes.StudentProfile)},
                                onHome = { navController.navigate(Routes.StudentFeed)},

                                )
                        }

                        composable(Routes.StudentProfile) {
                            StudentProfileScreen(
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable(Routes.SomeOneElseProfile) {
                            SomeElseProfileScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.Credits) {
                            CreditsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.StudentFeed) {
                            StudentFeedScreen(
                                onBack = { navController.popBackStack() },
                                onSomeOneElseProfile = { navController.navigate(Routes.SomeOneElseProfile) },
                                onExploreStudents = { navController.navigate(Routes.LeaderFeed) },
                                onSearch = { navController.navigate(Routes.Search)},
                                onProfile = { navController.navigate(Routes.StudentProfile)},
                            )
                        }
                        composable(Routes.InitiativeDetail) {
                            InitiativeDetailScreen(
                                onBack = { navController.popBackStack() }
                            )
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
            }
        }
    }
}
