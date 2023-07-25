package com.buysell.screens.category

import SharedPref
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentCategoryBinding
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException
import setToolBar

@AndroidEntryPoint
class CategoryFragment : BaseFragment() {
    private lateinit var binding:FragmentCategoryBinding
    private val viewModelCategory: ViewModelCategory by viewModels()
    private var browse:Boolean?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding=FragmentCategoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){
        binding.apply {
            model=viewModelCategory
        }
        browse=arguments?.getBoolean("browse")
        viewModelCategory.hitGetCategoryApi(SharedPref(requireContext()).getString(Constant.TOKEN_kEY))
        val appBarTitle=requireActivity().resources.getString(R.string.selectCategory)
        setToolBar(binding.appBarCategory,requireContext(),true,false,appBarTitle,10f)
        clickListner()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelCategory.categoryResponse.collect { data ->
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
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        val data: PojoCategory = Gson().fromJson(response, PojoCategory::class.java)
                        if (data.status == 200) {
                            if (data.success) {
                                setList(data.data)
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
    }
    private fun clickListner(){
        binding.apply {
                appBarCategory.tbBackBtn.setOnClickListener {
                    findNavController().popBackStack()
                }
        }
    }

    private fun setList( dataList:List<PojoCategoryData>){
        binding.apply {
            val list= arrayListOf<String>("Mobiles","Cars","Bikes","Baby","Clothing","Furniture","Households","Jewelry","Electronics","Collectibles","Games & Toys","Others")
            rvCategory.adapter=CategoryAdatper(requireActivity(),dataList,object:CategoryAdatper.Click{
                override fun onClick(position: PojoCategoryData) {
                    val bundle=Bundle()
                    bundle.putInt("categoryId",position.id)
                    Log.i(TAG,"Postition $position and last size ${list.size}")
                    if(browse!= null && browse == true){
                        bundle.putString("categoryName",position.category)
                        findNavController().navigate(R.id.action_categoryFragment_to_browseCategoryResultFragment2,bundle)
                    }
                    else{
                    if(position.id==1){
                        // for mobile
                        findNavController().navigate(R.id.action_categoryFragment_to_commonMobileFormFragment,bundle)
                    }
                    else if(position.id==2){
                        // for car
                        Log.i(TAG,"Inside 0")
                        findNavController().navigate(R.id.action_categoryFragment_to_carBikeFormFragment,bundle)

                    }
                    else if(position.id==3){
                        // for bike
                        Log.i(TAG,"Inside last index")
                        findNavController().navigate(R.id.action_categoryFragment_to_carBikeFormFragment,bundle)
                    }
                    else{
                        // for common
                        Log.i(TAG,"Else part")
                        findNavController().navigate(R.id.action_categoryFragment_to_commonMobileFormFragment,bundle)
                    }
                    }
                }

            })
        }
    }
}