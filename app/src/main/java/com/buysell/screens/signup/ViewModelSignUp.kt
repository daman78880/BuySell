package com.buysell.screens.signup

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buysell.R
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
//import com.buysell.utils.ApiStateToast
import com.buysell.utils.Extentions.TAG
import com.buysell.utils.Extentions.validateInput
import com.buysell.utils.Extentions.validatePassword
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelSignUp @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    var namee = ObservableField<String>("")
    var emaill = ObservableField<String>("")
    var pwdd = ObservableField<String>("")
    var confirmPwdd = ObservableField<String>("")
    var showToast = MutableLiveData<String>()
    var requestFouce = MutableLiveData<Int>()
    private val signUpResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var signUpResponse: StateFlow<ApiState> = signUpResponseMutable

    fun onClick(view: View) {
        when (view.id) {
            R.id.btnSignUpp -> {
                checkEmailValidation(view)
            }
        }
    }
    fun checkEmailValidation(view:View) {
        val name = this.namee.get()?.trim().toString()
        val email = this.emaill.get()?.trim().toString()
        val pswd = this.pwdd.get()?.trim().toString()
        val confirmPswd = this.confirmPwdd.get()?.trim().toString()
        if (name.isNotEmpty()) {
            val nameResult=validateInput(name,5,20)
            if(nameResult==null) {
                if (email.isNotEmpty()) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        if (pswd.isNotEmpty()) {
                            val tempPwdResponse = validatePassword(pswd, 6, 10)
                            if(tempPwdResponse==null){
                            if (confirmPswd.isNotEmpty()) {
                                if (pswd == confirmPswd) {
                                    var jsonObject = JsonObject()
                                    jsonObject.addProperty("name", name)
                                    jsonObject.addProperty("email", email)
                                    jsonObject.addProperty("password", pswd)
                                    jsonObject.addProperty("confirmPassword", confirmPswd)
                                    jsonObject.addProperty("loginType", "4")
                                    jsonObject.addProperty("deviceType", "android")
                                    hitSignUpApi(jsonObject)
                                } else {
                                    requestFouce.value = 5
                                    Toast.makeText(
                                        view.context,
                                        "Password didn't match",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    view.context,
                                    "Please enter confirm password",
                                    Toast.LENGTH_SHORT
                                ).show()
                                requestFouce.value = 3
                            }
                        }
                        else {
                            Toast.makeText(view.context, tempPwdResponse, Toast.LENGTH_SHORT).show()
                        }
                        } else {
                            Toast.makeText(
                                view.context,
                                "Please enter password",
                                Toast.LENGTH_SHORT
                            ).show()
                            requestFouce.value = 2
                        }
                    } else {
                        requestFouce.value = 4
                        Toast.makeText(view.context, "Please enter valid email", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(view.context, "Please enter email", Toast.LENGTH_SHORT).show()
                    requestFouce.value = 1
                }
            }else{
                Toast.makeText(view.context, nameResult, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(view.context, "Please enter name", Toast.LENGTH_SHORT).show()
            requestFouce.value = 0
        }
    }

    private fun hitSignUpApi(jsonData: JsonObject) {
        viewModelScope.launch {
            signUpResponseMutable.value = ApiState.Loading
            repositoryImplementation.signUp(jsonData)
               .collect { data ->
                signUpResponseMutable.value = ApiState.Success(data)
            }
        }
    }
}