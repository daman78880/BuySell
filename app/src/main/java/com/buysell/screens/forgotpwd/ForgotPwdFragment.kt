package com.buysell.screens.forgotpwd

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.databinding.FragmentForgotPwdBinding
import com.buysell.databinding.FragmentLogInBinding
import com.buysell.screens.login.PojoLogin
import com.buysell.screens.login.ViewModelLogin
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import hideKeyBoard
import retrofit2.HttpException
import setToolBar


@AndroidEntryPoint
class ForgotPwdFragment : BaseFragment() {

    private lateinit var binding:FragmentForgotPwdBinding
    private val viewModelForgotPassword: ViewModelForgotPassword by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentForgotPwdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    private fun init(){
        setToolBar(binding.appBarForgotPwd,requireContext(),false,false,"",0f)
        binding.model=viewModelForgotPassword
        clickListerer()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelForgotPassword.forgetPwdResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if(data.msg is HttpException){
                            val res=Extentions.getFailedMsg(data.msg)
                            Log.i(Extentions.TAG,"Error ${res?.message}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Log.i(Extentions.TAG,"else Error ${data.msg}")
                            Toast.makeText(requireContext(), "Failed due to ${data.msg}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        val response = data.data as JsonObject
                        val data: PojoForgetPwd = Gson().fromJson(response, PojoForgetPwd::class.java)
                        Log.i(Extentions.TAG, "forgot success ${data}")
                        Extentions.stopProgress()
                        if (data.success) {
                             forgetPwdDialog()
                        }
                        else{
                            Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }


        }
    }
    private fun clickListerer(){
        binding.apply {

            forgetPwdLayout.setOnClickListener {
                requireActivity().hideKeyBoard()
            }
        }
    }

    private fun forgetPwdDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_forget_pwd)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
            android.R.color.transparent))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(false)
        val backBtn: AppCompatImageView =dialog.findViewById(R.id.btnCancelForgetPwdUserDialog)
        backBtn.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.action_forgotPwdFragment_to_logInFragment)
        }
        dialog.show()
    }
}