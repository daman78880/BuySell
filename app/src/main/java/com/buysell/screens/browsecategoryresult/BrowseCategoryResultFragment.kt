package com.buysell.screens.browsecategoryresult

import SharedPref
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentBrowseCategoryResultBinding
import com.buysell.screens.editprofile.PojoCountrys
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException

@AndroidEntryPoint
class BrowseCategoryResultFragment : BaseFragment() {
   private lateinit var binding:FragmentBrowseCategoryResultBinding
    private val viewModelBrowseCategoryResult: ViewModelBrowseCategoryResult by viewModels()
    private var id:Int?=null
    private var idName:String?=null
    private var token:String?=null
    private var likeIndex:Int?=null
    private var list:kotlin.collections.ArrayList<Data>?=null
    private var adatperBrowseCategory:AdatperBrowseCategory?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding= FragmentBrowseCategoryResultBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun init(){
        id=arguments?.getInt("categoryId")
        idName=arguments?.getString("categoryName")
        list= kotlin.collections.ArrayList()
        binding.txtTempSearchBrowseCategoryResult.text?.clear()
        token=SharedPref(requireContext()).getString(Constant.TOKEN_kEY)
        if(id!=null){
        val tempJson=JsonObject()
        var priceFromTemp=0
        var priceToTemp=0
        if((fromToFilter.priceFrom!=null) ){
            if(fromToFilter.priceFrom!! >0){
                priceFromTemp= fromToFilter.priceFrom!!
                priceToTemp= fromToFilter.priceTo!!
            }
        }
        if(sortFilter.newest){
            tempJson.addProperty("sort",3)
        }else{
            if(sortFilter.closest){
                tempJson.addProperty("sort",3)
            }else{
                if(sortFilter.lowToHigh){
                    tempJson.addProperty("sort",2)
                }else{
                    if(sortFilter.highToLow){
                        tempJson.addProperty("sort",1)
                    }else{
                        tempJson.addProperty("sort",3)
//                                viewModelHome.hitPostsNoPriceApi(token!!,3)
                    }
                }
            }
        }
        if(priceFromTemp>0) {
            Log.i("asbskanbjkajfnbdsa","hiting post with price\nid$id\npriceFrom$priceFromTemp\npriceTo$priceToTemp\njsong$tempJson")
            viewModelBrowseCategoryResult.hitBrowseCategoryWithPriceApi(token!!, id.toString(), priceFromTemp ?: 0, priceToTemp ?: 0, tempJson)
        }else{
            Log.i("asbskanbjkajfnbdsa","hiting post without price\nid$id\npriceFrom$priceFromTemp\npriceTo$priceToTemp\njsong$tempJson")
            viewModelBrowseCategoryResult.hitBrowseCategoryApi(token!!, id.toString(),tempJson)
        }
        }

        binding.apply {
            setToolBar()
        }
        clickListeners()
        lifecycleScope.launchWhenStarted {
            viewModelBrowseCategoryResult.browseCategoryHomeResponse.collect { data ->
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
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                            Log.i(TAG,"Success  brose result $response")
                        val data: PojoBrowseCategoryResult = Gson().fromJson(response, PojoBrowseCategoryResult::class.java)
                        if (data.status == 200) {
                            if (data.success) {
//                                listData.clear()
                                list?.clear()
                                list?.addAll(data.data)
//                                listData.addAll(data.data)
//                                adatperBrowseCategory?.list?.clear()
//                                adatperBrowseCategory?.list?.addAll(data.data)
                                adatperBrowseCategory?.list=list as ArrayList<Data>
                              adatperBrowseCategory?.notifyDataSetChanged()
                                Log.i(Extentions.TAG, "inside success ${data.data}")
                            }
                        } else if (data.status==201) {
                            Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModelBrowseCategoryResult.likePostBrowseCategoryResponse.collect { data ->
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
                            Toast.makeText(requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        if (response.has("status")) {
                            if (response.get("status").toString().toInt() == 200) {
                                if(likeIndex!= null && likeIndex!! >=0){
                                    list!![likeIndex!!].liked=!list!![likeIndex!!].liked
                                    likeIndex=null
                                    binding.rvBrowseCategoryResult.adapter?.notifyDataSetChanged()
                                }
                            } else {
                                Toast.makeText(requireContext(), "${response.get("message")}",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    is ApiState.Empty -> {

                    }
                }
            }
        }
        adatperBrowseCategory=AdatperBrowseCategory(requireContext(),list!!,object :AdatperBrowseCategory.Click{
            override fun onClick(value: Data) {
                val bundle=Bundle()
                bundle.putInt("idP",value.id)
                findNavController().navigate(R.id.action_browseCategoryResultFragment2_to_productDetailFragment,bundle)
            }

            override fun onLike(value: Data,position: Int) {
                val jsonObject=JsonObject()
                jsonObject.addProperty("id",value.id)
                viewModelBrowseCategoryResult.hitLikePostBrowseCategoryApi(token!!,jsonObject)
                likeIndex=position

            }

        })
        binding.rvBrowseCategoryResult.adapter=adatperBrowseCategory
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clickListeners() {
        binding.apply {
            appBarBrowseCategoryResult.imgFilterHomeToolBar.setOnClickListener {
                findNavController().navigate(R.id.action_browseCategoryResultFragment2_to_filterFragment)
            }
            appBarBrowseCategoryResult.imgLocationIconHomeToolBar.setOnClickListener {
                findNavController().popBackStack()
            }
            txtTempSearchBrowseCategoryResult.doOnTextChanged { text, start, before, count ->
               adatperBrowseCategory?.filter?.filter(text)
            }

    }
    }

    override fun onStart() {
        super.onStart()
        binding.txtTempSearchBrowseCategoryResult.text?.clear()
    }
    private fun setToolBar() {
        binding.apply {
            appBarBrowseCategoryResult.cardViewHomeToolbar.cardElevation = 10f
            appBarBrowseCategoryResult.txtLocationHomeToolBar.gravity= Gravity.CENTER_HORIZONTAL
            appBarBrowseCategoryResult.txtLocationHomeToolBar.setTextColor(ContextCompat.getColor(requireContext(),R.color.signUpBtnColor))
            appBarBrowseCategoryResult.txtLocationHomeToolBar.text=idName.toString()
            appBarBrowseCategoryResult.imgLocationIconHomeToolBar.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.left_arrow))

        }
    }
    companion object{
        val sortFilter=Extentions.FilterSortModel()
        val fromToFilter=Extentions.FilterPriceSortModel()
    }

}