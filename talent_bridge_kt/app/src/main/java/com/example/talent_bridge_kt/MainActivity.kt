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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.example.talent_bridge_kt.presentation.ui.components.OfflineConnectionDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.talent_bridge_kt.presentation.ui.components.HomeWithDrawer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import com.example.talent_bridge_kt.data.AnalyticsManager
import com.example.talent_bridge_kt.data.repository.ProfileRepository
import com.example.talent_bridge_kt.presentation.ui.screens.CreateProjectPopUp
import com.google.firebase.auth.FirebaseAuth
import com.example.talent_bridge_kt.ConnectivityViewModel
import com.example.talent_bridge_kt.ConnectivityViewModelFactory
import kotlin.system.measureTimeMillis
import androidx.room.Room
import com.example.talent_bridge_kt.data.local.AppDatabase






class MainActivity : ComponentActivity() {

    private val viewmodel by viewModels<ConnectivityViewModel> {
        ConnectivityViewModelFactory(applicationContext)
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

                val context = LocalContext.current
                val projectsVm: com.example.talent_bridge_kt.presentation.ui.viewmodel.ProjectsViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                val app = context.applicationContext as android.app.Application
                                return com.example.talent_bridge_kt.presentation.ui.viewmodel.ProjectsViewModel(app) as T
                            }
                        }
                    )

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
                        // El ProjectsViewModel ya sincroniza automáticamente las aplicaciones pendientes
                        // cuando detecta que se reconectó a internet (en su init)
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

                // Show offline connection dialog when connection is lost
                // But NOT when in StudentProfile screen (it has its own custom dialog)
                if (showNoNetDialog && currentRoute != Routes.StudentProfile) {
                    OfflineConnectionDialog(
                        onDismiss = { showNoNetDialog = false }
                    )
                }

                Scaffold(Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snack) }
                ) { inner ->

                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    val startDest = if (firebaseUser != null) {
                        Routes.StudentFeed
                    } else {
                        Routes.Login
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDest,
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
                                    onOpenDrawer = { openDrawer() },
                                    onAddProject = { navController.navigate("createProject") }
                                )
                            }
                        }

                        dialog("createProject") {
                            CreateProjectPopUp(
                                onDismiss = { navController.popBackStack() },
                                { draft ->
                                    // 2) aquí usamos el VM que obtuvimos arriba
                                    projectsVm.createProject(
                                        title = draft.title,
                                        description = draft.description,
                                        skills = draft.skills,
                                        imageUri = draft.imageUri
                                    ) { ok, err ->
                                        if (ok) {
                                            println(" Proyecto creado con éxito")
                                            navController.popBackStack()
                                        } else {
                                            println(" Error creando proyecto: ${err?.message}")
                                        }
                                    }
                                }
                            )
                        }

                        composable(Routes.LeaderFeed) {
                            HomeWithDrawer(navController = navController) { openDrawer ->
                                LeaderFeedScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenDrawer = { openDrawer() },
                                    onStudentClick = { uid ->
                                        navController.navigate(Routes.someoneElse(uid))
                                    },
                                    onInitiativeProfile = {navController.navigate(Routes.InitiativeProfile)}

                                )
                            }
                        }
                        composable(Routes.SavedProjects) {
                            HomeWithDrawer(navController = navController) { openDrawer ->
                                SavedProjectsScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenDrawer = { openDrawer() },
                                    onHome = { navController.navigate(Routes.StudentFeed) },
                                    onSearch = { navController.navigate(Routes.Search) },
                                    onExploreStudents = { navController.navigate(Routes.LeaderFeed) },
                                    onFav = { navController.navigate(Routes.SavedProjects) }
                                )
                            }
                        }

                        composable(Routes.Search) {
                            val context = LocalContext.current
                            
                            // Crear instancia de la base de datos y obtener el DAO
                            val db = remember {
                                Room.databaseBuilder(
                                    context.applicationContext,
                                    AppDatabase::class.java,
                                    "projects_db"
                                )
                                    .fallbackToDestructiveMigration()
                                    .build()
                            }
                            val feedStudentDao = remember { db.feedStudentDao() }

                            val repo = FirestoreSearchRepository(FirebaseFirestore.getInstance())
                            val connectivityVm = viewmodel

                            val vm: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                factory = SearchViewModelFactory(repo, connectivityVm, feedStudentDao)
                            )
                            SearchScreen(
                                vm = vm,
                                onBack = { navController.popBackStack() },
                                onSearch = { navController.navigate(Routes.Search) },
                                onProfile = { navController.navigate(Routes.StudentProfile) },
                                onHome = { navController.navigate(Routes.StudentFeed) },
                                onFav = { navController.navigate(Routes.SavedProjects) },
                                onStudentClick = { uid ->
                                    navController.navigate(Routes.someoneElse(uid))
                                }
                            )
                        }


                        composable(Routes.StudentProfile) {
                            HomeWithDrawer(navController = navController) { openDrawer ->
                                StudentProfileScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenDrawer = { openDrawer() },
                                    onAddProject = { navController.navigate("createProject") },
                                    onHome = { navController.navigate(Routes.StudentFeed) },
                                    onSearch = { navController.navigate(Routes.Search) },
                                    onExploreStudents = { navController.navigate(Routes.LeaderFeed) },
                                    onFav = { navController.navigate(Routes.SavedProjects) }
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
                                    onSomeOneElseProfile = { uid -> navController.navigate(Routes.someoneElse(uid)) },
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
