package com.buysell.screens.myads

import SharedPref
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentAdsBinding
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException
import setToolBar

@AndroidEntryPoint
class AdsFragment : BaseFragment() {
    private lateinit var binding: FragmentAdsBinding
    private val viewModelAdsFragment: ViewModelAdsFragment by viewModels()
    private var token: String? = null
    private lateinit var myPostList:List<Data>
    private lateinit var myFavouritePostList:ArrayList<Data>
    private var likeIndex=-1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAdsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Extentions.stopProgress()
        init()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {


        val appBarTitle = requireActivity().resources.getString(R.string.my_ads)
        token = SharedPref(requireContext()).getString(Constant.TOKEN_kEY)
        setToolBar(binding.appBarMyAds, requireContext(), true, false, appBarTitle, 10f, true)
        setMyAdsTab()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
            viewModelAdsFragment.myPostsResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(Extentions.TAG, "Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.i(Extentions.TAG, "else Error ${data.msg}")
                            Toast.makeText(requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        val data: PojoMyads =  Gson().fromJson(response, PojoMyads::class.java)
                        if (data.status == 200) {
                            if (data.success) {
                                myPostList=ArrayList()
                                myPostList=data.data
                                setMyAdsAdapter()
                                clickListeners()
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
            viewModelAdsFragment.myFavouritePostsResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(Extentions.TAG, "Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.i(Extentions.TAG, "else Error ${data.msg}")
                            Toast.makeText(requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        val data: PojoMyads = Gson().fromJson(response, PojoMyads::class.java)
                        if (data.status == 200) {
                            if (data.success) {
                                myFavouritePostList=ArrayList()
                                myFavouritePostList.clear()
                                myFavouritePostList.addAll(data.data)
                                setFavouriteMyAdsAdapter()
                                clickListeners()
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
        viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
            viewModelAdsFragment.myFavouriteLikePostsResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(Extentions.TAG, "Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.i(Extentions.TAG, "else Error ${data.msg}")
                            Toast.makeText(requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        Log.i(Extentions.TAG, "Like success ${response}")
                        if (response.has("status")) {
                            if (response.get("status").toString().toInt() == 200) {
                                    if(likeIndex>-1){
                                        myFavouritePostList.removeAt(likeIndex)
                                        binding.rvMyAds.adapter?.notifyDataSetChanged()
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
    }

    private fun setMyAdsAdapter() {
        binding.apply {
            rvMyAds.adapter = AdatperMyAds(requireContext(), myPostList, object : AdatperMyAds.Clicks {
                override fun onClick(value: Data) {
                    val bundle = Bundle()
                    bundle.putInt("idP", value.id)
                    bundle.putBoolean("myAds", true)
                    findNavController().navigate(R.id.action_adsFragment_to_productDetailFragment, bundle)
                }
            })
        }
    }

    private fun setFavouriteMyAdsAdapter() {
        binding.apply {
            rvMyAds.adapter =
                AdatperFavouriteMyAds(requireContext(), myFavouritePostList, object : AdatperFavouriteMyAds.Click {
                    override fun onClick(value: Data) {
                        val bundle = Bundle()
                        bundle.putInt("idP", value.id)
                        findNavController().navigate(R.id.action_adsFragment_to_productDetailFragment, bundle)
                    }

                    override fun onLikeClick(value: Data, index: Int) {
                        Log.i(TAG,"inside like $index")
                        likeIndex=index
                        val jsonObject=JsonObject()
                        jsonObject.addProperty("id",value.id)
                        viewModelAdsFragment.hitLikePostAdsApi(token!!,jsonObject)
                    }
                })
        }
    }

    private fun clickListeners() {
        binding.apply {
            txtTempMyAdsMyAds.setOnClickListener {
                setMyAdsTab()
            }
            txtTempMyFavouriteMyAds.setOnClickListener {
                setFaviouriteTab()
            }

        }
    }

    private fun setMyAdsTab() {
        binding.apply {
            txtTempMyAdsMyAds.background =
                ActivityCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)
            txtTempMyFavouriteMyAds.background =
                ActivityCompat.getDrawable(requireContext(), android.R.color.transparent)
            txtTempMyAdsMyAds.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            txtTempMyFavouriteMyAds.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.signUpBtnColor))
            viewModelAdsFragment.hitMyPostsApi(token!!)
        }
    }

    private fun setFaviouriteTab() {
        binding.apply {
            txtTempMyAdsMyAds.background =
                ActivityCompat.getDrawable(requireContext(), android.R.color.transparent)
            txtTempMyFavouriteMyAds.background =
                ActivityCompat.getDrawable(requireContext(), R.drawable.round_sign_up_btn)
            txtTempMyFavouriteMyAds.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.white))
            txtTempMyAdsMyAds.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.signUpBtnColor))
            viewModelAdsFragment.hitMyFavoritesPostsApi(token!!)
        }
    }

}
