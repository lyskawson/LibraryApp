package com.example.libraryapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState


data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: AppScreenRoute
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        route = HomeRoute
    ),
    BottomNavItem(
        label = "My Library",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List,
        route = MyLibraryRoute
    )
)

@Composable
fun AppBottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,

    ) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination


    NavigationBar(modifier = modifier) {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { navDest ->
                navDest.route?.let { routeName ->
                    routeName == item.route::class.qualifiedName
                } ?: false
            } == true


            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                alwaysShowLabel = true
            )
        }
    }
}