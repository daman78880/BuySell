package com.buysell.screens.filter

import SharedPref
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentFilterBinding
import com.buysell.screens.browsecategoryresult.BrowseCategoryResultFragment
import com.buysell.utils.Extentions.TAG
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import hideKeyBoard
import setToolBar


class FilterFragment : BaseFragment() {
    private lateinit var binding: FragmentFilterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFilterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {

        if (!Places.isInitialized()) {
            val mapKey = requireActivity().resources.getString(R.string.googleApi)
            Places.initialize(requireActivity().applicationContext, mapKey)
        }
        Places.createClient(requireActivity().applicationContext)
        val appBarTitle = requireActivity().resources.getString(R.string.filter)
        setToolBar(binding.appBarFilter, requireContext(), true, false, appBarTitle, 10f)
        clickListeners()

        locationAddress.observe(requireActivity(), androidx.lifecycle.Observer {
            val dat = it as String
            if (dat.isNotEmpty()) {
                Log.i(TAG, "Return response is $dat")
                binding.etLocation.text = dat
            }
        })
        binding.apply {
            BrowseCategoryResultFragment.sortFilter.newest=true
            BrowseCategoryResultFragment.sortFilter.closest=false
            BrowseCategoryResultFragment.sortFilter.lowToHigh=false
            BrowseCategoryResultFragment.sortFilter.highToLow=false
            BrowseCategoryResultFragment.fromToFilter.priceTo=null
            BrowseCategoryResultFragment.fromToFilter.priceFrom=null
        }
    }



