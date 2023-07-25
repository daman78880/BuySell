package com.buysell.screens.welcome

import SharedPref
import android.Manifest
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
import com.buysell.databinding.FragmentLogInBinding
import com.buysell.databinding.FragmentWelcomeBinding
import com.buysell.screens.login.PojoLogin
import com.buysell.screens.login.ViewModelLogin
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException

@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    private lateinit var binding:FragmentWelcomeBinding
    private val viewModelWelcome: ViewModelWelcome by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }
    private fun init(){

        binding.apply {
            model=viewModelWelcome
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModelWelcome.guestResponse.collect { data ->
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

                            val data: PojoGustLogin = Gson().fromJson(response, PojoGustLogin::class.java)
                            if (data.status == 200) {
                                if (data.success) {
                                    SharedPref(requireContext()).saveBoolean(Constant.BOTTOM_BAR_CLICK,true)
                                    SharedPref(requireContext()).saveBoolean(Constant.GUEST_LOGIN_STATUS,true)
                                    SharedPref(requireContext()).saveBoolean(Constant.LOGIN_STATUS, true)
                                    SharedPref(requireContext()).saveString(Constant.TOKEN_kEY, "bearer ${data.token}")
                                    lifecycleScope.launchWhenResumed {
                                        if (findNavController().currentDestination?.id == R.id.welcomeFragment) {
                                            findNavController().navigate(R.id.action_welcomeFragment_to_homeFragment)
                                        }
                                    }
                                }
                            } else if (data.status == 201) {
                                Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                        is ApiState.Empty -> {

                        }
                    }
                }
            }
        }
    }

}