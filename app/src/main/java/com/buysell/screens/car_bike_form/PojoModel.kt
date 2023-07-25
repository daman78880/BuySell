package com.buysell.screens.car_bike_form

data class PojoModel(
    val `data`: List<DataModel>,
    val message: String,
    val status: Int,
    val success: Boolean
)

data class DataModel(
    val brandId: Int,
    val createdAt: String,
    val id: Int,
    val modelName: String,
    val updatedAt: String
)