    private fun setChecks(newest: Boolean, closest: Boolean, lowToHigh: Boolean, highToLow: Boolean) {
        binding.apply {
            if (newest) {
                checkBoxClosest.isChecked = false
                checkBoxHighToLow.isChecked = false
                checkBoxLowToHigh.isChecked = false
               BrowseCategoryResultFragment.sortFilter.newest=true
               BrowseCategoryResultFragment.sortFilter.closest=false
               BrowseCategoryResultFragment.sortFilter.highToLow=false
               BrowseCategoryResultFragment.sortFilter.lowToHigh=false
            } else {
                if (closest) {
                    checkBoxHighToLow.isChecked = false
                    checkBoxLowToHigh.isChecked = false
                    checkBoxNewestFirst.isChecked = false
                    BrowseCategoryResultFragment.sortFilter.newest=false
                    BrowseCategoryResultFragment.sortFilter.closest=true
                    BrowseCategoryResultFragment.sortFilter.highToLow=false
                    BrowseCategoryResultFragment.sortFilter.lowToHigh=false
                } else {
                    if (lowToHigh) {
                        checkBoxClosest.isChecked = false
                        checkBoxHighToLow.isChecked = false
                        checkBoxNewestFirst.isChecked = false
                        BrowseCategoryResultFragment.sortFilter.newest=false
                        BrowseCategoryResultFragment.sortFilter.closest=false
                        BrowseCategoryResultFragment.sortFilter.lowToHigh=true
                        BrowseCategoryResultFragment.sortFilter.highToLow=false
                    } else {
                        if (highToLow) {
                            checkBoxClosest.isChecked = false
                            checkBoxLowToHigh.isChecked = false
                            checkBoxNewestFirst.isChecked = false

                            BrowseCategoryResultFragment.sortFilter.newest=false
                            BrowseCategoryResultFragment.sortFilter.closest=false
                            BrowseCategoryResultFragment.sortFilter.lowToHigh=false
                            BrowseCategoryResultFragment.sortFilter.highToLow=true
                        } else {
                            checkBoxClosest.isChecked = false
                            checkBoxHighToLow.isChecked = false
                            checkBoxLowToHigh.isChecked = false
                            checkBoxNewestFirst.isChecked = true

                            BrowseCategoryResultFragment.sortFilter.newest=true
                            BrowseCategoryResultFragment.sortFilter.closest=false
                            BrowseCategoryResultFragment.sortFilter.lowToHigh=false
                            BrowseCategoryResultFragment.sortFilter.highToLow=false
                        }
                    }
                }
            }
        }
    }
    fun isNumeric(input: String): String? {
        val regex = "\\d+".toRegex()
        return if (regex.matches(input)) {
            null // Input is valid
        } else {
            "Input must contain only numbers" // Input is invalid, return error message
        }
    }
    private fun clickListeners() {
        binding.apply {
            btnApplyFilter.setOnClickListener {
                Log.i(TAG, "newest ${checkBoxNewestFirst.isChecked}\nclosest ${checkBoxClosest.isChecked}\nlowToHigh${checkBoxLowToHigh.isChecked}\nhighToLow${checkBoxHighToLow.isChecked}")
                val from = etFromRangeFilter.text.toString().trim()
                val to = etToRangeFilter.text.toString().trim()
                if (from.isNotEmpty()) {
                    val tempFromResponse = isNumeric(from)
                    if (tempFromResponse == null) {
                        if (to.isNotEmpty()) {
                            val tempToResponse = isNumeric(to)
                            if (tempToResponse == null) {
                                if (from.toInt() <= to.toInt()) {
                                    BrowseCategoryResultFragment.fromToFilter.priceFrom = from.toInt() ?: 0
                                    BrowseCategoryResultFragment.fromToFilter.priceTo = to.toInt() ?: 0
                                    findNavController().popBackStack()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Please enter valid from range",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                binding.etToRangeFilter.text?.clear()
                                Toast.makeText(requireContext(), tempToResponse, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            binding.etFromRangeFilter.text?.clear()
                            Toast.makeText(requireContext(), tempFromResponse, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }else {
                    findNavController().popBackStack()
                    }

            }
                    viewFilter.setOnClickListener {
                        requireActivity().hideKeyBoard()
                    }

                    txtAutoFillLocation.setOnClickListener {
                        checkLocationSetting()
                    }
                    etLocation.setOnClickListener {
                        val fields: List<Place.Field> = listOf(
                            Place.Field.ID,
                            Place.Field.NAME,
                            Place.Field.ADDRESS,
                            Place.Field.LAT_LNG,
                            Place.Field.ADDRESS_COMPONENTS
                        )
                        val intent =
                            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                                .build(requireContext())
                        startActivityForResult(intent, 7874)
                    }


                    checkBoxNewestFirst.setOnClickListener {
                        setChecks(checkBoxNewestFirst.isChecked, false, false, false)
                    }
                    checkBoxClosest.setOnClickListener {
                        setChecks(false, checkBoxClosest.isChecked, false, false)
                    }
                    checkBoxLowToHigh.setOnClickListener {
                        setChecks(false, false, checkBoxLowToHigh.isChecked, false)
                    }
                    checkBoxHighToLow.setOnClickListener {
                        setChecks(false, false, false, checkBoxHighToLow.isChecked)
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 7874) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data)
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
                SharedPref(requireContext()).saveString(
                    Constant.LONGITUDE_LOCATION,
                    llng.toString()
                )
                binding.etLocation.text = tempAdress
                Log.i(
                    TAG,
                    "id $id , name $name\naddress $aaddress , lat $llat ,lng $llng\nAdress subadress ${
                        subAddress.get(0)?.name
                    },Adress locatility ${subAddress.get(1)?.name}"
                )
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status: Status = Autocomplete.getStatusFromIntent(data)
                status.statusMessage?.let { Log.i(TAG, it) }
                Log.i(TAG, "eror is ${status.statusMessage}")
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "Cancled")
                // The user canceled the operation.
            }
        }
        if (requestCode == 123) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.i(TAG, "on gps")
                    requestPermssionForLocation()
                }
                Activity.RESULT_CANCELED -> {
                    Log.i(TAG, "cancel on gps")
                }
            }
        }
    }

}
