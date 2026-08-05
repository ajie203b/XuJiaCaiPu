package com.xujia.cookbook.data

import com.google.gson.annotations.SerializedName

/**
 * 菜品数据模型
 */
data class Dish(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("difficulty") val difficulty: Int,
    @SerializedName("introduction") val introduction: String = "",
    @SerializedName("ingredients_raw") val ingredientsRaw: String = "",
    @SerializedName("ingredients_list") val ingredientsList: List<String> = emptyList(),
    @SerializedName("calculation") val calculation: String = "",
    @SerializedName("operations") val operations: String = "",
    @SerializedName("additional") val additional: String = "",
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("main_image") val mainImage: String = "")

/**
 * 技巧文章数据模型
 */
data class Tip(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("file") val file: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String)

/**
 * 菜品列表响应
 */
data class DishesResponse(
    @SerializedName("version") val version: String,
    @SerializedName("total") val total: Int,
    @SerializedName("categories") val categories: List<String>,
    @SerializedName("dishes") val dishes: List<Dish>)

/**
 * 技巧列表响应
 */
data class TipsResponse(
    @SerializedName("version") val version: String,
    @SerializedName("total") val total: Int,
    @SerializedName("tips") val tips: List<Tip>)
