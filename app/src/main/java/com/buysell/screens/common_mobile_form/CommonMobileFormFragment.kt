package com.buysell.screens.common_mobile_form

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
import com.buysell.databinding.FragmentMobileFormBinding
import com.buysell.screens.productdetail.PojoProductDetail
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import hideKeyBoard
import retrofit2.HttpException
import setToolBar

@AndroidEntryPoint
class CommonMobileFormFragment : Fragment() {
    private var status: Boolean? = null
    private lateinit var binding: FragmentMobileFormBinding
    private var previousBundle:Bundle?=null
    private var categoryId:Int?=null
    private var id:Int?=null
    private var passedDataClass:PojoProductDetail?=null
    private val viewModelCommonForm: ViewModelCommonForm by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMobileFormBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.model=viewModelCommonForm
        val appBarTitle=requireActivity().resources.getString(R.string.addSomeData)
        setToolBar(binding.appBarCommonMobileForm,requireContext(),true,false,appBarTitle,10f)
        clickListener()
        if(arguments!=null)
        previousBundle=arguments
        categoryId=previousBundle?.getInt("categoryId")?:-1
        id=previousBundle?.getInt("id")?:-1
        if( id!! > 0 ){
            Log.i(TAG,"id is $id")
          viewModelCommonForm.hitPostDetailCommonMobileApi(SharedPref(requireContext()).getString(Constant.TOKEN_kEY),id!!)
        }
        binding.apply {
            if (categoryId==1) {
                // for mobile
                txtTempBrandCommonMobileForm.visibility = View.VISIBLE
                etBrandNameCommonMobileForm.visibility = View.VISIBLE

            } else {

                // common
                txtTempBrandCommonMobileForm.visibility = View.GONE
                etBrandNameCommonMobileForm.visibility = View.GONE
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
            viewModelCommonForm.brandResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if(data.msg is HttpException){
                            val res= Extentions.getFailedMsg(data.msg)
                            Log.i(Extentions.TAG,"Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Log.i(Extentions.TAG,"else Error ${data.msg}")
                            Toast.makeText(requireContext(), "Failed due to ${data.msg}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Log.i(Extentions.TAG, "Login success ${data.data}")
                        Extentions.stopProgress()
                        val response = data.data as JsonObject

                        val data: PojoBrands = Gson().fromJson(response, PojoBrands::class.java)
                        if (data.status == 200) {
                            if (data.success) {
                                showBrandsDialog(requireContext(),data.data)
                            }
                        } else if (data.status==201) {
                            Toast.makeText(requireContext(), "${data.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
            viewModelCommonForm.postDetailResponseCommonMobileResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if(data.msg is HttpException){
                            val res= Extentions.getFailedMsg(data.msg)
                            Log.i(Extentions.TAG,"Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Log.i(Extentions.TAG,"else Error ${data.msg}")
                            Toast.makeText(requireContext(), "Failed due to ${data.msg}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Log.i(Extentions.TAG, "Login success ${data.data}")
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        val data: PojoProductDetail = Gson().fromJson(response, PojoProductDetail::class.java)
                        if (data.status == 200) {
                            if (data.success) {
                                passedDataClass=data

                                previousBundle?.putParcelable("passDataClass",passedDataClass)
                                viewModelCommonForm.brandName.set(data.data.brand)
                                binding.etAddTitleCommonMobileForm.setText(data.data.title)
                                binding.etAddDescriptionCommonMobileForm.setText(data.data.description)
                            }
                        } else  {
                            Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Empty -> {

                    }

                }
            }
        }
    }

    private fun showBrandsDialog(context: Context,data: List<DataBrands>) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_brands)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), android.R.color.white))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(true)
        val rvBrand: RecyclerView =dialog.findViewById(R.id.rvBrandsDialog)
        val toolBarBrand=dialog.findViewById<CardView>(R.id.appBarBrandsDialog)
        val backBtn:AppCompatImageView=toolBarBrand.findViewById(R.id.tbBackBtn)
        val titleAppBar:AppCompatTextView=toolBarBrand.findViewById(R.id.tbTitle)
        toolBarBrand.elevation=10f
        titleAppBar.visibility=View.VISIBLE
        titleAppBar.text=context.getString(R.string.select_brand)
        backBtn.setOnClickListener {
            dialog.dismiss()
        }
        val adapter=AdapterBrand(context,data,object:AdapterBrand.Clicks{
            override fun onClick(data: DataBrands) {
                Log.i(TAG,"selected data is $data")
                if(data.brand.isNotEmpty()){
//                    if(data.hasModel){
                        viewModelCommonForm.brandName.set(data.brand)
                        binding.etBrandNameCommonMobileForm.setTextColor(ContextCompat.getColor(requireContext(),R.color.signUpBtnColor))
                        dialog.dismiss()
//                    }
                }
                Log.i(TAG,"CLicked brand is ${data}")
            }
        })
        rvBrand.adapter=adapter
        dialog.show()
    }

    private fun clickListener() {
        binding.apply {

            cLayoutCommonMobileForm.setOnClickListener {
                requireActivity().hideKeyBoard()
            }
            etBrandNameCommonMobileForm.setOnClickListener {
                viewModelCommonForm.hitGetBrandsApi(categoryId?:1)
            }
            btnNextCommonMobileForm.setOnClickListener {
                val title = etAddTitleCommonMobileForm.text?.trim().toString()
                val description = etAddDescriptionCommonMobileForm.text?.trim().toString()
                requireActivity().hideKeyBoard()
                // for common
                if (categoryId == 1) {
                    val brand = viewModelCommonForm.brandName.get().toString().trim()
                    if (brand.isNotEmpty() && brand!="Select Brand") {
                        if (title.isNotEmpty()) {
                            if (description.isNotEmpty()) {
                                previousBundle?.putString("brand", brand)
                                previousBundle?.putString("title", title)
                                previousBundle?.putString("description", description)
//                                etBrandNameCommonMobileForm.text?.clear()
//                                etAddTitleCommonMobileForm.text?.clear()
//                                etAddDescriptionCommonMobileForm.text?.clear()
                                findNavController().navigate(R.id.action_commonMobileFormFragment_to_uploadImageFragment, previousBundle)
                            } else {
                                Toast.makeText(requireContext(),
                                    "Add description",
                                    Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(),
                                "Add title",
                                Toast.LENGTH_SHORT)
                                .show()
                        }


                    }
                    else{
                        Toast.makeText(requireContext(),
                            "Add Brand Name",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                    else {
                        if (title.isNotEmpty()) {
                            if (description.isNotEmpty()) {
                                previousBundle?.putBoolean("common", true)
                                previousBundle?.putString("title", title)
                                previousBundle?.putString("description", description)
                                etAddTitleCommonMobileForm.text?.clear()
                                etAddDescriptionCommonMobileForm.text?.clear()
                                findNavController().navigate(R.id.action_commonMobileFormFragment_to_uploadImageFragment,
                                    previousBundle)
                            } else {
                                Toast.makeText(requireContext(),
                                    "Please enter description",
                                    Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            Toast.makeText(requireContext(),
                                "Please enter title",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }


    }