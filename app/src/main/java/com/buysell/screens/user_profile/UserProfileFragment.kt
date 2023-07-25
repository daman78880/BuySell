package com.buysell.screens.user_profile

import SharedPref
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.databinding.FragmentUserProfileBinding
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException

@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    private lateinit var binding:FragmentUserProfileBinding
    private var userId:Int?=null
    private var idd:Int?=null
    private val viewModelUserProfile: ViewModelUserProfile by viewModels()
    private var token: String?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding= FragmentUserProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    private fun init(){
        token=SharedPref(requireContext()).getString(Constant.TOKEN_kEY)
        userId=arguments?.getInt("userId")
        idd=arguments?.getInt("id")
        viewModelUserProfile.hitPostDetailApi(token!!,userId?:0)

        binding.apply {
            val name=requireActivity().resources.getString(R.string.temp_name)
            tbTitleUserProfile.text=name
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelUserProfile.userDetailResponse.collect { data ->
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
                        }
                        else {
                            Toast.makeText(requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        val data: PojoUserDetail = Gson().fromJson(response, PojoUserDetail::class.java)
                        if (data.status == 200) {
                            binding.txtUserNameUserProfilee.text=data.data[0].User.name
                            binding.txtUserMemberSinceUserProfilee.text=data.data[0].User.memberSince
                            binding.txtUserMemberSinceUserProfilee.text=data.data[0].User.memberSince
                            Glide.with(requireContext()).load(data.data[0].User.profileUrl).into(binding.imgUserImageUserProfilee).onLoadFailed(ContextCompat.getDrawable(requireContext(),R.drawable.no_img_found))
                            binding.rvUserProfile.adapter= AdatperUserProfile(requireContext(),data.data,object :AdatperUserProfile.Click{
                                override fun onClick(id: Int) {
                                    val bundle = Bundle()
                                    bundle.putInt("idP", id)
                                    if(idd==id){
                                     findNavController().popBackStack()
                                    }else {
                                        findNavController().navigate(R.id.action_userProfileFragment_to_productDetailFragment, bundle)
                                    }
                                }
                            })
                            clickListeners()
                        } else {
                            Toast.makeText(requireContext(),
                                "${response.get("message")}",
                                Toast.LENGTH_SHORT).show()
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
            tbLastTxtUserProfile.setOnClickListener {
                Toast.makeText(requireContext(), "Clicked", Toast.LENGTH_SHORT).show()
            }
            tbBackBtnUserProfile.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

}