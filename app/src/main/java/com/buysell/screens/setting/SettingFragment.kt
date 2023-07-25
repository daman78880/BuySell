package com.buysell.screens.setting

import SharedPref
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentSettingBinding
import com.buysell.screens.welcome.PojoGustLogin
import com.buysell.screens.welcome.ViewModelWelcome
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.HttpException
import setToolBar


class SettingFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingBinding
    private val viewModelSetting: ViewModelSetting by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.apply {
            model=viewModelSetting
        }
            val appBarTitle=requireActivity().resources.getString(R.string.settings)
            setToolBar(binding.appBarSetting,requireContext(),true,false,appBarTitle,10f)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
            viewModelSetting.deleteAccountResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(Extentions.TAG, "Error ${res?.message}")
                            Toast.makeText(requireContext(),
                                "${res?.message}",
                                Toast.LENGTH_SHORT).show()
                        } else {
                            Log.i(Extentions.TAG, "else Error ${data.msg}")
                            Toast.makeText(requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Log.i(Extentions.TAG, "Login success ${data.data}")
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        if(response.has("status") ){
                            if( response.get("status").toString().toInt() == 200){
                                Extentions.unSubscribedTopic(requireContext())
                                SharedPref(requireContext()).saveBoolean(Constant.BOTTOM_BAR_CLICK,false)
                                SharedPref(requireContext()).saveBoolean(Constant.LOGIN_STATUS, false)
                                SharedPref(requireContext()).clearData()
                                findNavController().navigate(R.id.action_settingFragment_to_welcomeFragment)
                            }
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
    }

}