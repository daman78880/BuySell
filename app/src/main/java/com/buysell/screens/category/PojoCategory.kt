package com.buysell.screens.category

data class PojoCategory(
    val `data`: List<PojoCategoryData>,
    val message: String,
    val status: Int,
    val success: Boolean
)

data class PojoCategoryData(
    val category: String,
    val createdAt: String,
    val icon: String,
    val id: Int,
    val updatedAt: String
)