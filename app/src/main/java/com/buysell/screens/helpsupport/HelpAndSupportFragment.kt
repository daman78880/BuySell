package com.buysell.screens.helpsupport

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.databinding.FragmentHelpAndSupportBinding
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import hideKeyboard
import retrofit2.HttpException
import setToolBar


@AndroidEntryPoint
class HelpAndSupportFragment : BaseFragment() {
    private lateinit var binding: FragmentHelpAndSupportBinding

    private val viewModelHelpAndSupport: ViewModelHelpAndSupport by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHelpAndSupportBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val appBarTitle = requireActivity().resources.getString(R.string.help_amp_support)
        setToolBar(binding.appBarHelpAndSuppprt, requireContext(), true, false, appBarTitle, 10f)
        clickListeners()
        binding.apply {
            model=viewModelHelpAndSupport
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelHelpAndSupport.helpAndSupportResponse.collect { data ->
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
                        val response = data.data as JsonObject
                       if(response.has("status")){
                           if(response.get("status").toString().toInt() == 200){
                               findNavController().popBackStack()
                           }
                       }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
    }

    private fun clickListeners() {
        binding.apply {
            viewHelpAndSuppprt.setOnClickListener {
                hideKeyboard()
            }
        }
    }

}