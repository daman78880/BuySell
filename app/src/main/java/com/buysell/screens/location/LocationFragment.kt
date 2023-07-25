package com.buysell.screens.location

import SharedPref
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentLocationBinding
import com.buysell.screens.productdetail.PojoProductDetail
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import hideKeyBoard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import setToolBar
import java.io.File


@AndroidEntryPoint
class LocationFragment : BaseFragment(), OnCompleteListener<Location> {
    private lateinit var binding: FragmentLocationBinding
    private var previousBundle: Bundle? = null
    private val viewModelLocation: ViewModelLocation by viewModels()
    private var uploadImageList: ArrayList<MultipartBody.Part?>? = null
    private var token: String? = null
    private var comeFromHome: Boolean? = null
//    private var passDataClass: PojoProductDetail?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLocationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        if (!Places.isInitialized()) {
            val mapKey=requireActivity().resources.getString(R.string.googleApi)
            Places.initialize(requireActivity().applicationContext, mapKey!!)
        }
       Places.createClient(requireActivity().applicationContext)
    }

    private fun init() {


        if (arguments != null)
            previousBundle = arguments

        comeFromHome=arguments?.getBoolean("comeFromHome")
        if(comeFromHome!=null && comeFromHome==true){
            binding.btnPostLocationLocation.text="ChangeLocation"
        }


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            passDataClass=previousBundle?.getParcelable("passDataClass", PojoProductDetail::class.java)
//        }
//        else{
//            passDataClass=previousBundle?.getParcelable("passDataClass")
//        }
//
//
//        if(passDataClass!=null && passDataClass?.data?.title?.isNotEmpty()!!)
//        {
//            Log.i(TAG,"inside hiting")
//           viewModelLocation.location.set(passDataClass!!.data.location)
//        }

        val list = previousBundle?.getStringArrayList("images")
        token = SharedPref(requireContext()).getString(Constant.TOKEN_kEY)
        uploadImageList = ArrayList()
        Log.e(TAG, list.toString())
        if (list != null) {
            if (list.isNotEmpty()) {
                for (i in 0 until list.size) {
                    val file = File(list[i].toString())
                    val requestFile: RequestBody =
                        file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    val body: MultipartBody.Part =
                        MultipartBody.Part.createFormData("images", file.name, requestFile)
                    uploadImageList!!.add(body)
                }
            }
        }
        val appBarTitle = requireActivity().resources.getString(R.string.locationTxt)
        setToolBar(binding.appBarLocation, requireContext(), true, false, appBarTitle, 10f)
        clickListener()
        binding.apply {
            model = viewModelLocation
            viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
                viewModelLocation.addPostResponse.collect { data ->
                    when (data) {
                        is ApiState.Loading -> {
                            Extentions.showProgress(requireContext())
                        }
                        is ApiState.Failure -> {
                            Extentions.stopProgress()
                            if (data.msg is HttpException) {
                                val res = Extentions.getFailedMsg(data.msg)
                                Log.i(TAG,
                                    "Error ${res?.message} , Error code:${res?.errorCode} , localized msg:${res?.localizedMessage}")
                                Toast.makeText(requireContext(),
                                    "${res?.message}",
                                    Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Log.i(TAG, "Error else ${data.msg}")
                                Toast.makeText(requireContext(),
                                    "Failed due to ${data.msg}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                        is ApiState.Success -> {
                            Extentions.stopProgress()
                            val response = data.data as JsonObject
                            Log.i(TAG, "response $response")
                            if (response.has("status")) {
                                if (response.get("status").toString().toInt() == 200) {
                                    findNavController().navigate(R.id.action_locationFragment_to_homeFragment)
                                } else {
                                    Toast.makeText(requireContext(),
                                        "${response.get("message")}",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        is ApiState.Empty -> {

                        }
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
                viewModelLocation.updatePostResponse.collect { data ->
                    when (data) {
                        is ApiState.Loading -> {
                            Extentions.showProgress(requireContext())
                        }
                        is ApiState.Failure -> {
                            Extentions.stopProgress()
                            if (data.msg is HttpException) {
                                val res = Extentions.getFailedMsg(data.msg)
                                Log.i(TAG,
                                    "Error ${res?.message} , Error code:${res?.errorCode} , localized msg:${res?.localizedMessage}")
                                Toast.makeText(requireContext(),
                                    "${res?.message}",
                                    Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Log.i(TAG, "Error else ${data.msg}")
                                Toast.makeText(requireContext(),
                                    "Failed due to ${data.msg}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                        is ApiState.Success -> {
                            Extentions.stopProgress()
                            val response = data.data as JsonObject
                            Log.i(TAG, "response $response")
                            if (response.has("status")) {
                                if (response.get("status").toString().toInt() == 200) {
                                    findNavController().popBackStack()
                                } else {
                                    Toast.makeText(requireContext(),
                                        "${response.get("message")}",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        is ApiState.Empty -> {

                        }
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModelLocation.updateLocationResponse.collect { data ->
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


            locationAddress.observe(requireActivity(), androidx.lifecycle.Observer {
                val dat= it as String
                if(dat.isNotEmpty()){
                    Log.i(TAG,"Return response is $dat")
                    viewModelLocation.location.set(dat)
                    if (comeFromHome!= null && comeFromHome == true){
                       Log.i(TAG,"come from home and get auto location function runed")
                    }
                    else {
                        previousBundle?.putString(
                            Constant.LOCATION_NAME,
                            SharedPref(requireContext()).getString(Constant.LOCATION_NAME)
                        )
                        previousBundle?.putString(
                            Constant.LATITUDE_LOCATION,
                            SharedPref(requireContext()).getString(Constant.LATITUDE_LOCATION)
                        )
                        previousBundle?.putString(
                            Constant.LONGITUDE_LOCATION,
                            SharedPref(requireContext()).getString(Constant.LONGITUDE_LOCATION)
                        )
                    }
                }
            })
        }

    }

    private fun clickListener() {
        binding.apply {

            btnPostLocationLocation.setOnClickListener {
                if(comeFromHome!=null && comeFromHome ==true){
                    val lat = SharedPref(requireContext()).getString(Constant.LATITUDE_LOCATION)
                    val lng = SharedPref(requireContext()).getString(Constant.LONGITUDE_LOCATION)
                    val data=SharedPref(requireContext()).getString(Constant.LOCATION_NAME)
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("location", data)
                    jsonObject.addProperty("longitude", lng)
                    jsonObject.addProperty("latitude", lat)
                    viewModelLocation.hitUpdateLocationLocationApi(token!!,jsonObject)
                }
                else {
                    Log.i(
                        TAG,
                        "Location is ${SharedPref(requireContext()).getString(Constant.LOCATION_NAME)} lat ${
                            SharedPref(requireContext()).getString(Constant.LATITUDE_LOCATION)
                        } lng ${SharedPref(requireContext()).getString(Constant.LONGITUDE_LOCATION)}"
                    )
                    if (viewModelLocation.location.get().toString().isNotEmpty()) {
                        val locationTemp =
                            SharedPref(requireContext()).getString(Constant.LOCATION_NAME)
                        val latTemp =
                            SharedPref(requireContext()).getString(Constant.LATITUDE_LOCATION)
                        val lngTemp =
                            SharedPref(requireContext()).getString(Constant.LONGITUDE_LOCATION)
                        val categoryId = previousBundle?.getInt("categoryId") ?: -1
                        val title = RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            previousBundle?.getString("title") ?: ""
                        )
                        val description = RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            previousBundle?.getString("description") ?: ""
                        )
                        val price = RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            previousBundle?.getString("price") ?: ""
                        )
                        val minBid = RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            previousBundle?.getString("minBid") ?: ""
                        )
                        val location =
                            RequestBody.create("text/plain".toMediaTypeOrNull(), locationTemp ?: "")
                        val latitude =
                            RequestBody.create("text/plain".toMediaTypeOrNull(), latTemp ?: "")
                        val longitude =
                            RequestBody.create("text/plain".toMediaTypeOrNull(), lngTemp ?: "")
                        val brand = RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            previousBundle?.getString("brand") ?: ""
                        )
                        val year = RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            previousBundle?.getString("year") ?: ""
                        )
                        val kmDrive = RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            previousBundle?.getString("kmDrive") ?: ""
                        )
                        val fuel = RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            previousBundle?.getString("fuel") ?: ""
                        )
                        val transmission = RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            previousBundle?.getString("transmission") ?: ""
                        )
                        val noOfOwner = RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            previousBundle?.getString("noOfOwner") ?: ""
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            val jsonObject = JsonObject()
                            jsonObject.addProperty("location", locationTemp)
                            jsonObject.addProperty("longitude", latTemp)
                            jsonObject.addProperty("latitude", lngTemp)
//                        viewModelLocation.hitUpdateLocationApi(token!!, jsonObject)
//                        if (passDataClass?.data?.title?.isNotEmpty() == true) {
//                            viewModelLocation.hitUpdatePostApi(
//                                token!!,
//                                categoryId,
//                                title,
//                                description,
//                                price,
//                                minBid,
//                                location,
//                                latitude,
//                                longitude,
//                                brand,
//                                year,
//                                fuel,
//                                kmDrive,
//                                transmission,
//                                noOfOwner,
//                                uploadImageList!!
//                            )
//                        } else {
                            viewModelLocation.hitAddPostApi(
                                token!!,
                                categoryId,
                                title,
                                description,
                                price,
                                minBid,
                                location,
                                latitude,
                                longitude,
                                brand,
                                year,
                                fuel,
                                kmDrive,
                                transmission,
                                noOfOwner,
                                uploadImageList!!
                            )
//                        }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please select location",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
                txtAutoFillLocationLocation.setOnClickListener {
//                CoroutineScope(Dispatchers.Main).launch {
//                    Extentions.showProgress(requireContext())
                    checkLocationSetting()
//                }
                }
                txtLocationLocation.setOnClickListener {
                    Log.i(TAG,"clicked")
                    val fields: List<Place.Field> = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                            .build(requireContext())
                    startActivityForResult(intent, 7874)
                }
                viewLocationLocation.setOnClickListener {
                    requireActivity().hideKeyBoard()
                }

        }
    }
     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 7874) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data)
                val id=place.id
                val name=place.name
                val aaddress=place.address
                val llat= place.latLng?.latitude
                val llng= place.latLng?.longitude
                if (llat!=null && llng!=null){
                    Log.i("sajfnjkasdfn","location not null lat$llat lng$llng")
                    SharedPref(requireContext()).saveString(Constant.LATITUDE_LOCATION,llat.toString())
                    SharedPref(requireContext()).saveString(Constant.LONGITUDE_LOCATION,llat.toString())
                }else{
                    Log.i("sajfnjkasdfn","location latlong null")
                }
                val subAddress= place.addressComponents?.asList()
                val sub= place.attributions
                val subSize=subAddress?.size?:-1
                var tempAdress=""
                Log.i("sajfnjkasdfn","place ${place.address}\nsubAdress${subAddress}  \nattribution $sub \nname$name")

                if(subSize>-1){
                    Log.i("sajfnjkasdfn","inside for loop")
                    for(i in 0 until subSize){
                    if((subAddress?.get(i)?.types?.get(0)?:"") == "sublocality_level_1"){
                        tempAdress+="${subAddress?.get(i)?.name},"
                    }
                    if((subAddress?.get(i)?.types?.get(0) ?: "") == "locality"){
                        tempAdress+= subAddress?.get(i)?.shortName
                    }
                }}
                else{
                    tempAdress=name
                    Log.i("sajfnjkasdfn","else part")
                }
                Log.i(TAG,"temp location is $tempAdress")
                Log.i(TAG,"sub adress \n\n$subAddress\n\n")
                SharedPref(requireContext()).saveString(Constant.LOCATION_NAME,tempAdress)
                SharedPref(requireContext()).saveString(Constant.LATITUDE_LOCATION,llat.toString())
                SharedPref(requireContext()).saveString(Constant.LONGITUDE_LOCATION,llng.toString())
                viewModelLocation.location.set(tempAdress)
                Log.i(TAG,"id $id , name $name\naddress $aaddress , lat $llat ,lng $llng\nAdress subadress ${subAddress?.get(0)?.name},Adress locatility ${subAddress?.get(1)?.name}")
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status: Status = Autocomplete.getStatusFromIntent(data)
                status.getStatusMessage()?.let { Log.i(TAG, it) }
                Log.i(TAG,"eror is ${status.statusMessage}")
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG,"Cancled")
                // The user canceled the operation.
            }
        }
         if(requestCode==123){
                 when (resultCode) {
                     Activity.RESULT_OK -> {
                         Log.i(TAG,"on gps")
                         requestPermssionForLocation()
                     }
                     Activity.RESULT_CANCELED -> {
                         Log.i(TAG,"cancel on gps")
                     }
                 }
         }
    }

}