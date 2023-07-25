package com.buysell.screens.car_bike_form

import SharedPref
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.databinding.FragmentCarBikeFormBinding
import com.buysell.screens.common_mobile_form.AdapterBrand
import com.buysell.screens.common_mobile_form.DataBrands
import com.buysell.screens.common_mobile_form.PojoBrands
import com.buysell.screens.common_mobile_form.ViewModelCommonForm
import com.buysell.screens.productdetail.PojoProductDetail
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.facebook.share.Share
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import hideKeyBoard
import retrofit2.HttpException
import setToolBar

@AndroidEntryPoint
class CarBikeFormFragment : Fragment() {
    private lateinit var binding: FragmentCarBikeFormBinding
    private var fuell: String? = null
    private var transmissionn: String? = null
    private var noOfOwner: String? = null
    private var previousBundle: Bundle? = null
    private var categoryId: Int? = null
    private var id: Int? = null
    private val viewModelCommonForm: ViewModelCommonForm by viewModels()
    private var brandApiHitingValue: Int = 0
    private var passedDataClass: PojoProductDetail? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCarBikeFormBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onResume() {
        super.onResume()
        if (viewModelCommonForm.brandName.get() == "Select Brand") {
            brandApiHitingValue = 0
        }
    }

    private fun init() {
        val appBarTitle = requireActivity().resources.getString(R.string.addSomeData)

        setToolBar(binding.appBarCarBikeForm, requireContext(), true, false, appBarTitle, 10f)
        clickListener()
        binding.apply {
            model = viewModelCommonForm
            if (arguments != null) {
                previousBundle = arguments
            }
            brandApiHitingValue = 0
            categoryId = previousBundle?.getInt("categoryId") ?: -1
            id = previousBundle?.getInt("id") ?: -1
            if (id!! > 0) {
                viewModelCommonForm.hitPostDetailCommonMobileApi(
                    SharedPref(requireContext()).getString(
                        Constant.TOKEN_kEY
                    ), id!!
                )
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModelCommonForm.postDetailResponseCommonMobileResponse.collect { data ->
                    when (data) {
                        is ApiState.Loading -> {
                            Extentions.showProgress(requireContext())
                        }
                        is ApiState.Failure -> {
                            Extentions.stopProgress()
                            if (data.msg is HttpException) {
                                val res = Extentions.getFailedMsg(data.msg)
                                Log.i(Extentions.TAG, "Error ${res?.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "${res?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.i(Extentions.TAG, "else Error ${data.msg}")
                                Toast.makeText(
                                    requireContext(),
                                    "Failed due to ${data.msg}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is ApiState.Success -> {
                            Log.i(Extentions.TAG, "Login success ${data.data}")
                            Extentions.stopProgress()
                            val response = data.data as JsonObject
                            val data: PojoProductDetail =
                                Gson().fromJson(response, PojoProductDetail::class.java)
                            if (data.status == 200) {
                                if (data.success) {
                                    passedDataClass = data
                                    previousBundle?.putParcelable("passDataClass", passedDataClass)
                                    viewModelCommonForm.brandName.set(passedDataClass!!.data.brand)
                                    binding.etYearCarBikeForm.setText(passedDataClass!!.data.year)
                                    binding.etKmDriveCarBikeForm.setText(passedDataClass!!.data.kmDriven)
                                    binding.etAddTitleCarBikeForm.setText(passedDataClass!!.data.title)
                                    binding.etAddDescriptionCarBikeForm.setText(passedDataClass!!.data.description)
                                    if (passedDataClass?.data?.fuel == requireContext().getString(R.string.cng_amp_hybrids)) {
                                        requireActivity().hideKeyBoard()
                                        fuell = requireContext().getString(R.string.cng_amp_hybrids)
                                        selectFuel(true, false, false, false)
                                    } else if (passedDataClass?.data?.fuel.toString() == requireContext().getString(
                                            R.string.diesel
                                        )
                                    ) {
                                        requireActivity().hideKeyBoard()
                                        fuell = requireContext().getString(R.string.diesel)
                                        selectFuel(false, true, false, false)
                                    } else if (passedDataClass?.data?.fuel.toString() == requireContext().getString(
                                            R.string.electric
                                        )
                                    ) {
                                        requireActivity().hideKeyBoard()
                                        fuell = requireContext().getString(R.string.electric)
                                        selectFuel(false, false, true, false)
                                    } else {
                                        requireActivity().hideKeyBoard()
                                        fuell = requireContext().getString(R.string.petrol)
                                        selectFuel(false, false, false, true)
                                    }
                                    if (passedDataClass?.data?.transmission.toString() == requireContext().getString(
                                            R.string.automatic
                                        )
                                    ) {
                                        requireActivity().hideKeyBoard()
                                        transmissionn =
                                            requireContext().getString(R.string.automatic)
                                        setTransmission(true, false)
                                    } else {
                                        requireActivity().hideKeyBoard()
                                        transmissionn = requireContext().getString(R.string.manual)
                                        setTransmission(false, true)
                                    }
                                    if (passedDataClass?.data?.numberOfowners.toString() == requireContext().getString(
                                            R.string._1st
                                        )
                                    ) {
                                        requireActivity().hideKeyBoard()
                                        noOfOwner = requireContext().getString(R.string._1st)
                                        setNoOfOwners(true, false, false, false, false)
                                    } else if (passedDataClass?.data?.numberOfowners.toString() == requireContext().getString(
                                            R.string._2nd
                                        )
                                    ) {
                                        requireActivity().hideKeyBoard()
                                        noOfOwner = requireContext().getString(R.string._2nd)
                                        setNoOfOwners(false, true, false, false, false)
                                    } else if (passedDataClass?.data?.numberOfowners.toString() == requireContext().getString(
                                            R.string._3rd
                                        )
                                    ) {
                                        requireActivity().hideKeyBoard()
                                        noOfOwner = requireContext().getString(R.string._3rd)
                                        setNoOfOwners(false, false, true, false, false)
                                    } else if (passedDataClass?.data?.numberOfowners.toString() == requireContext().getString(
                                            R.string._4th
                                        )
                                    ) {
                                        requireActivity().hideKeyBoard()
                                        noOfOwner = requireContext().getString(R.string._4th)
                                        setNoOfOwners(false, false, false, true, false)
                                    } else {
                                        requireActivity().hideKeyBoard()
                                        noOfOwner = requireContext().getString(R.string._4)
                                        setNoOfOwners(false, false, false, false, true)
                                    }
                                }
                            } else {
                                Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        is ApiState.Empty -> {

                        }

                    }
                }
            }


            // for car
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModelCommonForm.brandResponse.collect { data ->
                    when (data) {
                        is ApiState.Loading -> {
                            Extentions.showProgress(requireContext())
                        }
                        is ApiState.Failure -> {
                            Extentions.stopProgress()
                            if (data.msg is HttpException) {
                                val res = Extentions.getFailedMsg(data.msg)
                                Log.i(TAG, "Error ${res?.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "${res?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                            Log.i(TAG, "Login success ${data.data}")
                            Extentions.stopProgress()
                            val response = data.data as JsonObject
                            val data: PojoBrands = Gson().fromJson(response, PojoBrands::class.java)
                            if (data.status == 200) {
                                if (data.success) {
                                    Log.i(TAG, "shoing dialg now")
                                    val title = requireActivity().getString(R.string.select_brand)
                                    showBrandsDialog(requireContext(), title, data.data)
                                }
                            } else if (data.status == 201) {
                                Toast.makeText(
                                    requireContext(),
                                    data.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is ApiState.Empty -> {

                        }
                    }
                }
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModelCommonForm.modelResponse.collect { data ->
                    when (data) {
                        is ApiState.Loading -> {
                            Extentions.showProgress(requireContext())
                        }
                        is ApiState.Failure -> {
                            Extentions.stopProgress()
                            if (data.msg is HttpException) {
                                val res = Extentions.getFailedMsg(data.msg)
                                Log.i(TAG, "Error ${res?.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "${res?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                            Log.i(TAG, "Login success ${data.data}")
                            Extentions.stopProgress()
                            val response = data.data as JsonObject
                            val data: PojoModel = Gson().fromJson(response, PojoModel::class.java)
                            if (data.status == 200) {
                                if (data.success) {

                                    if (brandApiHitingValue == 1) {
                                        val title =
                                            requireActivity().getString(R.string.select_model)
                                        showModelsDialog(requireContext(), title, data.data)
                                    } else if (brandApiHitingValue == 2) {
                                        val title =
                                            requireActivity().getString(R.string.select_varient)
                                        showModelsDialog(requireContext(), title, data.data)
                                    } else {
                                        Log.i(TAG, "else part of model select")
                                    }
                                }
                            } else if (data.status == 201) {
                                Toast.makeText(
                                    requireContext(),
                                    data.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is ApiState.Empty -> {

                        }
                    }
                }
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModelCommonForm.varientResponse.collect { data ->
                    when (data) {
                        is ApiState.Loading -> {
                            Extentions.showProgress(requireContext())
                        }
                        is ApiState.Failure -> {
                            Extentions.stopProgress()
                            if (data.msg is HttpException) {
                                val res = Extentions.getFailedMsg(data.msg)
                                Log.i(TAG, "Error ${res?.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "${res?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                            Log.i(TAG, "Varient success ${data.data}")

//                            val parser = JsonParser()
//                            val mJson: JsonElement = parser.parse(response.toString())
//                            val gson = Gson()
//                            val data: PojoModel = gson.fromJson(mJson, PojoModel::class.java)
//                            if (data.status == 200) {
//                                if (data.success) {
//
//                                }
//                            } else if (data.status==201) {
//                                Toast.makeText(requireContext(), "${data.message}", Toast.LENGTH_SHORT).show()
//                            }
                        }
                        is ApiState.Empty -> {

                        }
                    }
                }
            }


            if (categoryId == 2) {
                txtTempFuelCarBikeForm.visibility = View.VISIBLE
                cLayoutFuelSelectCarBikeForm.visibility = View.VISIBLE
                txtTempTransmissionCarBikeForm.visibility = View.VISIBLE
                txtAutomaticCarBikeForm.visibility = View.VISIBLE
                txtManualCarBikeForm.visibility = View.VISIBLE
                txtTempNoOfOwnerCarBikeForm.visibility = View.VISIBLE
                txtFirstOwnerCarBikeForm.visibility = View.VISIBLE
                txtSecondOwnerCarBikeForm.visibility = View.VISIBLE
                txtThirdwnerCarBikeForm.visibility = View.VISIBLE
                txtFourthwnerCarBikeForm.visibility = View.VISIBLE
                txtFourthPluswnerCarBikeForm.visibility = View.VISIBLE
            }
            // for bike
            else {
                txtTempFuelCarBikeForm.visibility = View.GONE
                cLayoutFuelSelectCarBikeForm.visibility = View.GONE
                txtTempTransmissionCarBikeForm.visibility = View.GONE
                txtAutomaticCarBikeForm.visibility = View.GONE
                txtManualCarBikeForm.visibility = View.GONE
                txtTempNoOfOwnerCarBikeForm.visibility = View.GONE
                txtFirstOwnerCarBikeForm.visibility = View.GONE
                txtSecondOwnerCarBikeForm.visibility = View.GONE
                txtThirdwnerCarBikeForm.visibility = View.GONE
                txtFourthwnerCarBikeForm.visibility = View.GONE
                txtFourthPluswnerCarBikeForm.visibility = View.GONE
            }
            txtCngHyBrideCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
                fuell = requireContext().getString(R.string.cng_amp_hybrids)
                selectFuel(true, false, false, false)
            }
            txtDieselCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
                fuell = requireContext().getString(R.string.diesel)
                selectFuel(false, true, false, false)
            }
            txtElectricCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
                fuell = requireContext().getString(R.string.electric)
                selectFuel(false, false, true, false)
            }
            txtPetrolCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
                fuell = requireContext().getString(R.string.petrol)
                selectFuel(false, false, false, true)
            }
            txtAutomaticCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
//                transmission = 1
                transmissionn = requireContext().getString(R.string.automatic)
                setTransmission(true, false)
            }
            txtManualCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
//                transmission = 2
                transmissionn = requireContext().getString(R.string.manual)
                setTransmission(false, true)
            }
            txtFirstOwnerCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
                noOfOwner = requireContext().getString(R.string._1st)
                setNoOfOwners(true, false, false, false, false)
            }

            txtSecondOwnerCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
                noOfOwner = requireContext().getString(R.string._2nd)
                setNoOfOwners(false, true, false, false, false)
            }

            txtThirdwnerCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
                noOfOwner = requireContext().getString(R.string._3rd)
                setNoOfOwners(false, false, true, false, false)
            }

            txtFourthwnerCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
                noOfOwner = requireContext().getString(R.string._4th)
                setNoOfOwners(false, false, false, true, false)
            }

            txtFourthPluswnerCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
                noOfOwner = requireContext().getString(R.string._4)
                setNoOfOwners(false, false, false, false, true)
            }

        }
    }

    private fun clickListener() {
        binding.apply {

            cLayoutCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
            }
            etBrandNameCarBikeForm.setOnClickListener {
                viewModelCommonForm.hitGetBrandsApi(categoryId!!)
            }
            btnNextCarBikeForm.setOnClickListener {
                requireActivity().hideKeyBoard()
                val brand = etBrandNameCarBikeForm.text?.trim().toString()
                val year = etYearCarBikeForm.text?.trim().toString()
                val kmDrive = etKmDriveCarBikeForm.text?.trim().toString()
                val title = etAddTitleCarBikeForm.text?.trim().toString()
                val description = etAddDescriptionCarBikeForm.text?.trim().toString()

                if (brand.isNotEmpty()) {
                    if (year.isNotEmpty()) {
                        if (kmDrive.isNotEmpty()) {
                            // for car
                            if (categoryId == 2) {
                                if (null != fuell) {
                                    if (null != transmissionn) {
                                        if (null != noOfOwner) {
                                            if (title.isNotEmpty()) {
                                                if (description.isNotEmpty()) {

                                                    previousBundle?.putString("brand", brand)
                                                    previousBundle?.putString("year", year)
                                                    previousBundle?.putString("kmDrive", kmDrive)
                                                    previousBundle?.putString(
                                                        "fuel",
                                                        fuell
                                                            ?: requireContext().getString(R.string.petrol)
                                                    )
                                                    previousBundle?.putString(
                                                        "transmission",
                                                        transmissionn ?: requireContext().getString(
                                                            R.string.automatic
                                                        )
                                                    )
                                                    previousBundle?.putString(
                                                        "noOfOwner",
                                                        noOfOwner
                                                    )
                                                    previousBundle?.putString("title", title)
                                                    previousBundle?.putString(
                                                        "description",
                                                        description
                                                    )
//                                                       etBrandNameCarBikeForm.text?.clear()
//                                                       etYearCarBikeForm.text?.clear()
//                                                       etKmDriveCarBikeForm.text?.clear()
                                                    selectFuel(false, false, false, false)
                                                    setTransmission(false, false)
                                                    setNoOfOwners(false, false, false, false, false)
                                                    etAddTitleCarBikeForm.text?.clear()
                                                    etAddDescriptionCarBikeForm.text?.clear()
                                                    findNavController().navigate(
                                                        R.id.action_carBikeFormFragment_to_uploadImageFragment,
                                                        previousBundle
                                                    )
                                                } else {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Please enter description",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Please enter title",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Please select no. of owner",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "Please select transmission",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Please select fuel type",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            // for bike
                            else {
                                if (title.isNotEmpty()) {
                                    if (description.isNotEmpty()) {
                                        previousBundle?.putBoolean("car", false)
                                        previousBundle?.putString("brand", brand)
                                        previousBundle?.putString("year", year)
                                        previousBundle?.putString("kmDrive", kmDrive)
                                        previousBundle?.putString("title", title)
                                        previousBundle?.putString("description", description)
                                        findNavController().navigate(
                                            R.id.action_carBikeFormFragment_to_uploadImageFragment,
                                            previousBundle
                                        )
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "Please enter description",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Please enter title",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Please enter Km drive",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Please enter year", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter brand name", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }
    }


    private fun selectFuel(cng: Boolean, diesel: Boolean, electric: Boolean, petrol: Boolean) {
        binding.apply {
            if (cng) {
                txtCngHyBrideCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtCngHyBrideCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)
            } else {
                txtCngHyBrideCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtCngHyBrideCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)
            }
            if (diesel) {
                txtDieselCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtDieselCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)
            } else {
                txtDieselCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtDieselCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)
            }
            if (electric) {
                txtElectricCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtElectricCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)
            } else {
                txtElectricCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtElectricCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)
            }
            if (petrol) {
                txtPetrolCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtPetrolCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)
            } else {
                txtPetrolCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtPetrolCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)
            }
        }
    }

    private fun setTransmission(autoMatic: Boolean, manual: Boolean) {
        binding.apply {
            if (autoMatic) {
                txtAutomaticCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtAutomaticCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)

            } else {
                txtAutomaticCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtAutomaticCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)

            }
            if (manual) {
                txtManualCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtManualCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)

            } else {
                txtManualCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtManualCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)
            }
        }
    }

    private fun setNoOfOwners(
        first: Boolean,
        second: Boolean,
        third: Boolean,
        fourth: Boolean,
        fourPlus: Boolean
    ) {
        binding.apply {
            if (first) {
                txtFirstOwnerCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtFirstOwnerCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)

            } else {
                txtFirstOwnerCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtFirstOwnerCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)

            }
            if (second) {
                txtSecondOwnerCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtSecondOwnerCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)

            } else {
                txtSecondOwnerCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtSecondOwnerCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)
            }
            if (third) {
                txtThirdwnerCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtThirdwnerCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)

            } else {
                txtThirdwnerCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtThirdwnerCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)
            }
            if (fourth) {
                txtFourthwnerCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtFourthwnerCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)

            } else {
                txtFourthwnerCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtFourthwnerCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)
            }
            if (fourPlus) {
                txtFourthPluswnerCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                txtFourthPluswnerCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)

            } else {
                txtFourthPluswnerCarBikeForm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.signUpBtnColor
                    )
                )
                txtFourthPluswnerCarBikeForm.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.et_filter)
            }
        }
    }

    private fun showBrandsDialog(context: Context, appBarTitle: String, data: List<DataBrands>) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_brands)
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                android.R.color.white
            )
        )
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.setCancelable(true)
        val rvBrand: RecyclerView = dialog.findViewById(R.id.rvBrandsDialog)
        val toolBarBrand = dialog.findViewById<CardView>(R.id.appBarBrandsDialog)
        val backBtn: AppCompatImageView = toolBarBrand.findViewById(R.id.tbBackBtn)
        val titleAppBar: AppCompatTextView = toolBarBrand.findViewById(R.id.tbTitle)
        toolBarBrand.elevation = 10f
        titleAppBar.visibility = View.VISIBLE
        titleAppBar.text = appBarTitle
        backBtn.setOnClickListener {
            dialog.dismiss()
        }
        val adapter = AdapterBrand(context, data, object : AdapterBrand.Clicks {
            override fun onClick(data: DataBrands) {
                if (data.brand.isNotEmpty()) {
                    if (data.hasModel) {
                        //brand -model -- id
                        brandApiHitingValue = 1
                        viewModelCommonForm.brandName.set(data.brand)
                        viewModelCommonForm.hitGetModelApi(data.id, categoryId!!)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Sorry this model is not available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                Log.i(TAG, "CLicked brand is ${data}")
            }

        })
        rvBrand.adapter = adapter
        dialog.show()
    }

    private fun showModelsDialog(context: Context, appBarTitle: String, data: List<DataModel>) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_brands)
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                android.R.color.white
            )
        )
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.setCancelable(true)
        val rvBrand: RecyclerView = dialog.findViewById(R.id.rvBrandsDialog)
        val toolBarBrand = dialog.findViewById<CardView>(R.id.appBarBrandsDialog)
        val backBtn: AppCompatImageView = toolBarBrand.findViewById(R.id.tbBackBtn)
        val titleAppBar: AppCompatTextView = toolBarBrand.findViewById(R.id.tbTitle)
        toolBarBrand.elevation = 10f
        titleAppBar.visibility = View.VISIBLE
        titleAppBar.text = appBarTitle
        backBtn.setOnClickListener {
            dialog.dismiss()
        }
        val adapter = AdapterModel(context, data, object : AdapterModel.Clicks {
            override fun onClick(data: DataModel) {
                if (data.modelName.isNotEmpty()) {

                    if (brandApiHitingValue == 1 && categoryId == 3) {
                        //model -varient id
                        val brandName = viewModelCommonForm.brandName.get()
                        viewModelCommonForm.brandName.set("${brandName}/${data.modelName}")
                        brandApiHitingValue += 1
                        dialog.dismiss()
                    } else if (brandApiHitingValue == 1 && categoryId == 2) {
                        //model -varient id
                        val brandName = viewModelCommonForm.brandName.get()
                        viewModelCommonForm.brandName.set("${brandName}/${data.modelName}")
                        brandApiHitingValue += 1
                        viewModelCommonForm.hitGetVarientApi(data.id, categoryId!!)
                        dialog.dismiss()
                    } else {
                        //varient id
                        val brandName = viewModelCommonForm.brandName.get()
                        viewModelCommonForm.brandName.set("${brandName}/${data.modelName}")
                        dialog.dismiss()
                    }
                }
            }
        })
        rvBrand.adapter = adapter
        dialog.show()
    }
}