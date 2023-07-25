package com.buysell.screens.chatuser

data class PojoBuyingChatUser(
    var userImage: String = "",
    var userName: String = "",
    var msg: String = "",
    var time: Long = 0,
    var productImage: String = "",
    var userId: Long = 0,
    var productId: Long? = null,
    var productMinBid: Long? = null,
    var productPrice: Long? = null,
    var productOwner: Long? = null,
)
