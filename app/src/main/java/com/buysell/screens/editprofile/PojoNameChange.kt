package com.buysell.screens.editprofile

data class PojoNameChange(
    val `data`: Dataq,
    val message: String,
    val status: Int,
    val success: Boolean
)

data class Dataq(
    val countryCode: Any?,
    val createdAt: String,
    val deviceType: String,
    val email: String,
    val fcmToken: Any?,
    val id: Int,
    val isEmailverified: Boolean,
    val isMobileverified: Boolean,
    val loginType: String,
    val memberSince: String,
    val mobileOtp: Any?,
    val name: String,
    val password: String,
    val phoneNumber: Any?,
    val profileUrl: Any?,
    val socialToken: Any?,
    val socialmediaId: Any?,
    val updatedAt: String
)