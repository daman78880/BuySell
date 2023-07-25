package com.buysell.screens.productdetail

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PojoProductDetail(
    val `data`: Data,
    val message: String,
    val status: Int,
    val success: Boolean
): Parcelable
@Parcelize
data class Data(
    val User: User?,
    val brand: String?,
    val categoryId: String,
    val createdAt: String?,
    val description: String?,
    val fuel: String?,
    val id: Int,
    val images: List<Image>?,
    val isAlreadyView: Boolean?,
    val isReported: Boolean?,
    val kmDriven: String?,
    val latitude: String?,
    val likeCount: Int?,
    val liked: Boolean?,
    val location: String?,
    val longitude: String?,
    val minBid: String?,
    val numberOfowners: String?,
    val price: String?,
    val status: String?,
    val title: String?,
    val transmission: String?,
    val updatedAt: String?,
    val userId: Int,
    val viewCount: Int?,
    val year: String?
): Parcelable
@Parcelize
data class User(
    val memberSince: String,
    val name: String,
    val profileUrl: String?
): Parcelable

@Parcelize
data class Image(
    val createdAt: String,
    val id: Int,
    val images: String,
    val post_id: Int,
    val updatedAt: String,
    val userId: Int
): Parcelable