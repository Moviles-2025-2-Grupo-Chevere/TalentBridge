package com.example.talent_bridge_kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.talent_bridge_kt.core.navegation.Routes
import com.example.talent_bridge_kt.presentation.ui.screens.CreateAccountScreen
import com.example.talent_bridge_kt.presentation.ui.screens.InitiativeProfileScreen
import com.example.talent_bridge_kt.presentation.ui.screens.LeaderFeedScreen
import com.example.talent_bridge_kt.presentation.ui.screens.LoginScreen
import com.example.talent_bridge_kt.presentation.ui.screens.SavedProjectsScreen
import com.example.talent_bridge_kt.presentation.ui.screens.SearchScreen
import com.example.talent_bridge_kt.presentation.ui.screens.StudentFeedScreen
import com.example.talent_bridge_kt.presentation.ui.screens.StudentProfileScreen
import com.example.talent_bridge_kt.ui.theme.Talent_bridge_ktTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Talent_bridge_ktTheme {
                val navController = rememberNavController()
                Scaffold(Modifier.fillMaxSize()) { inner ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Login,
                        modifier = Modifier.padding(inner)
                    ) {
                        composable(Routes.Login) {
                            LoginScreen(
                                onCreateAccount = { navController.navigate(Routes.CreateAccount) } ,
                                onStudentFeed = { navController.navigate(Routes.StudentFeed) }
                            )
                        }
                        composable(Routes.CreateAccount) {
                            CreateAccountScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.StudentFeed) {
                            StudentFeedScreen(
                                onBack = { navController.popBackStack() } ,
                                onInitiativeProfile = { navController.navigate(Routes.InitiativeProfile) },
                                onLeaderFeed = { navController.navigate(Routes.LeaderFeed) },
                                onSavedProjects = { navController.navigate(Routes.SavedProjects) },
                                onSearch = { navController.navigate(Routes.Search) },
                                onStudentProfile = { navController.navigate(Routes.StudentProfile) },
                                )
                        }
                        composable(Routes.InitiativeProfile) {
                            InitiativeProfileScreen(
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
                            SearchScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.StudentProfile) {
                            StudentProfileScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                    }
                }
            }
        }
    }
}
