package com.buysell.screens.forgotpwd

import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assertk.assertions.support.show
import com.buysell.R
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelForgotPassword @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    var email = ObservableField<String>("")
    private val forgetPwdResponseMutable: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Empty)
    var forgetPwdResponse: StateFlow<ApiState> = forgetPwdResponseMutable

    fun onClick(view: View) {
        when (view.id) {
            R.id.btnVerify -> {
                checkValidation(view)
            }
        }
    }

    private fun hitForgotPwdApi(jsonObject: JsonObject) {
        viewModelScope.launch {
            forgetPwdResponseMutable.value = ApiState.Loading
            repositoryImplementation.forgetPassword(jsonObject)
                .catch { e->
                    forgetPwdResponseMutable.value=ApiState.Failure(e)
                }
                .collect { data ->
                    forgetPwdResponseMutable.value = ApiState.Success(data)
                }
        }
    }

    private fun checkValidation(view:View) {
        val emailAddress = email.get()?.trim().toString()
        if (emailAddress.isNotEmpty()) {
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                val jsonObject = JsonObject()
                jsonObject.addProperty("email", emailAddress)
                hitForgotPwdApi(jsonObject)
            } else {
                Toast.makeText(view.context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(view.context, "Please enter email", Toast.LENGTH_SHORT).show()
        }
    }
}
