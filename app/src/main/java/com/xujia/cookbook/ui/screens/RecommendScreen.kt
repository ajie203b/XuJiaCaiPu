package com.xujia.cookbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xujia.cookbook.ui.theme.*

@Composable
fun RecommendScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.Recommend,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Primary.copy(alpha = 0.5f)
            )
            Text(text = "为你推荐", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = "即将上线，敬请期待", fontSize = 14.sp, color = TextSecondary)
        }
    }
}
