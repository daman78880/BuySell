package com.buysell.screens.login

data class PojoLogin(
    val `data`: Data,
    val message: String,
    val status: Int,
    val success: Boolean,
    val token: String
)
data class Data(
    val countryCode: Any?,
    val deviceType: String?,
    val email: String?,
    val fcmToken: Any?,
    val id: Int?,
    val isEmailverified: Boolean?,
    val isMobileverified: Boolean?,
    val loginType: String?,
    val memberSince: String?,
    val mobileOtp: Any?,
    val name: String?,
    val phoneNumber: Any?,
    val profileUrl: Any?,
    val socialToken: Any?,
    val socialmediaId: Any?

)