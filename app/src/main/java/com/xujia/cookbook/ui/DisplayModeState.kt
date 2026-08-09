package com.xujia.cookbook.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 全局图文模式状态管理
 * 用于在首页、菜品详细页、小技巧页面之间共享图文切换状态
 */
object DisplayModeState {
    /**
     * 当前是否为图片模式
     * true: 图片模式 - 显示图片说明
     * false: 文字模式 - 显示文字说明（默认）
     */
    var isImageMode by mutableStateOf(false)
}
