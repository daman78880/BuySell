package com.buysell.screens.chatuser

data class PojoChatLastMsgDetailModel(
    var fileName: String = "",
    var message: String = "",
    var messageTime: Long = 0,
    var messageType: Long = 0,
    var opponentID: String = "",
    var readStatus: Boolean = false,
    var senderID: String = "",
    var offerData: PojoChatOfferDataModel,
    var opened: Boolean = false
)

data class PojoChatLastMsgDetailModelTwo(
    var fileName: String = "",
    var message: String = "",
    var messageTime: Long = 0,
    var messageType: Long = 0,
    var opponentID: String = "",
    var readStatus: Boolean = false,
    var senderID: String = "",
    var offerData: PojoChatOfferDataModel,
    var opened: Boolean = false,
    var msgId:String
)