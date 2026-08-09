package com.xujia.cookbook.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.xujia.cookbook.data.DishRepository
import com.xujia.cookbook.ui.DisplayModeState
import com.xujia.cookbook.ui.theme.Background
import com.xujia.cookbook.ui.theme.Primary
import com.xujia.cookbook.ui.utils.ImageMapper
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipDetailScreen(tipId: String, repository: DishRepository, navController: NavController, isImageMode: Boolean = false) {
    val tip = remember { repository.getAllTips().find { it.id == tipId } }

    // 拦截系统返回键，通过 NavController 正常出栈返回上一级
    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tip?.title ?: "技巧") },
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
                }
            )
        }
    ) { padding ->
        if (tip != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (isImageMode && ImageMapper.hasTipImage(tipId)) {
                    // 图片模式：显示小技巧图片
                    item {
                        val context = LocalContext.current
                        val imagePath = ImageMapper.getTipImagePath(tipId)
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
                                    contentDescription = tip.title,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                        }
                    }
                } else {
                    // 文字模式（或无图片时）：显示 Markdown 文字内容
                    item {
                        MarkdownText(markdown = tip.content)
                    }
                }
            }
        }
    }
}
