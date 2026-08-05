package com.xujia.cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.xujia.cookbook.ui.theme.XuJiaCookbookTheme
import com.xujia.cookbook.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XuJiaCookbookTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(context = this@MainActivity)
                }
            }
        }
    }
}
