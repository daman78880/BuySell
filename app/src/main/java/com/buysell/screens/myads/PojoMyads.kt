package com.buysell.screens.myads


 data class PojoMyads(
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
    val isReported: Boolean,
    val kmDriven: String,
    val latitude: String,
    val likeCount: Int,
    var liked: Boolean,
    val location: String,
    val longitude: String,
    val minBid: String,
    val numberOfowners: String,
    val price: String,
    val status: Any?,
    val title: String,
    val transmission: String,
    val updatedAt: String,
    val userId: Int,
    val viewCount: Int,
    val year: String
)

data class User(
    val memberSince: String,
    val name: String,
    val profileUrl: Any?
)

data class Image(
    val createdAt: String,
    val id: Int,
    val images: String,
    val post_id: Int,
    val updatedAt: String,
    val userId: Int
)