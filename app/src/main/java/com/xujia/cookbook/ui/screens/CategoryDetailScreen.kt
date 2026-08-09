package com.xujia.cookbook.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xujia.cookbook.data.DishRepository
import com.xujia.cookbook.ui.components.DishCard
import com.xujia.cookbook.ui.theme.*

/**
 * 分类详情页 - 展示某个分类下的所有菜品
 * 导航层级：主页 -> 分类详情页 -> 菜品详情页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    category: String,
    repository: DishRepository,
    navController: NavController
) {
    val dishes = remember(category) {
        repository.getAllDishes().filter { it.category == category }
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredDishes = remember(dishes, searchQuery) {
        if (searchQuery.isBlank()) dishes
        else dishes.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.ingredientsList.any { ing -> ing.contains(searchQuery, ignoreCase = true) }
        }
    }

    // 拦截系统返回键，返回到主页
    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Primary.copy(alpha = 0.1f), Color.Transparent)
                            )
                        )
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            // 搜索框
            item(span = { GridItemSpan(2) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("搜索该分类下的菜品...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // 菜品卡片网格
            items(filteredDishes, key = { it.id }) { dish ->
                DishCard(dish = dish, onClick = {
                    navController.navigate("dish_detail/" + Uri.encode(dish.id) + "/" + Uri.encode(category))
                })
            }
        }
    }
}
