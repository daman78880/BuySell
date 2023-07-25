package com.buysell.screens.search_screen

import SharedPref
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentSearchBinding
import com.buysell.screens.home.HomeFragment
import com.buysell.screens.home.PojoLocationResonse
import com.buysell.screens.home.ViewModelHome
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import hideKeyBoard
import retrofit2.HttpException
import setToolBar
import java.io.File

@AndroidEntryPoint
class SearchFragment : BaseFragment() {
    private lateinit var binding:FragmentSearchBinding
    private val viewModelSearch:ViewModelSearch by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding= FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    private fun init(){
        if (!Places.isInitialized()) {
            val mapKey = requireActivity().resources.getString(R.string.googleApi)
            Places.initialize(requireActivity().applicationContext, mapKey)
        }
        Places.createClient(requireActivity().applicationContext)
        val appBarTitle=resources.getString(R.string.search)
        setToolBar(binding.appBarSearch,requireContext(),true,false,appBarTitle,10f)
        clickListners()
        locationAddress.observe(requireActivity(), Observer {
            val data = it as String
            Extentions.stopProgress()
            if (data.isNotEmpty()) {
                binding.txtLocationSearch.setText(data)
            }
        })
        binding.etSearch.doOnTextChanged { text, start, before, count ->
            if(text!= null && text.isNotEmpty()) {
                val jsonObject = JsonObject()
                jsonObject.addProperty("search", text.toString())
                viewModelSearch.hitSearchApi(SharedPref(requireContext()).getString(Constant.TOKEN_kEY),jsonObject)
            }else{
                setList(ArrayList<String>())
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelSearch.postsResponseSearch.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
//                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
//                        Extentions.stopProgress()
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
                        val responsePojoSearchTitle: PojoSearchTitleResult =
                            Gson().fromJson(response, PojoSearchTitleResult::class.java)
//                        Extentions.stopProgress()
                        if (responsePojoSearchTitle.status == 200) {
                    val list=ArrayList<String>()
                            for(i in 0 until responsePojoSearchTitle.data.size){
                                if(!list.contains(responsePojoSearchTitle.data[i].title)){
                                    list.add(responsePojoSearchTitle.data[i].title)
                                }
                            }
                            setList(list)
                        } else {
                            Toast.makeText(requireContext(), responsePojoSearchTitle.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
    }
    private fun setList(list:List<String>){
        binding.apply {

            binding.rvSearchResultSearch.adapter=ArrayAdapter<String>(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, list);
            binding.rvSearchResultSearch.setOnItemClickListener(object:AdapterView.OnItemClickListener{
                override fun onItemClick(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    val bundle=Bundle()
                    bundle.putString("title",list[position])
                    findNavController().navigate(R.id.action_searchFragment_to_searchResultFragment,bundle)
                }
            })

        }
    }

    private fun clickListners() {
        binding.apply {
         viewSearch.setOnClickListener {
             requireActivity().hideKeyBoard()
         }
            txtAutoFillLocationSearch.setOnClickListener {
                checkLocationSetting()
            }
            txtLocationSearch.setOnClickListener {
                val fields: List<Place.Field> = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS_COMPONENTS
                )
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(requireContext())
                locationResultSuggestion.launch(intent)
            }
        }
    }
    private val locationResultSuggestion = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(result.data)
                val id = place.id
                val name = place.name
                val aaddress = place.address
                val llat = place.latLng?.latitude
                val llng = place.latLng?.longitude
                val subAddress = place.addressComponents?.asList()
                Log.i(TAG, "sub address\n$subAddress")
                var tempAdress = ""
                for (i in 0 until subAddress?.size!!) {
                    if (subAddress[i].types[0] == "sublocality_level_1") {
                        tempAdress += "${subAddress[i].name},"
                    }
                    if (subAddress[i].types[0] == "locality") {
                        tempAdress += subAddress[i].shortName
                    }
                }
                Log.i(TAG, "temp location is $tempAdress")
                Log.i(TAG, "sub adress \n\n$subAddress\n\n")
                SharedPref(requireContext()).saveString(Constant.LOCATION_NAME, tempAdress)
                SharedPref(requireContext()).saveString(Constant.LATITUDE_LOCATION, llat.toString())
                SharedPref(requireContext()).saveString(Constant.LONGITUDE_LOCATION, llng.toString())
                binding.txtLocationSearch.text = tempAdress
                Log.i(TAG, "id $id , name $name\naddress $aaddress , lat $llat ,lng $llng\nAdress subadress ${subAddress.get(0)?.name},Adress locatility ${subAddress.get(1)?.name}")
            } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status: Status = Autocomplete.getStatusFromIntent(result.data)
                status.statusMessage?.let { Log.i(TAG, it) }
                Log.i(TAG, "eror is ${status.statusMessage}")
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "Cancled")
            }
    }



}