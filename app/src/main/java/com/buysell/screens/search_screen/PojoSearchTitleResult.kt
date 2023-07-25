package com.buysell.screens.search_screen

 data class PojoSearchTitleResult(
    val `data`: List<Data>,
    val message: String,
    val status: Int,
    val success: Boolean
)

data class Data(
    val User: User,
    val brand: String,
    val categoryId: String,
    val createdAt: String,
    val description: String,
    val fuel: String,
    val id: Int,
    val images: List<Image>,
    val isAlreadyView: Boolean,
    val isOffered: Boolean,
    val isReported: Boolean,
    val kmDriven: String,
    val latitude: String,
    val likeCount: Int,
    val liked: Boolean,
    val location: String,
    val longitude: String,
    val minBid: String,
    val numberOfowners: String,
    val price: Int,
    val status: String,
    val title: String,
    val transmission: String,
    val updatedAt: String,
    val userId: Int,
    val viewCount: Int,
    val year: String
)

data class User(
    val latitude: String,
    val location: String,
    val longitude: String,
    val memberSince: String,
    val name: String,
    val profileUrl: String?
)

data class Image(
    val createdAt: String,
    val id: Int,
    val images: String,
    val post_id: Int,
    val updatedAt: String,
    val userId: Int
)