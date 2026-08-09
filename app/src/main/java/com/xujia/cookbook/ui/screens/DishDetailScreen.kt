package com.xujia.cookbook.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.xujia.cookbook.data.*
import com.xujia.cookbook.ui.DisplayModeState
import com.xujia.cookbook.ui.theme.*
import com.xujia.cookbook.ui.utils.ImageMapper
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailScreen(
    dishId: String,
    @Suppress("UNUSED_PARAMETER") category: String,
    repository: DishRepository,
    favoriteDao: FavoriteDao,
    navController: NavController,
    isImageMode: Boolean = false
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

    // 拦截系统返回键，返回到对应的分类详情页
    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dish.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 图文转换按钮
                    IconButton(onClick = { DisplayModeState.isImageMode = !DisplayModeState.isImageMode }) {
                        Icon(
                            imageVector = if (DisplayModeState.isImageMode) Icons.Filled.Image else Icons.Filled.TextFields,
                            contentDescription = if (DisplayModeState.isImageMode) "当前为图片模式" else "当前为文字模式",
                            tint = Primary
                        )
                    }
                    // 收藏按钮
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
            // 菜品名称、难度（图片模式下也显示）
            item {
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
            }

            if (isImageMode) {
                // 图片模式：显示菜品大图
                item {
                    val context = LocalContext.current
                    val imagePath = ImageMapper.getDishImagePath(dish.name)
                    if (imagePath != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Background)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data("file:///android_asset/$imagePath")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = dish.name,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    } else {
                        Text(
                            text = "暂无图片",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                }
            } else {
                // 文字模式：显示原有的 Markdown 内容
                item {
                    if (!dish.introduction.isNullOrEmpty()) {
                        Text(
                            text = dish.introduction,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
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
