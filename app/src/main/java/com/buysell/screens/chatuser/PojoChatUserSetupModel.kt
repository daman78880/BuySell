package com.buysell.screens.chatuser

data class PojoChatUserSetupModel(
    var chatBlockStatus: Boolean = false,
    var chatID: String, var members: List<String>, var productData: PojoChatProductDataModel,
    var updatedAt: Long
)