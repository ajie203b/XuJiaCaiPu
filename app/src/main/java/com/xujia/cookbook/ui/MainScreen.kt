package com.xujia.cookbook.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.xujia.cookbook.data.AppDatabase
import com.xujia.cookbook.data.DishRepository
import com.xujia.cookbook.ui.screens.*

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "首页")
    object Tips : BottomNavItem("tips", Icons.Filled.Lightbulb, "小技巧")
    object Recommend : BottomNavItem("recommend", Icons.Filled.Recommend, "为你推荐")
    object Favorites : BottomNavItem("favorites", Icons.Filled.Favorite, "收藏")
}

@Composable
fun MainScreen(context: Context) {
    val navController = rememberNavController()
    val repository = remember { DishRepository(context) }
    val favoriteDao = remember { AppDatabase.getDatabase(context).favoriteDao() }
    var savedCategory by remember { mutableStateOf("") }

    val navItems = listOf(BottomNavItem.Home, BottomNavItem.Tips, BottomNavItem.Recommend, BottomNavItem.Favorites)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) { backStackEntry ->
                val returnCategory = backStackEntry.savedStateHandle.get<String>("returnCategory") ?: ""
                if (returnCategory.isNotEmpty()) {
                    savedCategory = returnCategory
                    backStackEntry.savedStateHandle.remove<String>("returnCategory")
                }
                HomeScreen(
                    repository = repository,
                    favoriteDao = favoriteDao,
                    navController = navController,
                    initialCategory = savedCategory
                )
            }
            composable(BottomNavItem.Tips.route) {
                TipsScreen(repository = repository, navController = navController)
            }
            composable(BottomNavItem.Recommend.route) {
                RecommendScreen()
            }
            composable(BottomNavItem.Favorites.route) {
                FavoritesScreen(
                    favoriteDao = favoriteDao,
                    navController = navController
                )
            }
            composable("dish_detail/{dishId}") { backStackEntry ->
                val dishId = Uri.decode(backStackEntry.arguments?.getString("dishId") ?: "")
                DishDetailScreen(
                    dishId = dishId,
                    repository = repository,
                    favoriteDao = favoriteDao,
                    navController = navController
                )
            }
            composable("tip_detail/{tipId}") { backStackEntry ->
                val tipId = Uri.decode(backStackEntry.arguments?.getString("tipId") ?: "")
                TipDetailScreen(tipId = tipId, repository = repository, navController = navController)
            }
        }
    }
}
