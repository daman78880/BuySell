package com.buysell.screens.change_pwd

import SharedPref
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentChangePasswordBinding
import com.buysell.databinding.FragmentEditProfileBinding
import com.buysell.screens.editprofile.PojoNameChange
import com.buysell.screens.editprofile.ViewModelEditProfile
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import hideKeyboard
import kotlinx.coroutines.flow.combine
import retrofit2.HttpException
import setToolBar

@AndroidEntryPoint
class ChangePasswordFragment : BaseFragment() {
    private lateinit var binding:FragmentChangePasswordBinding
    private val viewModelChangePwd: ViewModelChangePwd by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
      return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    private fun init(){
        val appBarTitle=requireActivity().resources.getString(R.string.edit_profile)
        setToolBar(binding.appBarChangePwd,requireContext(),true,false,appBarTitle,10f)

        clickListner()
        binding.apply {
            model=viewModelChangePwd

            viewModelChangePwd.changePwdHitApi.observe(viewLifecycleOwner, Observer {
                val data = it as JsonObject?
                if (data != null) {
                    val token=SharedPref(requireContext()).getString(Constant.TOKEN_kEY)
                    viewModelChangePwd.hitChangePasswordApi(token,data)
                }
            })
            lifecycleScope.launchWhenResumed {
                viewModelChangePwd.changePwdResponse.collect { data ->
                    when (data) {
                        is ApiState.Loading -> {
                            Extentions.showProgress(requireContext())
                        }
                        is ApiState.Failure -> {
                            Extentions.stopProgress()
                            if(data.msg is HttpException){
                                val res= Extentions.getFailedMsg(data.msg)
                                Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                Toast.makeText(requireContext(), "Failed due to ${data.msg}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is ApiState.Success -> {
                            Extentions.stopProgress()
                            val response = data.data as JsonObject
                            val data: PojoNameChange = Gson().fromJson(response, PojoNameChange::class.java)
                            if (data.status == 200) {
                                if (data.success) {
                                    etCurrentPwdCngPwd.text?.clear()
                                    etNewPwdCngPwd.text?.clear()
                                    etConfirmPwdCngPwd.text?.clear()
                                    Toast.makeText(requireContext(), "Password changed", Toast.LENGTH_SHORT).show()
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
        }
    }

    private fun clickListner() {
        binding.apply {
            viewChangePwd.setOnClickListener {
                hideKeyboard()
            }
        }
    }



}