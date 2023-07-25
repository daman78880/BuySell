package com.buysell.screens.signup

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.databinding.FragmentSignUpBinding
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import hideKeyBoard
import retrofit2.HttpException
import setToolBar


@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private val viewModelSignUp: ViewModelSignUp by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        setToolBar(binding.appBarSignUp, requireContext(), false, false, "", 0f)
        setSpanable()
        clickListeners()
        binding.apply {
            model = viewModelSignUp

            // for request focus set on appContext edittext
            viewModelSignUp.requestFouce.observe(viewLifecycleOwner, Observer {
                val value = it
                if (value != null) {
                    when (value) {
                        0 -> {
                            etFullNameSignUp.requestFocus()
                        }
                        1 -> {
                            etEmailSignUp.requestFocus()
                        }
                        2 -> {
                            etPwdSignUp.requestFocus()
                        }
                        3 -> {
                            etConfirmPwdSignUp.requestFocus()
                        }
                        4 -> {
                            etEmailSignUp.requestFocus()
                        }
                        5 -> {
                            etPwdSignUp.requestFocus()
                        }
                        else -> {
                            Toast.makeText(requireContext(), "Else part 4", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
            // Hitting signUpApi
            viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
                viewModelSignUp.signUpResponse.collect { data ->
                    when (data) {
                        is ApiState.Loading -> {
                            Extentions.showProgress(requireContext())
                        }
                        is ApiState.Failure -> {
                            Extentions.stopProgress()
                            if(data.msg is HttpException){
                                val res=Extentions.getFailedMsg(data.msg)
                                Log.i(TAG,"Error ${res?.message}")
                                Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                Log.i(TAG,"else Error ${data.msg}")
                                Toast.makeText(requireContext(), "Failed due to ${data.msg}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is ApiState.Success -> {
                            Log.i(TAG,"signUp success ${data.data}")
                            Extentions.stopProgress()
                            val response=data.data as JsonObject
                            val data: PojoSignUp = Gson().fromJson(response, PojoSignUp::class.java)
                            if(data.success){
                                verifyUserDialog()
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
    }

    private fun clickListeners() {
        binding.apply {
            // hide keyboad on layout click
            signUpFragmentLayout.setOnClickListener {
                requireActivity().hideKeyBoard()
            }
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun showWebView(url:String){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.webview_dialog)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
            android.R.color.transparent))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(true)
        val webView: WebView = dialog.findViewById(R.id.webviewDialog)
        webView.loadUrl(url)
        webView.settings.javaScriptEnabled = true;
        webView.webViewClient = WebViewClient()
        dialog.show()
    }
    // setting spanale text
    private fun setSpanable() {
        val str = requireContext().resources.getString(R.string.signUpTerm)
        val ss = SpannableString(str)
        val span2: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                (textView as AppCompatTextView).highlightColor = ContextCompat.getColor(
                    requireActivity(),
                    android.R.color.transparent)
                showWebView("https://www.google.com/search?q=google+&sxsrf=AJOqlzWFfmFJGaFy-YkipvhrgJMjoL4IGQ%3A1676527740169&ei=fMjtY6v9CeaeseMP-5ioyA8&ved=0ahUKEwirq6LDsJn9AhVmT2wGHXsMCvkQ4dUDCA8&uact=5&oq=google+&gs_lcp=Cgxnd3Mtd2l6LXNlcnAQAzIHCCMQsAMQJzIHCCMQsAMQJzIHCCMQsAMQJzIKCAAQRxDWBBCwAzIKCAAQRxDWBBCwAzIKCAAQRxDWBBCwAzIKCAAQRxDWBBCwAzIKCAAQRxDWBBCwAzIKCAAQRxDWBBCwAzIKCAAQRxDWBBCwA0oECEEYAFDsA1jsA2CmBWgBcAF4AIABAIgBAJIBAJgBAKABAcgBCsABAQ&sclient=gws-wiz-serp")
            }

            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = requireActivity().resources.getColor(R.color.signUpBtnColor)
            }

        }
        val span4: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                (textView as AppCompatTextView).highlightColor = ContextCompat.getColor(
                    requireActivity(),
                    android.R.color.transparent)
                showWebView("https://www.google.com/search?q=honda+bikes&sxsrf=AJOqlzXeF1egXe9ozRMJdiZL8vBU1UTOLQ:1676527767360&source=lnms&tbm=isch&sa=X&ved=2ahUKEwip-J3QsJn9AhVRcGwGHecuB7wQ_AUoAXoECAEQAw&biw=1360&bih=625&dpr=1")
            }

            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = ContextCompat.getColor(requireContext(),R.color.signUpBtnColor)
            }
        }
        ss.setSpan(span2, 38, 58, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(span4, 63, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.txtSignUpLogin.text = ss
        binding.txtSignUpLogin.highlightColor = ContextCompat.getColor(requireActivity(),
            android.R.color.transparent)
        binding.txtSignUpLogin.movementMethod = LinkMovementMethod.getInstance()
    }
    // for verify email dialog show
    private fun verifyUserDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_verify_accout)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
            android.R.color.transparent))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(false)
        val backBtn: AppCompatImageView = dialog.findViewById(R.id.btnCancelVerifyUserDialog)
        backBtn.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.action_signUpFragment_to_logInFragment)
        }
        dialog.show()
    }
}