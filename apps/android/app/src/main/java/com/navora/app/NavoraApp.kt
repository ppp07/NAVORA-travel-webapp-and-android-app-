package com.navora.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.navora.app.navigation.NavoraDestination
import com.navora.app.navigation.NavoraNavHost
import com.navora.app.ui.components.NavoraBottomBar
import com.navora.app.ui.theme.NavoraCream

@Composable
fun NavoraApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = NavoraDestination.bottomBarDestinations.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NavoraCream,
        bottomBar = {
            if (showBottomBar) {
                NavoraBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(NavoraDestination.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavoraNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
