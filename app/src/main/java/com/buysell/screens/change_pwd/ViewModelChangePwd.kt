package com.buysell.screens.change_pwd

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.buysell.R
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelChangePwd @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    var oldPwd = ObservableField<String>("")
    var newPwd = ObservableField<String>("")
    var confirmPwd = ObservableField<String>("")
    private val changePwdResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var changePwdResponse: StateFlow<ApiState> = changePwdResponseMutable
    var changePwdHitApi=MutableLiveData<JsonObject>()

    fun onClick(view: View) {
        when (view.id) {
            R.id.btnCngPwd -> {
                val oldPwd = oldPwd.get().toString().trim()
                val newPwd = newPwd.get().toString().trim()
                val confirmPwd = confirmPwd.get().toString().trim()
                if (oldPwd.isNotEmpty()) {
                    val tempPwdResponse = Extentions.validatePassword(oldPwd, 6, 10)
                    if (tempPwdResponse == null) {
                        if (newPwd.isNotEmpty()) {
                            val tempNewPwdResponse = Extentions.validatePassword(newPwd, 6, 10)
                            if (tempNewPwdResponse == null) {
                                if (confirmPwd.isNotEmpty()) {
                                    val tempConfirmPwdResponse = Extentions.validatePassword(confirmPwd, 6, 10)
                                    if (tempConfirmPwdResponse == null) {
                                        if (newPwd == confirmPwd) {
                                            val jsonObject = JsonObject()
                                            jsonObject.addProperty("oldPassword", oldPwd)
                                            jsonObject.addProperty("newPassword", newPwd)
                                            jsonObject.addProperty("confirmPassword", confirmPwd)
                                            changePwdHitApi.value = jsonObject
                                        } else {
                                            Toast.makeText(view.context, "Password and Confirm password not matched", Toast.LENGTH_SHORT).show()
                                        }
                                    }else{
                                        Toast.makeText(view.context, tempConfirmPwdResponse, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(view.context, "Please enter confirm password", Toast.LENGTH_SHORT).show()

                                }
                            } else {
                                Toast.makeText(view.context, tempNewPwdResponse, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(view.context, "Please enter new password", Toast.LENGTH_SHORT).show()

                        }
                    } else {
                        Toast.makeText(view.context, tempPwdResponse, Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(view.context, "Please enter password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun hitChangePasswordApi(token:String,jsonObject:JsonObject){
        CoroutineScope(Dispatchers.IO).launch {
            changePwdResponseMutable.value = ApiState.Loading
            repositoryImplementation.changePassword(token,jsonObject)
                .catch { e ->
                    changePwdResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    changePwdResponseMutable.value = ApiState.Success(data)
                }
        }
    }
}