package com.xujia.cookbook.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xujia.cookbook.data.Dish
import com.xujia.cookbook.data.DishRepository
import com.xujia.cookbook.data.FavoriteDao
import com.xujia.cookbook.ui.components.DishCard
import com.xujia.cookbook.ui.theme.*
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: DishRepository,
    favoriteDao: FavoriteDao,
    navController: NavController,
    initialCategory: String = ""
) {
    val allDishes = remember { repository.getAllDishes() }
    val categories = remember { repository.getCategories() }
    var selectedCategory by remember {
        mutableStateOf(initialCategory.ifEmpty { categories.firstOrNull() ?: "" })
    }

    LaunchedEffect(initialCategory) {
        if (initialCategory.isNotEmpty() && initialCategory != selectedCategory) {
            selectedCategory = initialCategory
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    val filteredDishes = remember(selectedCategory, searchQuery) {
        allDishes.filter { it.category == selectedCategory }
            .let { dishes ->
                if (searchQuery.isBlank()) dishes
                else dishes.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                        it.ingredientsList.any { ing -> ing.contains(searchQuery, ignoreCase = true) }
                }
            }
    }

    val gridState = rememberLazyGridState()
    val showScrollToTop by remember { derivedStateOf { gridState.firstVisibleItemIndex > 2 } }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
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
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "徐家菜谱",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
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
                    placeholder = { Text("搜索菜品或食材...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // 分类标签
            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
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

            // 菜品卡片网格
            items(filteredDishes, key = { it.id }) { dish ->
                DishCard(dish = dish, onClick = {
                        navController.currentBackStackEntry?.arguments?.putString("fromCategory", selectedCategory)
                        navController.navigate("dish_detail/" + Uri.encode(dish.id))
                    })
            }
        }

        // 回到顶部按钮
        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { coroutineScope.launch { gridState.animateScrollToItem(0) } },
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "回到顶部")
            }
        }
    }
}
