package com.buysell.screens.chatuser

import SharedPref
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentChatUserBinding
import com.buysell.screens.chat.ChatActivity
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.firebase.firestore.FirebaseFirestore
import setToolBar


class ChatUserFragment : BaseFragment() {
    private lateinit var binding: FragmentChatUserBinding
    lateinit var firebaseb: FirebaseFirestore
    private var loginId: Int? = null
    private var buySellClickStatus=-1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChatUserBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Extentions.stopProgress()
        firebaseb = FirebaseFirestore.getInstance()
        init()
    }

    override fun onStart() {
        super.onStart()
        if (!SharedPref(requireContext()).getBoolean(Constant.GUEST_LOGIN_STATUS)) {
            if (buySellClickStatus == -1 || buySellClickStatus == 1) {
                getBuyerUserData()
            } else {
                getSellerUserData()
            }
        }
    }

    private fun init() {
        loginId = SharedPref(requireContext()).getInt(Constant.ID_kEY)
        binding.apply {
            val appBarTitle = requireActivity().resources.getString(R.string.chat)
            setToolBar(binding.appBarChatUse, requireContext(), true, false, appBarTitle, 10f, true)
            setBuying(true)
            clickListeners()
            binding.apply {

            }
        }
    }

    private fun getBuyerUserData() {
        val dataList = ArrayList<PojoBuyingChatUser>()
        firebaseb.collection("Chats").whereArrayContains("members", loginId.toString())
            .addSnapshotListener { value, error ->
                if (value?.isEmpty == true) {
                    Log.i(TAG, "user not found")

                } else {
                    dataList.clear()
                    Log.i(TAG, "User found")
                    val data = value?.documents
                    for (i in 0 until data?.size!!) {
                        val memeberListTemp = data.get(i).data?.get("members") as ArrayList<*>?
                        if (memeberListTemp != null && memeberListTemp.isNotEmpty()) {
                            Log.i(TAG, "memebre list ${memeberListTemp}")
                            val lastMsgTemp =
                                data[i].get("lastMessage") as HashMap<String, PojoChatLastMsgDetailModel>?
                            if (lastMsgTemp != null && lastMsgTemp.isNotEmpty()) {
                                val productDetailTemp = data[i].get("productData") as HashMap<String, PojoChatProductDataModel>?
                                if (productDetailTemp != null && productDetailTemp.isNotEmpty()) {
                                    Log.i(TAG,"check data $productDetailTemp")
                                    val tempBuying = PojoBuyingChatUser()
                                    tempBuying.msg = lastMsgTemp["message"].toString()
                                    val msgType= lastMsgTemp["messageTime"].toString()
                                    tempBuying.time =msgType.toLong()
                                    tempBuying.productImage = productDetailTemp["productImage"].toString()
                                    tempBuying.productId = productDetailTemp["productId"].toString().toLong()
                                    val minBid=productDetailTemp["minBid"].toString()
                                    tempBuying.productMinBid = minBid.toLong()
                                    val price=productDetailTemp["price"].toString()
                                    tempBuying.productPrice = price.toLong()
                                    val productOwner=productDetailTemp["productOwner"].toString()
                                    if(productOwner!=loginId.toString()) {
                                        tempBuying.productOwner = productOwner.toLong()
                                        for (j in 0 until memeberListTemp.size) {
                                            if (memeberListTemp[j].toString() != loginId.toString()) {

                                                tempBuying.userId =
                                                    memeberListTemp[j].toString().toLong()
                                                val dataTemp =
                                                    data[i].get(memeberListTemp[j].toString()) as HashMap<String, PojoChatUserDetailModel>?
                                                if (dataTemp != null && dataTemp.isNotEmpty()) {
                                                    tempBuying.userImage =
                                                        dataTemp["image"].toString()
                                                    tempBuying.userName =
                                                        dataTemp["name"].toString()

                                                }
                                            }
                                        }
                                        dataList.add(tempBuying)
                                    }
                                    if (data.size - 1 == i) {
                                        dataList.sortByDescending {
                                            it.time
                                        }
                                        if(buySellClickStatus==-1 || buySellClickStatus== 1)
                                        setMyAdsAdapter(dataList)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }
    private fun getSellerUserData(){
        val dataList = ArrayList<PojoBuyingChatUser>()
        firebaseb.collection("Chats").whereArrayContains("members", loginId.toString())
            .addSnapshotListener { value, error ->
                if (value?.isEmpty == true) {
                    Log.i(TAG, "user not found")

                } else {
                    dataList.clear()
                    Log.i(TAG, "User found")
                    val data = value!!.documents
                    for (i in 0 until data.size) {
                        val memeberListTemp = data.get(i).data?.get("members") as ArrayList<*>?
                        if (memeberListTemp != null && memeberListTemp.isNotEmpty()) {
                            Log.i(TAG, "memebre list ${memeberListTemp}")
                            val lastMsgTemp = data[i].get("lastMessage") as HashMap<String, PojoChatLastMsgDetailModel>?
                            if (lastMsgTemp != null && lastMsgTemp.isNotEmpty()) {
                                val productDetailTemp = data[i].get("productData") as HashMap<String, PojoChatProductDataModel>?
                                if (productDetailTemp != null && productDetailTemp.isNotEmpty()) {
                                    Log.i(TAG,"check data $productDetailTemp")
                                    val tempBuying = PojoBuyingChatUser()
                                    tempBuying.msg = lastMsgTemp["message"].toString()
                                    val msgType= lastMsgTemp["messageTime"].toString()
                                    tempBuying.time =msgType.toLong()
                                    tempBuying.productImage = productDetailTemp["productImage"].toString()
                                    tempBuying.productId = productDetailTemp["productId"].toString().toLong()
                                    val minBid=productDetailTemp["minBid"].toString()
                                    tempBuying.productMinBid = minBid.toLong()
                                    val price=productDetailTemp["price"].toString()
                                    tempBuying.productPrice = price.toLong()
                                    val productOwner=productDetailTemp["productOwner"].toString()
                                    if(productOwner==loginId.toString()) {
                                        tempBuying.productOwner = productOwner.toLong()
                                        for (j in 0 until memeberListTemp.size) {
                                            if (memeberListTemp[j].toString() != loginId.toString()) {
                                                Log.i(   "asjdfnbskajfn", "not equal to ${memeberListTemp[j].toString()} == my id ${loginId.toString()}")
                                                tempBuying.userId =
                                                    memeberListTemp[j].toString().toLong()
                                                val dataTemp =
                                                    data[i].get(memeberListTemp[j].toString()) as HashMap<String, PojoChatUserDetailModel>?
                                                if (dataTemp != null && dataTemp.isNotEmpty()) {
                                                    tempBuying.userImage =
                                                        dataTemp["image"].toString()
                                                    tempBuying.userName =
                                                        dataTemp["name"].toString()
                                                    Log.i(
                                                        "asjdfnbskajfn", "name ${dataTemp["name"]}"
                                                    )
                                                }
                                            }
                                        }
                                        dataList.add(tempBuying)
                                    }
                                    if (data.size - 1 == i) {
                                        dataList.sortByDescending {
                                            it.time
                                        }
                                        if(buySellClickStatus==2)
                                        setMyAdsAdapter(dataList)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }


    private fun clickListeners() {
        binding.apply {
            txtTempsellingChatChatUse.setOnClickListener {
                buySellClickStatus=2
                getSellerUserData()
                setSelling(true)
            }
            txtTempBuyingChatUse.setOnClickListener {
                buySellClickStatus=1
                getBuyerUserData()
                setBuying(true)
            }
        }
    }


    private fun setSelling(status: Boolean) {
        binding.apply {
            if (status) {
                txtTempsellingChatChatUse.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtTempsellingChatChatUse.background =
                    ActivityCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)
                setBuying(false)

            } else {
                txtTempsellingChatChatUse.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtTempsellingChatChatUse.background =
                    ActivityCompat.getDrawable(requireContext(), android.R.color.transparent)
            }
        }
    }

    private fun setBuying(status: Boolean) {
        binding.apply {
            if (status) {
                txtTempBuyingChatUse.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtTempBuyingChatUse.background =
                    ActivityCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)
                setSelling(false)

            } else {
                txtTempBuyingChatUse.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtTempBuyingChatUse.background =
                    ActivityCompat.getDrawable(requireContext(), android.R.color.transparent)
            }
        }
    }


    private fun setMyAdsAdapter(list: ArrayList<PojoBuyingChatUser>) {
        binding.apply {
            rvChatUse.adapter =
                AdatperChatUser(requireContext(), list, object : AdatperChatUser.Clicks {
                    override fun onClick(data: PojoBuyingChatUser) {
                        val intent = Intent(this@ChatUserFragment.requireContext(), ChatActivity::class.java)
                        intent.putExtra("userId",data.userId.toInt())
                        intent.putExtra("userName",data.userName.toString())
                        intent.putExtra("userImage",data.userImage.toString())

                        intent.putExtra("productId",data.productId?.toInt()?:0)
                        intent.putExtra("productImage",data.productImage.toString())
                        intent.putExtra("productMinBid",data.productMinBid?.toInt()?:0)
                        intent.putExtra("productPrice",data.productPrice?.toInt()?:0)
                        intent.putExtra("productOwner",data.productOwner?.toInt()?:-1)

                        Log.i("asjfnsjkfnklasfndl","userId- ${data.userId}\nuserName-${data.userName}\nuserProfile-${data.userImage}\nproductId-${data.productId}" +
                                "\nproductMinBid-${data.productMinBid}\nproductImage-${data.productImage}\nproductOwner-${data.productOwner}")
                        startActivity(intent)
                    }

                })
        }
    }
}