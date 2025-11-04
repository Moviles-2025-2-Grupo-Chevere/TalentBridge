@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.talent_bridge_kt.presentation.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.example.talent_bridge_kt.data.AuthManager
import com.example.talent_bridge_kt.core.navegation.Routes
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

@Composable
fun HomeWithDrawer(
    navController: NavController,
    content: @Composable (openDrawer: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menu", modifier = androidx.compose.ui.Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium)

                NavigationDrawerItem(
                    label = { Text("Credits") },
                    selected = false,
                    icon = { Icon(Icons.Filled.Info, null) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.Credits)
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Log out") },
                    selected = false,
                    icon = { Icon(Icons.Filled.ExitToApp, null) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        AuthManager.signOut()
                        navController.navigate(Routes.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { content(openDrawer) }
}


