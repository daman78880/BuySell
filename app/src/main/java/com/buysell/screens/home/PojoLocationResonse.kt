package com.buysell.screens.home

data class PojoLocationResonse(
    val data: Dataa,
    val message: String,
    val status: Int,
    val success: Boolean
)

data class Dataa(
    val latitude: String?,
    val location: String?,
    val longitude: String?
)