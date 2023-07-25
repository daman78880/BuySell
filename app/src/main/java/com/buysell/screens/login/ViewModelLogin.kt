package com.buysell.screens.login

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.buysell.MainActivity
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.buysell.utils.Extentions.validatePassword
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import hideKeyBoard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelLogin @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    var email = ObservableField<String>("")
    var pwd = ObservableField<String>("")
    var requestFouce = MutableLiveData<Int>()
    private val facebookResponseMutable: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Empty)
    var facebookResponse: StateFlow<ApiState> = facebookResponseMutable

    private val logInResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var logInResponse: StateFlow<ApiState> = logInResponseMutable

    fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin -> {

                checkEmailValidation(view.context)
            }
            R.id.txtForgetPwdLogin -> {
                view.findNavController().navigate(R.id.action_logInFragment_to_forgotPwdFragment)
            }

        }
    }

    fun checkEmailValidation(context: Context) {
        val email = email.get()?.trim().toString()
        val etPasswordLogin = pwd.get()?.trim().toString()
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (etPasswordLogin.isNotEmpty()) {
                val tempPwdResponse = validatePassword(etPasswordLogin, 6, 10)
                if(tempPwdResponse==null) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("email", email)
                    jsonObject.addProperty("password", etPasswordLogin)
                    jsonObject.addProperty("loginType", "4")
                    jsonObject.addProperty("deviceType", "android")
                    hitLoginApi(jsonObject)
                }else{
                    Toast.makeText(context, tempPwdResponse, Toast.LENGTH_SHORT).show()
                }
            }
            else {
                requestFouce.value = 1
                Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show()
            }

        } else {
            requestFouce.value = 0
            Toast.makeText(context, "Please enter valid email", Toast.LENGTH_SHORT).show()
        }
    }

    fun hitLoginApi(jsonObject: JsonObject) {
        Log.i("sadfjnasjklfbnaskldfbs", "login data is $jsonObject")
        CoroutineScope(Dispatchers.IO).launch {
            logInResponseMutable.value = ApiState.Loading
            repositoryImplementation.logIn(jsonObject)
                .catch { e ->
                    logInResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    logInResponseMutable.value = ApiState.Success(data)
                }
        }
    }

    fun hitLoginWithSocialMediaApi(jsonObject: JsonObject) {
        Log.i("asgfjknasfjkdnas", jsonObject.toString())
        CoroutineScope(Dispatchers.IO).launch {
            facebookResponseMutable.value = ApiState.Loading
            repositoryImplementation.logInWithSocialMedia(jsonObject)
                .catch { e ->
                    facebookResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    facebookResponseMutable.value = ApiState.Success(data)
                }
        }
    }
}