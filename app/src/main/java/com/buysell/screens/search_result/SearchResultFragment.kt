package com.buysell.screens.search_result

import SharedPref
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.databinding.FragmentSearchResultBinding
import com.buysell.screens.search_screen.Data
import com.buysell.screens.search_screen.PojoSearchTitleResult
import com.buysell.screens.search_screen.ViewModelSearch
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException
import setToolBar

@AndroidEntryPoint
class SearchResultFragment : Fragment() {
    private lateinit var binding:FragmentSearchResultBinding
    private var title:String=""
    private val viewModelSearch: ViewModelSearch by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding= FragmentSearchResultBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    private fun init(){
        title= arguments?.getString("title","").toString()
        if(title.isNotEmpty()){
            val jsonObject = JsonObject()
            jsonObject.addProperty("search", title.toString())
            viewModelSearch.hitSearchApi(SharedPref(requireContext()).getString(Constant.TOKEN_kEY),jsonObject)
        }
        val appBarTitle=resources.getString(R.string.searchResult)
        setToolBar(binding.appBarSearchResult,requireContext(),true,false,appBarTitle,10f)
        clickListeners()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelSearch.postsResponseSearch.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(
                                Extentions.TAG,
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
                        Extentions.stopProgress()
                        if (responsePojoSearchTitle.status == 200) {
                           setAdapter(responsePojoSearchTitle.data)
                        } else {
                            Toast.makeText(requireContext(), responsePojoSearchTitle.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
    }
    private fun clickListeners(){
        binding.apply {
            appBarSearchResult.tbBackBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
    private fun setAdapter(data: List<Data>) {

        binding.rvSearchResult.adapter=AdatperSearchResult(requireContext(),data,object:AdatperSearchResult.Click{
            override fun onClick(value: Data) {
                
        val bundle = Bundle()
        bundle.putInt("idP", value.id)
        findNavController().navigate(R.id.action_searchResultFragment_to_productDetailFragment, bundle)
            }
        })
    }
}