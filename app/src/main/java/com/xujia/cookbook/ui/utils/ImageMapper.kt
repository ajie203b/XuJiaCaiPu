package com.xujia.cookbook.ui.utils

import android.content.Context

/**
 * 图片路径映射工具类
 * 提供菜品名称和小技巧ID到assets图片路径的映射
 */
object ImageMapper {

    /**
     * 小技巧ID到图片文件名的映射表
     */
    private val tipImageMap = mapOf(
        "tip_去腥" to "去腥指南.jpg",
        "tip_学习凉拌" to "凉拌 (Cold Tossed).jpg",
        "tip_学习炒与煎" to "基础炒_煎指南与不粘技巧.jpg",
        "tip_学习焯水" to "焯水.jpg",
        "tip_学习煮" to "煮.jpg",
        "tip_学习腌" to "肉类腌渍指南.jpg",
        "tip_学习蒸" to "蒸法基础指南.jpg",
        "tip_微波炉" to "使用微波炉指南.jpg",
        "tip_空气炸锅" to "空气炸锅使用指南.jpg",
        "tip_食品安全" to "食品安全.jpg",
        "tip_高压力锅" to "煮锅蒸米饭.jpg",
        "tip_食材相克与禁忌" to "揭秘食材搭配的智慧：这些食物不宜同食.jpg",
        "tip_如何选择现在吃什么" to "如何决策吃什么.jpg",
        "tip_油温判断技巧" to "油温判断技巧及换算表.jpg",
        "tip_糖色的炒制" to "糖色的炒制.jpg",
        "tip_辅料技巧" to "辅料与放盐技巧.jpg",
        "tip_高级专业术语" to "做菜专业术语.jpg"
    )

    /**
     * 菜品名称无法精确匹配时的关键词兜底映射
     * 当精确匹配、前缀匹配、包含匹配都失败时使用
     */
    private val dishKeywordMap = mapOf(
        "豉汁排骨" to "鼓汁排骨.jpg",
        "清蒸鳜鱼" to "清蒸鱼.jpg",
        "茭白炒肉" to "菱白炒肉.jpg",
        "手撕包菜" to "包菜炒鸡蛋粉丝.jpg",
        "炒茄子" to "红烧茄子.jpg",
        "脆皮豆腐" to "葱煎豆腐.jpg",
        "松仁玉米" to "玉米排骨汤.jpg",
        "印度土豆花菜" to "干锅花菜.jpg",
        "印度葫芦丸子" to "西葫芦炒鸡蛋.jpg",
        "西红柿豆腐汤羹" to "西红柿鸡蛋汤.jpg",
        "小酥肉" to "炸鲜奶.jpg",
        "杀猪菜" to "猪肉烩酸菜.jpg",
        "黄油鸡" to "咖喱肥牛.jpg",
        "卤菜" to "酱牛肉.jpg",
        "西红柿土豆炖牛肉" to "西红柿牛腩的做法.jpg",
        "香干芹菜炒肉" to "香干肉丝.jpg",
        "溏心蛋" to "唐心蛋的做法.jpg",
        "空气炸锅面包片" to "空气炸锅烤全麦面包片.jpg",
        "生汆丸子汤" to "生氽丸子汤.jpg",
        "排骨山药玉米汤" to "玉米排骨汤.jpg",
        "醪糟小汤圆" to "穆糟小汤圆.jpg",
        "酸梅汤（半成品加工）" to "酸梅汤.jpg",
        "豉汁蒸白鱔" to "豉汁蒸白鳝.jpg",
        "韩国麻药鸡蛋" to "茶香浓郁茶叶蛋.jpg",
        "响油鳝丝" to "豉汁蒸白鳝.jpg",
        "鲤鱼炖白菜" to "白菜猪肉炖粉条.jpg",
        "鳊鱼炖豆腐" to "蝙鱼炖豆腐.jpg",
        "日式肥牛丼饭" to "日式肥牛开饭的做法.jpg",
        "红烧鱼" to "红烧鱼(Braised Fish).jpg",
        "酒酿醪糟" to "酒酿糟的做法.jpg"
    )

    // 缓存菜品图片文件名列表（懒加载）
    private var dishImageFiles: Set<String>? = null

    /**
     * 初始化菜品图片文件名缓存
     */
    fun initDishImages(context: Context) {
        if (dishImageFiles == null) {
            dishImageFiles = try {
                context.assets.list("images/dishes")?.toHashSet() ?: emptySet()
            } catch (e: Exception) {
                emptySet()
            }
        }
    }

    /**
     * 获取菜品图片的 assets 路径（支持模糊匹配）
     * @param dishName 菜品名称
     * @return assets 中的图片路径，如果不存在则返回 null
     */
    fun getDishImagePath(dishName: String): String? {
        val images = dishImageFiles ?: return null

        // 1. 精确匹配：菜品名.jpg
        val exactMatch = "$dishName.jpg"
        if (images.contains(exactMatch)) {
            return "images/dishes/$exactMatch"
        }

        // 2. 前缀匹配：图片文件名以菜品名开头（如 "炒青菜" → "炒青菜的做法.jpg"）
        val prefixMatch = images.find { it.startsWith(dishName) }
        if (prefixMatch != null) {
            return "images/dishes/$prefixMatch"
        }

        // 3. 包含匹配：图片文件名包含菜品名
        val containsMatch = images.find { it.contains(dishName) }
        if (containsMatch != null) {
            return "images/dishes/$containsMatch"
        }

        // 4. 关键词兜底映射
        dishKeywordMap[dishName]?.let { mappedFile ->
            if (images.contains(mappedFile)) {
                return "images/dishes/$mappedFile"
            }
        }

        return null
    }

    /**
     * 获取小技巧图片的 assets 路径
     * @param tipId 小技巧ID
     * @return assets 中的图片路径，如果不存在则返回 null
     */
    fun getTipImagePath(tipId: String): String? {
        val fileName = tipImageMap[tipId] ?: return null
        return "images/tips/$fileName"
    }

    /**
     * 检查小技巧是否有对应的图片
     * @param tipId 小技巧ID
     * @return 是否有对应图片
     */
    fun hasTipImage(tipId: String): Boolean {
        return tipImageMap.containsKey(tipId)
    }
}
