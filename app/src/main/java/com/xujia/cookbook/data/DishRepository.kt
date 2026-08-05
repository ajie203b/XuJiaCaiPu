package com.xujia.cookbook.data

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

/**
 * 数据仓库 - 从 assets 读取 JSON 数据
 */
class DishRepository(private val context: Context) {
    private val gson = Gson()
    private var dishesResponse: DishesResponse? = null
    private var tipsResponse: TipsResponse? = null

    /**
     * 获取所有菜品
     */
    fun getAllDishes(): List<Dish> {
        if (dishesResponse == null) {
            val json = readAssetFile("data/dishes.json")
            dishesResponse = gson.fromJson(json, DishesResponse::class.java)
        }
        return dishesResponse?.dishes ?: emptyList()
    }

    /**
     * 获取所有分类
     */
    fun getCategories(): List<String> {
        return dishesResponse?.categories ?: emptyList()
    }

    /**
     * 根据分类获取菜品
     */
    fun getDishesByCategory(category: String): List<Dish> {
        return getAllDishes().filter { it.category == category }
    }

    /**
     * 根据ID获取菜品
     */
    fun getDishById(id: String): Dish? {
        return getAllDishes().find { it.id == id }
    }

    /**
     * 搜索菜品
     */
    fun searchDishes(query: String): List<Dish> {
        if (query.isBlank()) return getAllDishes()
        val q = query.lowercase()
        return getAllDishes().filter {
            it.name.lowercase().contains(q) ||
            it.ingredientsList.any { ing -> ing.lowercase().contains(q) }
        }
    }

    /**
     * 获取所有技巧
     */
    fun getAllTips(): List<Tip> {
        if (tipsResponse == null) {
            val json = readAssetFile("data/tips.json")
            tipsResponse = gson.fromJson(json, TipsResponse::class.java)
        }
        return tipsResponse?.tips ?: emptyList()
    }

    /**
     * 读取 assets 文件
     */
    private fun readAssetFile(path: String): String {
        return context.assets.open(path).use { inputStream ->
            InputStreamReader(inputStream, "UTF-8").use { reader ->
                reader.readText()
            }
        }
    }
}
