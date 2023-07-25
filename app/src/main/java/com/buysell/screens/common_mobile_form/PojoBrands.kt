package com.buysell.screens.common_mobile_form

data class PojoBrands(
    val `data`: List<DataBrands>,
    val message: String,
    val status: Int,
    val success: Boolean
)

data class DataBrands(
    val brand: String,
    val createdAt: String,
    val hasModel: Boolean,
    val id: Int,
    val updatedAt: String
)