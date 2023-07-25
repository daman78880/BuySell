package com.buysell.screens.home

import SharedPref
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentHomeBinding
import com.buysell.screens.browsecategoryresult.BrowseCategoryResultFragment
import com.buysell.screens.category.PojoCategory
import com.buysell.screens.category.PojoCategoryData
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.gson.*
import dagger.hilt.android.AndroidEntryPoint
import hideKeyBoard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.lang.ref.WeakReference


@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private lateinit var AdapterProductShow: ProductShowAdatper
    private lateinit var binding: FragmentHomeBinding
    private lateinit var productList: ArrayList<AllProductsList>
    private val viewModelHome: ViewModelHome by viewModels()
    private var token: String? = null
    private var likeIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        productList = ArrayList()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        Log.i(TAG, "token is ${SharedPref(requireContext()).getString(Constant.TOKEN_kEY)}")
    }
    override fun onStart() {
        super.onStart()
        askNotificationPermission()
//        init()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        token = SharedPref(requireContext()).getString(Constant.TOKEN_kEY)
        BrowseCategoryResultFragment.fromToFilter.priceFrom=null
        BrowseCategoryResultFragment.fromToFilter.priceTo=null
        if(SharedPref(requireContext()).getBoolean(Constant.GUEST_LOGIN_STATUS)) {
            clickListner()
            viewModelHome.getCategoryHome(token!!)
            viewModelHome.hitPostsApii(token!!)
            setSpanable("")
            setToolBar()
        }
        else{
            Log.i(TAG,"getting location api hinting")
            viewModelHome.hitLocationGetHomeApi(token!!)
        }
        updateLocation.observe(requireActivity(), Observer {
            val status=it as Boolean
            if(status){
                Log.i(TAG,"updating location function hinting")
                val lat = SharedPref(requireContext()).getString(Constant.LATITUDE_LOCATION)
                val lng = SharedPref(requireContext()).getString(Constant.LONGITUDE_LOCATION)
                val data=SharedPref(requireContext()).getString(Constant.LOCATION_NAME)
                val jsonObject = JsonObject()
                jsonObject.addProperty("location", data)
                jsonObject.addProperty("longitude", lng)
                jsonObject.addProperty("latitude", lat)
                viewModelHome.hitUpdateLocationHomeApi(token!!, jsonObject)
                Log.i(TAG,"updating location Api hinting")

            }
        })
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
//        CoroutineScope(Dispatchers.IO).launch {
            viewModelHome.locationResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(
                                TAG,
                                "Error ${res?.message} , Error code:${res?.errorCode} , localized msg:${res?.localizedMessage}"
                            )
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is ApiState.Success -> {
                        val response = data.data as JsonObject
                        val ResonsePojoLocation: PojoLocationResonse = Gson().fromJson(response, PojoLocationResonse::class.java)
                        Log.i(TAG,"geting location response")
                        Log.i("asjfbnskajfnsjand ", "Rsponse loation sfnajksdfnjsjkafndajksdfnksndf ${ResonsePojoLocation}")
                            binding.imgTempFolderHome.visibility=View.VISIBLE
                            binding.txtTempFolderMsgHome.visibility=View.VISIBLE
                            binding.productShowListRv.visibility=View.GONE
                        if (ResonsePojoLocation.status == 200) {
                            SharedPref(requireContext()).saveBoolean(Constant.BOTTOM_BAR_CLICK,true)
                            Extentions.stopProgress()
                            productList = ArrayList()
                            if (ResonsePojoLocation.data.longitude != null && ResonsePojoLocation.data.latitude != null && ResonsePojoLocation.data.location != null) {

                                if (ResonsePojoLocation.data.latitude.isNotEmpty() && ResonsePojoLocation.data.longitude.isNotEmpty()
                                    && ResonsePojoLocation.data.location.isNotEmpty()) {
                                    Log.i(TAG,"location have")
                                    binding.txtTempFolderMsgHome.text="No Data"
                                    SharedPref(requireContext()).saveString(Constant.LOCATION_NAME,ResonsePojoLocation.data.location)
                                    SharedPref(requireContext()).saveString(Constant.LATITUDE_LOCATION,ResonsePojoLocation.data.latitude)
                                    SharedPref(requireContext()).saveString(Constant.LONGITUDE_LOCATION,ResonsePojoLocation.data.longitude)
                                    setSpanable(ResonsePojoLocation.data.location)
                                    setToolBar()
                                    viewModelHome.hitPostsApii(token!!)
                                    viewModelHome.getCategoryHome(token!!)
                                } else {
                                    Log.i(TAG,"location not found")
                                    Log.i("asjfbnskajfnsjand ", "Location not found function called")
                                    SharedPref(requireContext()).saveBoolean(Constant.UPDATE_LOCATION,true)
                                    checkLocationSetting()
                                }
                            } else {
                                binding.txtTempFolderMsgHome.text="No Location"

                                Log.i("asbskanbjkajfnbdsa","location empty null")
                                Log.i(TAG,"else else location setting")
                                SharedPref(requireContext()).saveBoolean(Constant.UPDATE_LOCATION,true)
                                checkLocationSetting()
                            }
                        } else {
                            SharedPref(requireContext()).saveBoolean(Constant.BOTTOM_BAR_CLICK,false)

                            Toast.makeText(requireContext(), ResonsePojoLocation.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelHome.locationUpdateResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(
                                TAG,
                                "Error ${res?.message} , Error code:${res?.errorCode} , localized msg:${res?.localizedMessage}"
                            )
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
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
                                viewModelHome.hitLocationGetHomeApi(token!!)
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
            viewModelHome.categoryHomeResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(
                                TAG,
                                "Error ${res?.message} , Error code:${res?.errorCode} , localized msg:${res?.localizedMessage}"
                            )
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
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
                        Log.i(TAG, "category Login sdf  dsuccess ${data.data}")
                        val data: PojoCategory = Gson().fromJson(response, PojoCategory::class.java)
                        if (data.status == 200) {
                            if (data.success) {
                                binding.apply {
                                    categoryListRv.adapter = CategoryProductAdatper(
                                        requireContext(),
                                        data.data,
                                        object : CategoryProductAdatper.Click {
                                            override fun onClick(value: PojoCategoryData) {
                                                if (SharedPref(requireContext()).getBoolean(Constant.GUEST_LOGIN_STATUS)) {
                                                    findNavController().navigate(
                                                        R.id.action_homeFragment_to_guestAccountFragment
                                                    )
                                                } else {
                                                    val bundle = Bundle()
                                                    bundle.putInt("categoryId", value.id)
                                                    bundle.putString("categoryName", value.category)
                                                    findNavController().navigate(
                                                        R.id.action_homeFragment_to_browseCategoryResultFragment2,
                                                        bundle
                                                    )
                                                }
                                            }
                                        })
                                }
                            }
                        } else if (data.status == 201) {
                            Toast.makeText(requireContext(), "${data.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelHome.postsResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {

                        Log.i(TAG, "post api failed")
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

                        Log.i(TAG, "Login sdf  dsuccess ${data.data}")
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        val data: PojoProductShow = Gson().fromJson(response, PojoProductShow::class.java)
                        if (data.status == 200) {
                            if (data.success) {
                                if(data.data.isNotEmpty()){
                                    binding.productShowListRv.visibility=View.VISIBLE
                                    binding.imgTempFolderHome.visibility=View.GONE
                                    binding.txtTempFolderMsgHome.visibility=View.GONE
                                }
                                else{
                                    binding.productShowListRv.visibility=View.GONE
                                    binding.imgTempFolderHome.visibility=View.VISIBLE
                                    binding.txtTempFolderMsgHome.visibility=View.VISIBLE
                                    binding.txtTempFolderMsgHome.text="No data Found"
                                }
                                Log.i("asdfbsdfb","value is data ")
                                productList.addAll(data.data)
                                binding.apply {
                                    AdapterProductShow = ProductShowAdatper(
                                        requireActivity(),
                                        productList,
                                        object : ProductShowAdatper.Click {
                                            override fun onClick(value: AllProductsList) {
                                                if (SharedPref(requireContext()).getBoolean(Constant.GUEST_LOGIN_STATUS)) {
                                                    findNavController().navigate(
                                                        R.id.action_homeFragment_to_guestAccountFragment
                                                    )
                                                }else {
                                                    val bundle = Bundle()
                                                    bundle.putInt("idP", value.id)
                                                    findNavController().navigate(
                                                        R.id.action_homeFragment_to_productDetailFragment,
                                                        bundle
                                                    )
                                                }
                                            }
                                            override fun onLike(
                                                value: AllProductsList,
                                                index: Int
                                            ) {
                                                if (SharedPref(requireContext()).getBoolean(Constant.GUEST_LOGIN_STATUS)) {
                                                    findNavController().navigate(
                                                        R.id.action_homeFragment_to_guestAccountFragment
                                                    )
                                                }else{
                                                likeIndex = index
                                                val jsonObject = JsonObject()
                                                jsonObject.addProperty("id", value.id)
                                                viewModelHome.hitLikePostHomeApi(
                                                    token!!,
                                                    jsonObject
                                                )
                                            }
                                            }
                                        })
                                    binding.productShowListRv.adapter = AdapterProductShow
                                    clickListner()
                                }
                            }
                        } else if (data.status == 201) {
                            Toast.makeText(requireContext(), "${data.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                    else -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
            viewModelHome.likePostHomeResponse.collect { data ->
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
                                if (likeIndex >= 0) {
                                    productList[likeIndex].liked = !productList[likeIndex].liked
                                    likeIndex = -1
                                    binding.productShowListRv.adapter?.notifyDataSetChanged()
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
    }

    private fun clickListner() {
        binding.apply {
            appBarHome.txtLocationHomeToolBar.setOnClickListener {
                val bundle=Bundle()
                bundle.putBoolean("comeFromHome",true)
                findNavController().navigate(R.id.action_homeFragment_to_locationFragment,bundle)
            }
            appBarHome.toolBarHomeToolBarLayout.setOnClickListener {
                requireActivity().hideKeyBoard()
            }
            etSearchHome.setOnClickListener {
                if (SharedPref(requireContext()).getBoolean(Constant.GUEST_LOGIN_STATUS)) {
                    findNavController().navigate(
                        R.id.action_homeFragment_to_guestAccountFragment
                    )
                } else {
                    findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
                }
            }

            viewHome.setOnClickListener {
                requireActivity().hideKeyBoard()
            }
            txtSeeAllHome.setOnClickListener {
                if (SharedPref(requireContext()).getBoolean(Constant.GUEST_LOGIN_STATUS)) {
                    findNavController().navigate(
                        R.id.action_homeFragment_to_guestAccountFragment
                    )
                } else {
                    val tempBundle = Bundle()
                    tempBundle.putBoolean("browse", true)
                    findNavController().navigate(
                        R.id.action_homeFragment_to_categoryFragment2,
                        tempBundle
                    )
                }
            }
        }
    }

    private fun setToolBar() {
        binding.apply {
            appBarHome.cardViewHomeToolbar.cardElevation = 10f
            appBarHome.txtLocationHomeToolBar.gravity = Gravity.START
            appBarHome.txtLocationHomeToolBar.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.blue_Txt
                )
            )
            appBarHome.imgLocationIconHomeToolBar.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_location_on_24
                )
            )
            appBarHome.imgFilterHomeToolBar.visibility=View.GONE
        }
    }
    private fun setSpanable(address: String) {
        val str = address
        val ss = SpannableString(str)
        val span2: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                (textView as AppCompatTextView).highlightColor = ContextCompat.getColor(
                    requireActivity(),
                    android.R.color.transparent
                )
                Toast.makeText(requireContext(), "Clicked", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = ContextCompat.getColor(requireContext(), R.color.blue_Txt)
                textPaint.isUnderlineText = true
            }
        }
        ss.setSpan(span2, 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.appBarHome.txtLocationHomeToolBar.text = ss
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.i("asnasjkfnadsfksnfd","You can show notification granted")
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Log.i("asnasjkfnadsfksnfd","Showing custom dialog and get permission")
            } else {
                Log.i("asnasjkfnadsfksnfd","Request permission")
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }else{
            Log.i("asnasjkfnadsfksnfd","Else build comparision")
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.i("asnasjkfnadsfksnfd","You can show notification")
        } else {
            Log.i("asnasjkfnadsfksnfd","You can't show notification")
        }
    }



}