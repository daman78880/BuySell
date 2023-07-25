package com.buysell.screens.productdetail

import SharedPref
import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.databinding.FragmentProductDetailBinding
import com.buysell.screens.chat.ChatActivity
import com.buysell.screens.chatuser.*
import com.buysell.screens.home.AllProductsList
import com.buysell.screens.home.PojoCategoryProduct
import com.buysell.screens.home.PojoProductShow
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.math.min


@AndroidEntryPoint
class ProductDetailFragment : Fragment() {
    private lateinit var binding: FragmentProductDetailBinding
    private var likeStatus: Boolean = false
    private var apiKey: String? = null

    private lateinit var realtedAdsPostList: ArrayList<AllProductsList>
    private var comingFrom = 0
    private var userData: PojoProductDetail? = null
    private val viewModelProductDetail: ViewModelProductDetail by viewModels()
    private var token: String = ""
    private var myAdsStatus = false
    private var likedIndex = -1
    private var id: Int? = -1

    var firebaseb : FirebaseFirestore?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProductDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.container.onCreate(savedInstanceState)
        binding.model = viewModelProductDetail
        firebaseb= FirebaseFirestore.getInstance()
        token = SharedPref(requireContext()).getString(Constant.TOKEN_kEY)
        realtedAdsPostList = ArrayList()
        apiKey = resources.getString(R.string.googleApi)
        init()
    }

    private fun init() {
        if (arguments != null) {
            id = arguments?.getInt("idP") ?: 0
            myAdsStatus = arguments?.getBoolean("myAds") ?: false
            viewModelProductDetail.hitPostDetailApi(token, id ?: -1)
            binding.apply {
                if (myAdsStatus) {
                    Log.i(TAG, "inside true eye")
                    cLayoutEyeProductDetail.visibility = View.VISIBLE
                    cLayoutHeartProductDetail.visibility = View.VISIBLE
                    cLayoutDetailProductDetail.visibility = View.GONE
                    cLayoutUserProfileProductDetail.visibility = View.GONE
                    cLImgLikeProductDetail.visibility = View.GONE
                    binding.cLayoutRelatedAdsProductDetail.visibility=View.VISIBLE
                    imgHeartProductDetail.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                    txtNoOfPeoperWatchProductDetail.text = userData?.data?.viewCount.toString()
                    txtNoOfLikedCountProductDetail.text = userData?.data?.likeCount.toString()
                    btnAskProductDetails.text = requireActivity().getString(R.string.delete)
                    btnMakeOfferProductDetails.text = requireActivity().getString(R.string.edit)
                }
                else {
                    cLayoutUserProfileProductDetail.visibility = View.VISIBLE
                    cLayoutEyeProductDetail.visibility = View.GONE
                    cLayoutHeartProductDetail.visibility = View.GONE
                    cLImgLikeProductDetail.visibility = View.VISIBLE
                    btnAskProductDetails.text = requireActivity().getString(R.string.ask)
                    btnMakeOfferProductDetails.text =
                        requireActivity().getString(R.string.make_offer)
                }
            }
        } else {
            Log.i(TAG, "init: else part")
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelProductDetail.postDetailResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        binding.nestedScrollViewProductDetail.visibility = View.GONE
                        binding.cardBottomButtonProductDetails.visibility = View.GONE
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        binding.nestedScrollViewProductDetail.visibility = View.GONE
                        binding.cardBottomButtonProductDetails.visibility = View.GONE
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(TAG, "Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.i(TAG, "else Error ${data.msg}")
                            Toast.makeText(
                                requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        val data: PojoProductDetail =
                            Gson().fromJson(response, PojoProductDetail::class.java)
                        if (data.status == 200) {
                            Log.i(TAG, "Return data is ${data.data}")
                            if (!myAdsStatus) {
                                viewModelProductDetail.hitRelatedAdsPostApi(
                                    token,
                                    data.data.categoryId.toInt(),
                                    data.data.id
                                )
                            }
                            userData = data
                            Log.i("asfdkjnasjkdf","response is $userData")

                            binding.container.getMapAsync { googleMap->
                                Log.i(TAG,"map asynced")
                                if(userData?.data?.latitude != null && userData?.data?.longitude != null) {
                                    googleMap.clear()
                                    try {
                                        val circleOptions = CircleOptions()
                                            .center(
                                                LatLng(
                                                    userData!!.data.latitude!!.toDouble(),
                                                    userData!!.data.longitude!!.toDouble()
                                                )
                                            )
                                            .radius(400.0)
                                            .fillColor(0x40ff0000)
                                            .strokeColor(Color.RED)
                                            .strokeWidth(2f)
                                        googleMap.addCircle(circleOptions)
                                        val markerOptions = MarkerOptions()
                                        markerOptions.position(
                                            LatLng(
                                                userData!!.data.latitude!!.toDouble(),
                                                userData!!.data.longitude!!.toDouble()
                                            )
                                        )
                                        markerOptions.title(userData?.data?.title.toString())
                                        googleMap.addMarker(markerOptions)

                                        googleMap.mapType=GoogleMap.MAP_TYPE_NORMAL
                                        googleMap.uiSettings.isMapToolbarEnabled = true

                                        googleMap.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(
                                                    userData!!.data.latitude!!.toDouble(),
                                                    userData!!.data.longitude!!.toDouble()
                                                ),
                                                13F
                                            )
                                        )

                                        Log.i(TAG, "onMap ready lat ${userData!!.data.latitude!!.toDouble()} lng${userData!!.data.longitude!!.toDouble()}")

                                    }
                                    catch (e:Exception){
                                        Log.i(TAG,"error during loading map ${e.message}")
                                    }
                                }
                                else{
                                    Log.i(TAG,"null lat long ${userData?.data?.latitude} and long ${userData?.data?.longitude}")
                                }

                                googleMap.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
                                    override fun onMapClick(p0: LatLng) {
                                        Log.i(TAG, "On Click location is ${p0}")
//                addGeoFence(p0.latitude, p0.longitude)
                                    }
                                })
                            }
//                            binding.container.parent.setOnCameraMoveStartedListener{
//
//                            }
//                            binding.container.parent.requestDisallowInterceptTouchEvent(true)
//                            binding.container.parent.requestDisallowInterceptTouchEvent(false)


                            }
                            Log.i(TAG,"userdata $userData")
                            likeStatus = userData?.data?.liked ?: false
                            setLikeStatus()
                            setLayout()
                            setProductImages()
                            binding.nestedScrollViewProductDetail.visibility = View.VISIBLE
                            binding.cardBottomButtonProductDetails.visibility = View.VISIBLE
                            clickListeners()
                            if (!myAdsStatus) {
                                if (null != userData?.data?.isAlreadyView ) {
                                    if(userData!!.data.isAlreadyView==false) {
                                        val jsonObject = JsonObject()
                                        jsonObject.addProperty("id", userData!!.data.id)
                                        viewModelProductDetail.hitIncrementPostApi(
                                            token,
                                            jsonObject
                                        )
                                    }
                                }
                            }
                        }


                    is ApiState.Empty -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelProductDetail.incrementViewResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(TAG, "Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.i(TAG, "else Error ${data.msg}")
                            Toast.makeText(
                                requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        Log.i(TAG, "increment success ${response}")
                        if (response.has("status")) {
                            if (response.get("status").toString().toInt() == 200) {
                                Log.i(TAG, "increment")
                            }
                            else {
                                Toast.makeText(requireContext(), "${response.get("message")}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelProductDetail.likePostResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(TAG, "Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.i(TAG, "else Error ${data.msg}")
                            Toast.makeText(
                                requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        if (response.has("status")) {
                            if (response.get("status").toString().toInt() == 200) {
                                likeStatus = !likeStatus
                                setLikeStatus()

                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "${response.get("message")}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    is ApiState.Empty -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelProductDetail.likeRelatedAdsPostResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(TAG, "Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.i(TAG, "else Error ${data.msg}")
                            Toast.makeText(
                                requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        if (response.has("status")) {
                            if (response.get("status").toString().toInt() == 200) {
                                if (likedIndex >= 0) {
                                    realtedAdsPostList[likedIndex].liked =
                                        !realtedAdsPostList[likedIndex].liked
                                    binding.rvRelatedAdsProductDetails.adapter?.notifyItemChanged(
                                        likedIndex
                                    )
                                    likedIndex = -1
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "${response.get("message")}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    is ApiState.Empty -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelProductDetail.deletePostResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(TAG, "Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.i(TAG, "else Error ${data.msg}")
                            Toast.makeText(
                                requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        if (response.has("status")) {
                            if (response.get("status").toString().toInt() == 200) {
                                findNavController().popBackStack()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "${response.get("message")}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelProductDetail.relatedAdsPostResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(TAG, "Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.i(TAG, "else Error ${data.msg}")
                            Toast.makeText(
                                requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject

                        val data: PojoProductShow =
                            Gson().fromJson(response, PojoProductShow::class.java)
                        if (data.status == 200) {
                            if (data.data.isNotEmpty()) {
                                binding.rvRelatedAdsProductDetails.visibility = View.VISIBLE
                                binding.txtTempRelatedAdsProductDetail.visibility = View.VISIBLE
                            }
                            realtedAdsPostList.clear()
                            realtedAdsPostList.addAll(data.data)
                            val displayMetrics = DisplayMetrics()
                            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
                            val width = displayMetrics.widthPixels
                            binding.rvRelatedAdsProductDetails.adapter = RelatedProductAdatper(
                                requireContext(),
                                width,
                                realtedAdsPostList,
                                object : RelatedProductAdatper.Click {
                                    override fun onClick(value: AllProductsList) {
                                        val bundle = Bundle()
                                        bundle.putInt("idP", value.id)
                                        findNavController().navigate(
                                            R.id.action_productDetailFragment_self,
                                            bundle
                                        )
                                    }

                                    override fun onLikeClick(
                                        value: AllProductsList,
                                        position: Int
                                    ) {
                                        likedIndex = position
                                        val jsonObject = JsonObject()
                                        jsonObject.addProperty("id", userData?.data?.id)
                                        viewModelProductDetail.hitRelatedLikePostApi(
                                            token,
                                            jsonObject
                                        )
                                    }
                                })
                        } else {
                            Toast.makeText(requireContext(), "${data.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }

    }

    private fun clickListeners() {
        binding.apply {
            imgBackProductDetail.setOnClickListener {
                findNavController().popBackStack()
            }

            cLImgLikeProductDetail.setOnClickListener {
                val jsonObject = JsonObject()
                jsonObject.addProperty("id", userData?.data?.id)
                viewModelProductDetail.hitLikePostApi(token, jsonObject)
            }
            btnMakeOfferProductDetails.setOnClickListener {
                val makeOffer = requireActivity().resources.getString(R.string.make_offer)
                if (btnMakeOfferProductDetails.text == makeOffer) {
                    makeAnOfferDialog()
                } else {
                    val bundle=Bundle()
                    bundle.putInt("categoryId",userData?.data?.categoryId?.toInt()?:-1)
                    bundle.putInt("id",userData?.data?.id?:-1)
                    if(userData?.data?.categoryId.toString().toInt()  == 2 ||  userData?.data?.categoryId.toString().toInt()  == 3){
                        findNavController().navigate(R.id.action_productDetailFragment_to_carBikeFormFragment,bundle)
                    }else{
                        findNavController().navigate(R.id.action_productDetailFragment_to_commonMobileFormFragment,bundle)
                    }
                }
            }
            btnAskProductDetails.setOnClickListener {
                if (btnAskProductDetails.text == requireActivity().getString(R.string.delete)) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("id", userData?.data?.id)
                    viewModelProductDetail.hitDeletePostApi(token, jsonObject)

                } else {

                    if(userData!=null){
                        if(findNavController().currentDestination?.id == R.id.productDetailFragment){
                            val intent = Intent(this@ProductDetailFragment.requireContext(), ChatActivity::class.java)
                            Log.i("asjfnsjkfnklasfndl","data is $userData")
                            intent.putExtra("userId",userData?.data?.userId?:0)
                            intent.putExtra("userName",userData?.data?.User?.name?:"")
                            intent.putExtra("userImage",userData?.data?.User?.profileUrl?:"")
                            intent.putExtra("productOwner",userData?.data?.userId?:0)
                            intent.putExtra("productId",userData?.data?.id?:0)
                            intent.putExtra("productMinBid",userData?.data?.minBid?:0)
                            intent.putExtra("productPrice",userData?.data?.price?:0)
                            var image=""
                            if(userData?.data?.images != null && userData?.data?.images?.size?:0 > 0) {
                                image = userData?.data?.images?.get(0)?.images ?: ""
                            }

                            intent.putExtra("productImage",image?:"")

//                            Log.i("asjfnsjkfnklasfndl","userId- ${userData?.data?.userId}\nuserName-${userData?.data?.User?.name}\nuserProfile-${userData?.data?.User?.profileUrl}\nproductId-${userData?.data?.id}" +
//                                    "\nproductMinBid-${userData?.data?.minBid}\nproductImage-${userData?.data?.images?.get(0)?.images}\nproductOwner-${userData?.data?.userId}")
                            startActivity(intent)
                        }
                    }
                }
            }
            txtSeeProfileProductDetail.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("userId", userData?.data?.userId ?: -1)
                bundle.putInt("id", userData?.data?.id ?: -1)
                findNavController().navigate(
                    R.id.action_productDetailFragment_to_userProfileFragment,
                    bundle
                )
            }
        }
    }
    private fun makeAnOfferDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_make_an_offer)
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
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
        val btnCancelDialog = dialog.findViewById<AppCompatImageView>(R.id.btnCancelMakeAnOfferDialog)
        btnMakeOffer.setOnClickListener {
            val value=etEnterAmount.text?.trim()
            var minBid=userData?.data?.minBid
            if (minBid == null || minBid.isEmpty()){
                minBid= "0"
            }
            if (value?.isNotEmpty()!!) {
                Log.i("asfdkjnasjkdf","value is $value and minBid ${minBid}")
                if(value.toString().toInt() > minBid.toString().toInt()){
                Log.i(TAG, "Make offer Amount : ${etEnterAmount.text}")
                    if(findNavController().currentDestination?.id == R.id.productDetailFragment){
                        dialog.dismiss()
                        val intent = Intent(this@ProductDetailFragment.requireContext(), ChatActivity::class.java)
                        intent.putExtra("loginBid",value.toString().toInt())
                        intent.putExtra("userId",userData?.data?.userId?:0)
                        intent.putExtra("userName",userData?.data?.User?.name?:"")
                        intent.putExtra("userImage",userData?.data?.User?.profileUrl?:"")
                        intent.putExtra("productOwner",userData?.data?.userId?:0)
                        intent.putExtra("productId",userData?.data?.id?:0)
                        intent.putExtra("productMinBid",userData?.data?.minBid?:0)
                        intent.putExtra("productPrice",userData?.data?.price?:0)
                        try {
                            intent.putExtra(
                                "productImage",
                                userData?.data?.images?.get(0)?.images ?: ""
                            )
                        }
                        catch (e:Exception){
                            Log.i(TAG,"error in photo get ${e.message}")
                            intent.putExtra(
                                "productImage",
                                 ""
                            )
                        }
//                        Log.i("asjfnsjkfnklasfndl","userId- ${userData?.data?.userId}\nuserName-${userData?.data?.User?.name}\nuserProfile-${userData?.data?.User?.profileUrl}\nproductId-${userData?.data?.id}" +
//                                "\nproductMinBid-${userData?.data?.minBid}\nproductImage-${userData?.data?.images?.get(0)?.images}\nproductOwner-${userData?.data?.userId}\nbid$value")
                        startActivity(intent)
                    }

                }
                else{
                    Toast.makeText(requireContext(), "Enter high amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter amount", Toast.LENGTH_SHORT).show()
            }
        }
        btnCancelDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setLikeStatus() {
        if (likeStatus) {
            binding.imgLikeProductDetail.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            )
        } else {
            binding.imgLikeProductDetail.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
    }

    private fun showBrand(status: Boolean) {
        binding.apply {
            if (status) {

                txtTempBrandProductDetail.visibility = View.VISIBLE
                txtBrandProductDetail.visibility = View.VISIBLE
                txtBrandProductDetail.text = userData?.data?.brand ?: ""
            } else {
                txtTempBrandProductDetail.visibility = View.GONE
                txtBrandProductDetail.visibility = View.GONE
            }

        }
    }

    private fun showYear(status: Boolean) {
        binding.apply {
            if (status) {
                txtTempYearProductDetail.visibility = View.VISIBLE
                txtYearProductDetail.visibility = View.VISIBLE
                txtYearProductDetail.text = userData?.data?.year ?: ""
            } else {
                txtTempYearProductDetail.visibility = View.GONE
                txtYearProductDetail.visibility = View.GONE
            }
        }
    }

    private fun showFuel(status: Boolean) {
        binding.apply {
            if (status) {
                txtFuelProductDetail.visibility = View.VISIBLE
                txtTempFuelProductDetail.visibility = View.VISIBLE
                txtFuelProductDetail.text = userData?.data?.fuel ?: ""
            } else {
                txtFuelProductDetail.visibility = View.GONE
                txtTempFuelProductDetail.visibility = View.GONE
            }
        }
    }

    private fun showTransmission(status: Boolean) {
        binding.apply {
            if (status) {
                txtTempTransmissionProductDetail.visibility = View.VISIBLE
                txtTransmissionProductDetail.text = userData?.data?.transmission ?: ""
                txtTransmissionProductDetail.visibility = View.VISIBLE
            } else {
                txtTempTransmissionProductDetail.visibility = View.GONE
                txtTransmissionProductDetail.visibility = View.GONE
            }
        }
    }

    private fun showkmDrive(status: Boolean) {
        binding.apply {
            if (status) {
                txtKmDriveProductDetail.visibility = View.VISIBLE
                txtKmDriveProductDetail.text = userData?.data?.kmDriven ?: ""
                txtTempKmDriveProductDetail.visibility = View.VISIBLE
            } else {
                txtKmDriveProductDetail.visibility = View.GONE
                txtTempKmDriveProductDetail.visibility = View.GONE
            }
        }
    }

    private fun showNoOfOwner(status: Boolean) {
        binding.apply {
            if (status) {
                txtNoOfOwnderProductDetail.visibility = View.VISIBLE
                txtTempNoOfOwnderProductDetail.visibility = View.VISIBLE
                txtNoOfOwnderProductDetail.text = userData?.data?.numberOfowners ?: ""
            } else {
                txtNoOfOwnderProductDetail.visibility = View.GONE
                txtTempNoOfOwnderProductDetail.visibility = View.GONE
            }
        }
    }

    private fun setUserDetails() {
        binding.apply {
            if (null != userData?.data) {
                txtTitleProductDetail.text = userData?.data?.title?:""
                txtAddressProductDetail.text = userData?.data?.location?:""
                txtPriceProductList.text = Extentions.formatPricee(userData?.data?.price?.toLong()?:0)
                txtTimePostedProductDetail.text =
                    Extentions.getPostAgoTime(userData?.data?.createdAt ?: "")
                txtDescriptionProductDetail.text = userData?.data?.description?:""
                txtUserNameProductDetail.text = userData?.data?.User?.name?:""
                txtUserMemberSinceProductDetail.text = userData?.data?.User?.memberSince?:""
                if (null != (userData!!.data.User)) {
                    if(userData!!.data.User?.profileUrl?.isNotEmpty() == true) {
                        Glide.with(requireContext()).load(userData?.data?.User?.profileUrl ?: "")
                            .into(imgUserImageProductDetail)
                    }
                    else {
                        Glide.with(requireContext())
                            .load(ContextCompat.getDrawable(requireContext(), R.drawable.no_img_found))
                            .into(imgUserImageProductDetail)
                    }
                }
                if (myAdsStatus) {
                    txtNoOfPeoperWatchProductDetail.text = userData?.data?.viewCount?.toString()?:""
                    txtNoOfLikedCountProductDetail.text = userData?.data?.likeCount?.toString()?:""
                }
            }
        }
    }


    private fun setLayout() {
        binding.apply {

            // common  show only // end side show
            comingFrom = userData?.data?.categoryId.toString().toInt()
            setUserDetails()

            if (comingFrom in 4..12) {
                cLayoutDetailProductDetail.visibility = View.GONE
            }
            // For mobile single brand show
            else if (comingFrom == 1) {

                cLayoutDetailProductDetail.visibility = View.VISIBLE
                showBrand(true)
                showYear(false)
                showFuel(false)
                showTransmission(false)
                showkmDrive(false)
                showNoOfOwner(false)
            }
            // for bike  show only brand ,year,kmDrive
            else if (comingFrom == 3) {
                cLayoutDetailProductDetail.visibility = View.VISIBLE
                showBrand(true)
                showYear(true)
                showFuel(true)
                showTransmission(false)
                showkmDrive(false)
                showNoOfOwner(false)
            }
            // for car show only brand ,year,fuel,transmission,kmdirve,no.Owner
            else if (comingFrom == 2) {
                cLayoutDetailProductDetail.visibility = View.VISIBLE
                showBrand(true)
                showYear(true)
                showFuel(true)
                showTransmission(true)
                showkmDrive(true)
                showNoOfOwner(true)
            }

        }
    }

    private fun setProductImages() {
        binding.apply {
            val listt = userData?.data?.images
            if (null != listt && listt.isNotEmpty()) {
                viewpagerProductDetail.adapter = ProductDetailPagerAdatper(requireContext(), listt)
                tabDots.setupWithViewPager(viewpagerProductDetail, true)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        binding.container.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.container.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.container.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        binding.container.onDestroy()
    }

}