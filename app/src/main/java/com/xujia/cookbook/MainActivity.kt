package com.xujia.cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.xujia.cookbook.ui.theme.Background
import com.xujia.cookbook.ui.theme.XuJiaCookbookTheme
import com.xujia.cookbook.ui.MainScreen
import com.xujia.cookbook.ui.utils.ImageMapper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化菜品图片缓存
        ImageMapper.initDishImages(this)
        // 设置状态栏为浅色背景，图标使用深色
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Background.toArgb()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setContent {
            XuJiaCookbookTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(context = this@MainActivity)
                }
            }
        }
    }
}
