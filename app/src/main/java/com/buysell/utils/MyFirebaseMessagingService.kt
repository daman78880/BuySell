package com.buysell.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.buysell.R
import com.buysell.screens.chat.ChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

class MyFirebaseMessagingService : FirebaseMessagingService()  {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageRe/ceived(remoteMessage)
        Log.i("asnasjkfnadsfksnfd", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d("asnasjkfnadsfksnfd", "Message data payload: " + remoteMessage.data)


/*
//            val msg=mJsonObject.getString("message")
//                val daata=JSONObject(remoteMessage.data["data"])
                val title=remoteMessage.data.get("title")
                val message=remoteMessage.data.get("message")
//                val subTitle=remoteMessage.data.get("subTitle")
//                val extraData=remoteMessage.data.get("extraData")
                sendNotification(title!!,message!!,"subTitle"!!,"extraData"!!)*/


            val title= remoteMessage.data["title"]
            val message= remoteMessage.data["message"]
            var userId = remoteMessage.data["userId"]
            var chatId = remoteMessage.data["chatId"]
            if(userId==null || userId.isEmpty())
                userId="0"
            val userName = remoteMessage.data["userName"]
            val userImage = remoteMessage.data["userImage"]

            var productId = remoteMessage.data["productId"]
            if(productId==null || productId.isEmpty())
                productId="0"
            val productImage = remoteMessage.data["productImage"]
            var productMinBid = remoteMessage.data["productMinBid"]
            if(productMinBid==null || productMinBid.isEmpty())
                productMinBid="0"
            var productPrice = remoteMessage.data["productPrice"]
            if(productPrice==null || productPrice.isEmpty())
                productPrice="0"
            var productOwner = remoteMessage.data["productOwner"]
            if(productOwner==null || productOwner.isEmpty())
                productOwner="0"
            if(chatId!=Extentions.tempChatId) {
                sendNotification(
                    chatId?:"",
                    title ?: "",
                    message ?: "",
                    userId.toString().toInt() ?: 0,
                    userName ?: "",
                    userImage ?: "",
                    productId.toString().toInt() ?: 0,
                    productImage ?: "",
                    productMinBid.toString().toInt() ?: 0,
                    productPrice.toString().toInt() ?: 0,
                    productOwner.toString().toInt() ?: 0
                )
            }

        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("asnasjkfnadsfksnfd", "Message Notification Body: ${it.body}")
//            val daata=JSONObject(remoteMessage.data["data"])
//            val title=daata.getString("title")
//            val message=daata.getString("message")
//            val subTitle=daata.getString("subTitle")
//            val extraData=daata.getString("extraData")
//            sendNotification(title,message,subTitle,extraData)}


            val title= remoteMessage.data["title"]
            val message= remoteMessage.data["message"]
            val chatId = remoteMessage.data["chatId"]
            var userId = remoteMessage.data["userId"]
            if(userId==null || userId?.isEmpty()==true)
                userId="0"
            val userName = remoteMessage.data["userName"]
            val userImage = remoteMessage.data["userImage"]

            var productId = remoteMessage.data["productId"]
            if(productId==null || productId?.isEmpty()==true)
                productId="0"
            val productImage = remoteMessage.data["productImage"]
            var productMinBid = remoteMessage.data["productMinBid"]
            if(productMinBid==null || productMinBid?.isEmpty()==true)
                productMinBid="0"
            var productPrice = remoteMessage.data["productPrice"]
            if(productPrice==null || productPrice?.isEmpty()==true)
                productPrice="0"
            var productOwner = remoteMessage.data["productOwner"]
            if(productOwner==null || productOwner?.isEmpty()==true)
                productOwner="0"

            if(chatId!=Extentions.tempChatId) {
                sendNotification(chatId?:"",
                    title ?: "",
                    message ?: "",
                    userId.toString().toInt() ?: 0,
                    userName ?: "",
                    userImage ?: "",
                    productId.toString().toInt() ?: 0,
                    productImage ?: "",
                    productMinBid.toString().toInt() ?: 0,
                    productPrice.toString().toInt() ?: 0,
                    productOwner.toString().toInt() ?: 0
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d("asnasjkfnadsfksnfd", "Refreshed token: $token")
        sendRegistrationToServer(token)
    }
    private fun sendRegistrationToServer(token: String?) {
        Log.d("asnasjkfnadsfksnfd", "registernation token is : $token")

    }
    private fun sendNotification(
        chatId: String,
        title: String,
        message: String,
        userId: Int,
        userName: String,
        userImage: String,
        productId: Int,
        productImage: String,
        productMinBid: Int,
        productPrice: Int,
        productOwner: Int
    ) {
        Log.i("asnasjkfnadsfksnfd","sowind notifivation userId- $userId\nuserName-$userName\nuserProfile-$userImage\nproductId-$productId" +
                "\nproductMinBid-$productMinBid\nproductImage-$productImage\nproductOwner-$productOwner end\n\n")

        val extraBundle=Bundle()
        extraBundle.putInt("userId",userId)
        extraBundle.putString("userName",userName)
        extraBundle.putString("userImage",userImage)

        extraBundle.putInt("productId",productId)
        extraBundle.putInt("productOwner",productOwner)
        extraBundle.putString("productImage",productImage)
        extraBundle.putInt("productMinBid",productMinBid)
        extraBundle.putInt("productPrice",productPrice)
        Log.i("asnasjkfnadsfksnfd","message is ${title}")
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("userId",userId)
        intent.putExtra("userName",userName)
        intent.putExtra("userImage",userImage)

        intent.putExtra("productId",productId)
        intent.putExtra("productImage",productImage.toString())
        intent.putExtra("productMinBid",productMinBid)
        intent.putExtra("productPrice",productPrice)
        intent.putExtra("productOwner",productOwner)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val penIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT)
        val penIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
//     val intent2=   PendingIntent.getActivity(
//            applicationContext,
//            101,
//         intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
//        )

        val channelId = chatId
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.buy_sell)
//            .setContentTitle(getString(R.string.fcm_message))
            .setContentTitle(title)
            .setSubText(message)
            .setContentText(message)
            .setExtras(extraBundle)
            .setAutoCancel(true)
            .setContentIntent(penIntent)


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
var random=Random().nextInt(10000)
        notificationManager.notify(channelId, random,  notificationBuilder.build())
    }

}