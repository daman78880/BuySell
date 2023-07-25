package com.buysell.screens.chat

import SharedPref
import android.Manifest
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.databinding.ActivityChatBinding
import com.buysell.screens.chatuser.*
import com.buysell.screens.home.PojoLocationResonse
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.buysell.utils.Extentions.tempChatId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import setToolBar

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    private var userId: Int? = null
    private var userName: String? = null
    private var userProfile: String? = null

    private var loginBid: Int? = null
    private var loginId: Int? = null
    private var loginName: String? = null
    private var loginProfile: String? = null

    private var productId: Int? = null
    private var productImage: String? = null
    private var productMinBid: Int? = null
    private var productPrice: Int? = null
    private var productOwner: Int? = null
    private var chatId = ""
    private lateinit var binding: ActivityChatBinding
    private var firebaseb: FirebaseFirestore? = null
    private var userFoundStatus: Boolean? = null
    private var tempMsg: String? = null
    private val viewModelChatActivity: ViewModelChatActivity by viewModels()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseb = FirebaseFirestore.getInstance()
        init()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        loginBid = intent.getIntExtra("loginBid", 0)
        userId = intent.getIntExtra("userId", 0)
        userName = intent.getStringExtra("userName") ?: ""
        userProfile = intent.getStringExtra("userImage") ?: ""
        productPrice = intent.getIntExtra("productPrice", 0)
        productMinBid = intent.getIntExtra("productMinBid", 0)
        productImage = intent.getStringExtra("productImage") ?: ""
        productId = intent.getIntExtra("productId", 0)
        productOwner = intent.getIntExtra("productOwner", 0)
        loginId = SharedPref(this@ChatActivity).getInt(Constant.ID_kEY)
        loginName = SharedPref(this@ChatActivity).getString(Constant.NAME_KEY)
        loginProfile = SharedPref(this@ChatActivity).getString(Constant.PROFILE_URL)
        if (productOwner == loginId) {
            tempMsg = "Your offer"
        } else {
            tempMsg = "Buyer offer"
        }
        Log.i(
            "asnasjkfnadsfksnfd",
            "userId- $userId\nuserName-$userName\nuserProfile-$userProfile\nproductId-$productId" +
                    "\nproductMinBid-$productMinBid\nproductImage-$productImage\nproductOwner-$productOwner"
        )
        if (loginId!! > userId!!) {
            chatId = "${userId}_$loginId"
        } else {
            chatId = "${loginId}_$userId"
        }
        chatId += "_$productId"
        tempChatId=chatId
        removeNotification(chatId)
        Log.i(TAG, "chat id $chatId")
        val appBarTitle = userName ?: ""
        setToolBar(binding.appBarChat, this@ChatActivity, true, false, appBarTitle, 10f, false)
        getMsg()
        checkUserExist()
        clickListeners()
        lifecycleScope.launchWhenStarted {
            viewModelChatActivity.msgNotificationResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Log.i("asnasjkfnadsfksnfd", "loading notification response")
//                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Log.i("asnasjkfnadsfksnfd", "failed notification response")
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(
                                TAG,
                                "Error ${res?.message} , Error code:${res?.errorCode} , localized msg:${res?.localizedMessage}"
                            )
                            Toast.makeText(this@ChatActivity, "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(
                                this@ChatActivity,
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is ApiState.Success -> {
                        val response = data.data as JsonObject
                        Log.i("asnasjkfnadsfksnfd", "sucess notification response $response")

                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
    }

    private fun clickListeners() {
        binding.apply {
            appBarChat.tbBackBtn.setOnClickListener {
                super.onBackPressed()
            }
            clChatMain.setOnClickListener {
                Extentions.hideKeyboardd(this@ChatActivity)
            }
            appBarChat.appBar.setOnClickListener {
                Extentions.hideKeyboardd(this@ChatActivity)
            }
            imgSendChat.setOnClickListener {
                val msg = etMsgChat.text.toString().trim()
                etMsgChat.text?.clear()
                if (userFoundStatus != null) {
                    val timeSend = System.currentTimeMillis() / 1000
                    if (msg.isNotEmpty()) {
                        if (userFoundStatus!!) {
                            saveUserDetailAdavance(timeSend, msg, 1, 0, 0)
                        } else {
                            saveUserDetailAdavance(timeSend, msg, 1, 0, 0)
                            oneTimeSaveDetail(timeSend)
                        }
                    }
                } else {
                    checkUserExist()
                }
            }
        }
    }

    private fun checkUserExist() {
        firebaseb?.collection("Chats")?.document(chatId)?.get()?.addOnSuccessListener {
            if (it.exists()) {
                Log.i(TAG, "user found")
                userFoundStatus = true
                if (loginBid != null && loginBid!! > 0) {
                    val timeSend = System.currentTimeMillis() / 1000
                    oneTimeSaveDetail(timeSend)
                    saveUserDetailAdavance(timeSend, tempMsg.toString(), 2, loginBid!!, 0)
                }
            } else {
                userFoundStatus = false
                if ((loginBid != null) && loginBid!! > 0) {
                    val timeSend = System.currentTimeMillis() / 1000
                    oneTimeSaveDetail(timeSend)
                    saveUserDetailAdavance(timeSend, tempMsg.toString(), 2, loginBid!!, 0)
//                    askNotificationPermission()
                }
                Log.i(TAG, "user not found")
            }
        }?.addOnFailureListener {
            Log.i(TAG, "init getting user detail failed due to ${it.message}")
        }
    }

    private fun oneTimeSaveDetail(timeSend: Long) {
        // save loginUserDetail
        CoroutineScope(Dispatchers.IO).launch {
            val loginUserDetail = PojoChatUserDetailModel(0, loginProfile!!, loginName!!, false)
            val log = HashMap<String, Any>()
            log[loginId.toString()] = loginUserDetail
            firebaseb?.collection("Chats")?.document(chatId)?.set(log, SetOptions.merge())
                ?.addOnSuccessListener {
                    Log.i(Extentions.TAG, "login detail save successfull")
                }?.addOnFailureListener {
                    Log.i(Extentions.TAG, "login detail save failed due to ${it.message}")
                }
        }
        // save userDetail
        CoroutineScope((Dispatchers.IO)).launch {
            val userData = PojoChatUserDetailModel(0, userProfile ?: "", userName!!, false)
            val userLog = HashMap<String, Any>()
            userLog[userId.toString()] = userData
            firebaseb?.collection("Chats")?.document(chatId)?.set(userLog, SetOptions.merge())
                ?.addOnSuccessListener {
                    Log.i(Extentions.TAG, "login detail save successfull")
                }?.addOnFailureListener {
                    Log.i(Extentions.TAG, "login detail save failed due to ${it.message}")
                }
        }
        // save otherData
        CoroutineScope(Dispatchers.IO).launch {
            val productDetail =
                PojoChatProductDataModel(
                    productMinBid.toString(),
                    productPrice.toString(),
                    productId.toString(),
                    productImage.toString(),
                    productOwner.toString()
                )
            val data = PojoChatUserSetupModel(
                false,
                chatId,
                listOf(loginId.toString(), userId.toString()),
                productDetail,
                timeSend
            )
            firebaseb?.collection("Chats")?.document(chatId)?.set(data, SetOptions.merge())
                ?.addOnSuccessListener {
                    Log.i(Extentions.TAG, "Data saved successfully")
                }?.addOnFailureListener {
                    Log.i(Extentions.TAG, "Data saved failed due to ${it.message}")
                }
        }
    }

    private fun saveUserDetailAdavance(
        time: Long,
        msg: String,
        type: Int,
        minBid: Int,
        offerStatus: Int
    ) {
        var offerDataMsg: PojoChatOfferDataModel? = null
        var lastMsgModelMsg: PojoChatLastMsgDetailModel? = null
        sendNotificationByClick(msg)
        if (chatId.isNotEmpty()) {
            if (type == 1) {
                offerDataMsg = PojoChatOfferDataModel(
                    "",
                    0,
                    productOwner.toString(),
                    0,
                    "",
                    productId.toString(),
                    loginId.toString(),
                    userId.toString()
                )
                lastMsgModelMsg = PojoChatLastMsgDetailModel(
                    productImage.toString(),
                    msg,
                    time,
                    type.toLong(),
                    userId.toString(),
                    false,
                    loginId.toString(),
                    offerDataMsg,
                    false
                )
            } else {
                if (offerStatus == 2) {
                    offerDataMsg = PojoChatOfferDataModel(
                        "I accept the offer, Lets meet and close the deal.",
                        2,
                        productOwner.toString(),
                        2,
                        minBid.toString(),
                        productId.toString(),
                        loginId.toString(),
                        userId.toString()
                    )
                } else {

                    if (productOwner == loginId) {
                        offerDataMsg = PojoChatOfferDataModel(
                            "",
                            1,
                            productOwner.toString(),
                            0,
                            minBid.toString(),
                            productId.toString(),
                            loginId.toString(),
                            userId.toString()
                        )
                    } else {
                        offerDataMsg = PojoChatOfferDataModel(
                            "",
                            0,
                            productOwner.toString(),
                            1,
                            minBid.toString(),
                            productId.toString(),
                            loginId.toString(),
                            userId.toString()
                        )
                    }
                }
                lastMsgModelMsg = PojoChatLastMsgDetailModel(
                    productImage.toString(),
                    msg,
                    time,
                    type.toLong(),
                    userId.toString(),
                    false,
                    loginId.toString(),
                    offerDataMsg,
                    false
                )
            }
            // for MessageSend
            CoroutineScope(Dispatchers.IO).launch {
                firebaseb?.collection("Chats")?.document(chatId)?.collection("Messages")?.document()
                    ?.set(lastMsgModelMsg, SetOptions.merge())?.addOnSuccessListener {
                    Log.i(Extentions.TAG, "lastMsg saved sucessfull")
                    binding.etMsgChat.text?.clear()
                }?.addOnFailureListener {
                    Log.i(Extentions.TAG, "lastMsg saved failed due to ${it.message}")
                }
            }

            // save unreadUserCount
            CoroutineScope(Dispatchers.IO).launch {
                val readStatusLoginUser = HashMap<String, Int>()
                readStatusLoginUser[loginId.toString()] = 0
                readStatusLoginUser[userId.toString()] = 1
                val readModel = HashMap<String, Any>()
                readModel["unreadMessageCount"] = readStatusLoginUser
                Log.i(Extentions.TAG, "unread data $readModel")
                firebaseb?.collection("Chats")?.document(chatId)?.set(readModel, SetOptions.merge())
                    ?.addOnSuccessListener {
                        Log.i(Extentions.TAG, "readMsg data saved successfully")
                    }?.addOnFailureListener {
                    Log.i(Extentions.TAG, "readMsg data saved failed due to ${it.message}")
                }
            }

            // save lastMsgData
            CoroutineScope(Dispatchers.IO).launch {
                val hashMapLastMsg = HashMap<String, Any>()
                hashMapLastMsg.put("lastMessage", lastMsgModelMsg)
                firebaseb?.collection("Chats")?.document(chatId)
                    ?.set(hashMapLastMsg, SetOptions.merge())?.addOnSuccessListener {
                    Log.i(Extentions.TAG, "lastMsg saved sucessfull")
                }?.addOnFailureListener {
                    Log.i(Extentions.TAG, "lastMsg saved failed due to ${it.message}")
                }
            }
        }
    }

    private fun getMsg() {
        val msgList = ArrayList<PojoChatLastMsgDetailModelTwo>()
        firebaseb?.collection("Chats")?.document(chatId)?.collection("Messages")
            ?.addSnapshotListener { value, error ->
                if (value?.isEmpty == true) {
                    Log.i(TAG, "empty msg")
                } else {
                    msgList.clear()
                    val data = value?.documents
                    if (data?.isNotEmpty() == true) {
                        for (i in 0 until data.size) {
                            val fileName = data[i].data?.get("fileName") as String?
                            val opened = data[i].data?.get("opened") as Boolean?
                            val message = data[i].data?.get("message") as String?
                            val messageTime = data[i].data?.get("messageTime") as Long?
                            val messageType = data[i].data?.get("messageType") as Long?
                            val opponentID = data[i].data?.get("opponentID") as String?
                            val readStatus = data[i].data?.get("readStatus") as Boolean?
                            val senderID = data[i].data?.get("senderID") as String?
                            val offerData = data[i].data?.get("offerData") as HashMap<String, Any>
                            val offerMsg = offerData.get("offerMessage") as String?
                            val offerStatus = offerData.get("offerStatus") as Long?
                            val offerStatusLong = offerStatus?.toInt() ?: 0
                            val ownerId = offerData.get("ownerId") as String?
                            val price = offerData.get("price") as String?
                            val productId = offerData.get("productId") as String?
                            val senderById = offerData.get("senderById") as String?
                            val sendToId = offerData.get("sendToId") as String?
                            val ownerOfferStatus = offerData.get("ownerOfferStatus") as Long?
                            val ownerOfferStatusInt = ownerOfferStatus?.toInt() ?: 0
                            val offer = PojoChatOfferDataModel(
                                offerMsg ?: "",
                                offerStatusLong, ownerId ?: "",
                                ownerOfferStatusInt, price ?: "",
                                productId ?: "",
                                senderById ?: "",
                                sendToId ?: ""
                            )

                            msgList.add(
                                PojoChatLastMsgDetailModelTwo(
                                    fileName ?: "",
                                    message ?: "",
                                    messageTime ?: 0,
                                    messageType ?: 1,
                                    opponentID ?: "",
                                    readStatus ?: false,
                                    senderID ?: "",
                                    offer,
                                    opened ?: false,
                                    data.get(i).id
                                )
                            )
                        }
                        msgList.sortBy {
                            it.messageTime
                        }
                        binding.rvChatShowChat.adapter = AdapterChatShow(
                            this@ChatActivity,
                            msgList,
                            object : AdapterChatShow.Click {
                                override fun onClick(
                                    value: Int,
                                    msgId: String,
                                    offerDataModel: PojoChatOfferDataModel
                                ) {
                                    loginBid = offerDataModel.price.toInt()
                                    if (value == 1) {
                                        makeAnOfferDialog(offerDataModel.ownerId, msgId)
                                    } else {
                                        val timeSend = System.currentTimeMillis() / 1000
                                        saveUserDetailAdavance(
                                            timeSend,
                                            tempMsg.toString(),
                                            2,
                                            loginBid!!,
                                            value
                                        )
                                        if (offerDataModel.ownerId == loginId.toString()) {
                                            firebaseb?.collection("Chats")?.document(chatId)
                                                ?.collection("Messages")?.document(msgId)?.update(
                                                mapOf("offerData.ownerOfferStatus" to 0)
                                            )?.addOnSuccessListener {
                                                Log.i("bfsambdfs", "update value 0 sucess")
                                            }?.addOnFailureListener {
                                                Log.i("bfsambdfs", "update value 0 failed")
                                            }
                                        } else {
                                            firebaseb?.collection("Chats")?.document(chatId)
                                                ?.collection("Messages")?.document(msgId)?.update(
                                                mapOf("offerData.offerStatus" to 0)
                                            )?.addOnSuccessListener {
                                                Log.i("bfsambdfs", "update value 0 sucess")
                                            }?.addOnFailureListener {
                                                Log.i("bfsambdfs", "update value 0 failed")
                                            }
                                        }
                                    }
                                }

                                override fun onView(view: View) {
                                    view.setOnClickListener {
                                        Extentions.hideKeyboardd(this@ChatActivity)
                                    }
                                }
                            })
                        binding.rvChatShowChat.scrollToPosition(msgList.size - 1)
                    }
                }
            }
    }

    private fun makeAnOfferDialog(ownerId: String, msgId: String) {
        val dialog = Dialog(this@ChatActivity)
        dialog.setContentView(R.layout.dialog_make_an_offer)
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                this@ChatActivity,
                android.R.color.transparent
            )
        )
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.setCancelable(false)
        val etEnterAmount = dialog.findViewById<AppCompatEditText>(R.id.etAmountMakeAnOfferDialog)

        val btnMakeOffer = dialog.findViewById<AppCompatButton>(R.id.btnMakeAnOfferDialog)
        val btnCancelDialog =
            dialog.findViewById<AppCompatImageView>(R.id.btnCancelMakeAnOfferDialog)
        btnMakeOffer.setOnClickListener {
            val value = etEnterAmount.text?.trim()
            var minBid = loginBid
            if (value?.isNotEmpty()!!) {
                // if you want user didn't able to give offer low then your offer then uncomment this condition which is given below
//                if(value.toString().toInt() > (minBid ?: 100)){
                Log.i(TAG, "Make offer Amount : ${etEnterAmount.text}")
                loginBid = value.toString().toInt()
                val timeSend = System.currentTimeMillis() / 1000
                saveUserDetailAdavance(timeSend, tempMsg.toString(), 2, loginBid!!, 1)
                if (ownerId == loginId.toString()) {
                    firebaseb?.collection("Chats")?.document(chatId)?.collection("Messages")
                        ?.document(msgId)?.update(
                        mapOf("offerData.ownerOfferStatus" to 0)
                    )?.addOnSuccessListener {
                        Log.i("bfsambdfs", "update value 0 sucess")
                    }?.addOnFailureListener {
                        Log.i("bfsambdfs", "update value 0 failed")
                    }
                } else {
                    firebaseb?.collection("Chats")?.document(chatId)?.collection("Messages")
                        ?.document(msgId)?.update(
                        mapOf("offerData.offerStatus" to 0)
                    )?.addOnSuccessListener {
                        Log.i("bfsambdfs", "update value 0 sucess")
                    }?.addOnFailureListener {
                        Log.i("bfsambdfs", "update value 0 failed")
                    }
                }
                dialog.dismiss()
//                }
//                else{
//                    Toast.makeText(this@ChatActivity, "Enter high amount", Toast.LENGTH_SHORT).show()
//                }
            } else {
                Toast.makeText(this@ChatActivity, "Please enter amount", Toast.LENGTH_SHORT).show()
            }
        }
        btnCancelDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
//    intent.putExtra("userId",data.userId.toInt())
//    intent.putExtra("userName",data.userName.toString())
//    intent.putExtra("userImage",data.userImage.toString())
//
//    intent.putExtra("productId",data.productId?.toInt()?:0)
//    intent.putExtra("productImage",data.productImage.toString())
//    intent.putExtra("productMinBid",data.productMinBid?.toInt()?:0)
//    intent.putExtra("productPrice",data.productPrice?.toInt()?:0)
//    intent.putExtra("productOwner",data.productOwner?.toInt()?:-1)


    private fun sendNotificationByClick(msg: String) {
        Log.i(TAG, "send notification funcation called")
//        val topic="topic_$userId"
        val topic = "topic$userId"
//        val topic="$userId"
        Log.i("asnasjkfnadsfksnfd", "send notification topic is->$topic")

        val tokenData = "Bearer ${applicationContext.resources.getString(R.string.fcm_Server_key)}"
        val contentType = "application/json"
        val json = JsonObject()
        val map = JsonObject()
        map.addProperty("title", loginName)
//        map.addProperty("subTitle", msg)
        map.addProperty("message", msg)

        map.addProperty("chatId", chatId)
        map.addProperty("userId", loginId)
        map.addProperty("userName", userName)
        map.addProperty("userImage", userProfile)

        map.addProperty("productId", productId)
        map.addProperty("productImage", productImage)
        map.addProperty("productMinBid", productMinBid ?: 0)
        map.addProperty("productPrice", productPrice ?: 0)
        map.addProperty("productOwner", productOwner ?: 0)
        json.addProperty("to", "/topics/$topic")
        json.add("data", map)
        viewModelChatActivity.sendMessage(tokenData, contentType, json)
    }

    override fun onDestroy() {
        super.onDestroy()
        tempChatId=null
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun removeNotification(chatId:String){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.deleteNotificationChannel(chatId)
    }
}