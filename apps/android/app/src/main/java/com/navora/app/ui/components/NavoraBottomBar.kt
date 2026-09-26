package com.navora.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardTravel
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.navora.app.navigation.NavoraDestination
import com.navora.app.ui.theme.NavoraBlue
import com.navora.app.ui.theme.NavoraCard
import com.navora.app.ui.theme.NavoraLine
import com.navora.app.ui.theme.NavoraMuted
import com.navora.app.ui.theme.NavoraNavy

@Composable
fun NavoraBottomBar(
    currentRoute: String?,
    onNavigate: (NavoraDestination) -> Unit,
) {
    NavigationBar(
        containerColor = NavoraCard,
        modifier = Modifier.border(width = 1.dp, color = NavoraLine),
    ) {
        NavoraDestination.bottomBarDestinations.forEach { destination ->
            val label = destination.barLabel()
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination) },
                icon = { Icon(destination.barIcon(), contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavoraNavy,
                    selectedTextColor = NavoraNavy,
                    indicatorColor = NavoraBlue,
                    unselectedIconColor = NavoraMuted,
                    unselectedTextColor = NavoraMuted,
                ),
            )
        }
    }
}

private fun NavoraDestination.barLabel(): String = when (this) {
    NavoraDestination.Home -> "Home"
    NavoraDestination.Trips -> "Trips"
    NavoraDestination.Explore -> "Explore"
    NavoraDestination.Profile -> "Profile"
    else -> ""
}

private fun NavoraDestination.barIcon(): ImageVector = when (this) {
    NavoraDestination.Trips -> Icons.Outlined.CardTravel
    NavoraDestination.Explore -> Icons.Outlined.Explore
    NavoraDestination.Profile -> Icons.Outlined.Person
    else -> Icons.Outlined.Home
}
