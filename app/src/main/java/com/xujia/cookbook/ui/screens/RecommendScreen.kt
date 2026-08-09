package com.xujia.cookbook.ui.screens

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xujia.cookbook.R
import com.xujia.cookbook.data.Dish
import com.xujia.cookbook.data.DishRepository
import com.xujia.cookbook.ui.components.DishCard
import com.xujia.cookbook.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun RecommendScreen(repository: DishRepository, navController: NavController) {
    val allDishes = remember { repository.getAllDishes() }
    val categories = remember { repository.getCategories() }

    // 选中的分类（默认全选）
    var selectedCategories by remember(categories) {
        mutableStateOf(categories.toSet())
    }

    // 当前展示的菜品（使用 rememberSaveable 保证页面重组和导航后状态不丢失）
    var currentDishId by rememberSaveable { mutableStateOf<String?>(null) }
    var currentDish by remember(currentDishId, allDishes) {
        mutableStateOf(allDishes.find { it.id == currentDishId })
    }

    // 是否正在随机
    var isPlaying by rememberSaveable { mutableStateOf(false) }

    // 记录是否已经做出过推荐（停止后锁定）
    var hasLocked by rememberSaveable { mutableStateOf(false) }

    // 轮播用的无限动画计数
    val infiniteTransition = rememberInfiniteTransition(label = "random")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tick"
    )

    // 筛选后的菜品池
    val filteredDishes = remember(allDishes, selectedCategories) {
        allDishes.filter { it.category in selectedCategories }
    }

    // 轮播逻辑：播放时快速切换
    LaunchedEffect(isPlaying, tick, filteredDishes) {
        if (isPlaying && filteredDishes.isNotEmpty()) {
            currentDishId = filteredDishes.random().id
            delay(80)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题
        Text(
            text = "今天吃什么？",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Text(
            text = "不知道吃？让运气来决定！",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 分类筛选标签
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = category in selectedCategories,
                    onClick = {
                        selectedCategories = if (category in selectedCategories) {
                            if (selectedCategories.size > 1) selectedCategories - category else selectedCategories
                        } else {
                            selectedCategories + category
                        }
                    },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 中央展示卡片区域
        if (filteredDishes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "请选择至少一个分类",
                    fontSize = 18.sp,
                    color = TextSecondary
                )
            }
        } else {
            // 展示卡片或占位
            if (currentDish == null) {
                // 未开始时显示应用图标占位
                Image(
                    painter = painterResource(id = R.drawable.ic_chef_hat),
                    contentDescription = "徐家菜谱",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 展示菜品卡片，可点击跳转详情
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                ) {
                    DishCard(
                        dish = currentDish!!,
                        onClick = {
                            if (!isPlaying) {
                                navController.navigate("dish_detail/" + Uri.encode(currentDish!!.id) + "/" + Uri.encode(currentDish!!.category))
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 分类提示
        if (!isPlaying && currentDish != null) {
            Text(
                text = "分类：${currentDish!!.category}",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "点击卡片查看详情",
                fontSize = 12.sp,
                color = TextSecondary.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 开始/停止按钮
        Button(
            onClick = {
                if (isPlaying) {
                    // 停止：锁定当前菜品
                    isPlaying = false
                    hasLocked = true
                } else if (hasLocked) {
                    // 换一个：直接推荐一个新菜品，不播放动画
                    if (filteredDishes.isNotEmpty()) {
                        currentDishId = filteredDishes.random().id
                    }
                } else {
                    if (filteredDishes.isNotEmpty()) {
                        isPlaying = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            ),
            enabled = filteredDishes.isNotEmpty()
        ) {
            Text(
                text = when {
                    isPlaying -> "🛑 停止"
                    hasLocked -> "🔄 换一个"
                    else -> "🎲 开始"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 提示文字
        Text(
            text = if (isPlaying) "点击停止锁定今日美食" else "点击开始随机推荐",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
