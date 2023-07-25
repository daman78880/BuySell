package com.buysell.screens.chatuser

data class PojoChatOfferDataModel (var offerMessage:String="",
                                   var offerStatus:Int=0,
                                    var ownerId:String="",
                                   var ownerOfferStatus:Int=0,
                                    var price:String="",
                                    var productId:String="",
                                    var senderById:String="",
                                    var sendToId:String="",
)