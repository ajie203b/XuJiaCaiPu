package com.xujia.cookbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
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
import com.xujia.cookbook.data.*
import com.xujia.cookbook.ui.theme.*
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailScreen(
    dishId: String,
    repository: DishRepository,
    favoriteDao: FavoriteDao,
    navController: NavController
) {
    val dish = remember { repository.getDishById(dishId) }
    val scope = rememberCoroutineScope()
    val isFavorite by favoriteDao.isFavorite(dishId).collectAsState(initial = false)

    if (dish == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("菜品不存在")
        }
        return
    }

    val dishCategory = remember { dish.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dish.name) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("returnCategory", dishCategory)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            if (isFavorite) {
                                favoriteDao.deleteById(dishId)
                            } else {
                                favoriteDao.insert(
                                    Favorite(
                                        dishId = dish.id,
                                        dishName = dish.name,
                                        category = dish.category,
                                        mainImage = dish.mainImage,
                                        difficulty = dish.difficulty
                                    )
                                )
                            }
                        }
                    }) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = if (isFavorite) FavoriteColor else Color.Gray
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 菜品名称、难度和简介
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dish.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "★".repeat(dish.difficulty) + "☆".repeat(5 - dish.difficulty),
                            fontSize = 16.sp,
                            color = StarColor
                        )
                    }
                    if (!dish.introduction.isNullOrEmpty()) {
                        Text(
                            text = dish.introduction,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // 使用循环渲染各个 Markdown 章节，减少重复代码
            val sections = listOf(
                "必备原料和工具" to dish.ingredientsRaw,
                "计算" to dish.calculation,
                "操作步骤" to dish.operations,
                "附加内容" to dish.additional
            )
            sections.forEach { (title, content) ->
                if (!content.isNullOrEmpty()) {
                    item {
                        SectionTitle(title)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Background)
                        ) {
                            MarkdownText(
                                markdown = content,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp),
        color = Primary
    )
}
