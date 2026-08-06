package com.xujia.cookbook.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xujia.cookbook.data.Dish
import com.xujia.cookbook.data.Favorite
import com.xujia.cookbook.data.FavoriteDao
import com.xujia.cookbook.ui.components.DishCard
import com.xujia.cookbook.ui.theme.*

@Composable
fun FavoritesScreen(favoriteDao: FavoriteDao, navController: NavController) {
    val favorites by favoriteDao.getAllFavorites().collectAsState(initial = emptyList())

    // 从收藏中提取分类列表
    val categories = remember(favorites) {
        favorites.map { it.category }.distinct()
    }
    var selectedCategory by remember(categories) {
        mutableStateOf(categories.firstOrNull() ?: "")
    }

    // 按分类筛选
    val filteredFavorites = remember(favorites, selectedCategory) {
        if (selectedCategory.isEmpty()) favorites
        else favorites.filter { it.category == selectedCategory }
    }

    if (favorites.isEmpty()) {
        // 空状态
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = TextSecondary
                )
                Text(
                    text = "还没有收藏任何菜品",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
                Text(
                    text = "在菜品详情页点击❤️收藏",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "我的收藏 (${favorites.size})",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            // 分类筛选标签
            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            // 收藏菜品网格
            items(filteredFavorites, key = { it.dishId }) { favorite ->
                FavoriteItem(
                    favorite = favorite,
                    onClick = { navController.navigate("dish_detail/" + Uri.encode(favorite.dishId)) }
                )
            }
        }
    }
}

/**
 * 收藏列表项 - 从 Favorite 数据构造一个轻量的 Dish 用于复用 DishCard
 */
@Composable
private fun FavoriteItem(favorite: Favorite, onClick: () -> Unit) {
    val dish = remember(favorite) {
        Dish(
            id = favorite.dishId,
            name = favorite.dishName,
            category = favorite.category,
            difficulty = favorite.difficulty,
            mainImage = favorite.mainImage
        )
    }
    DishCard(dish = dish, onClick = onClick)
}
