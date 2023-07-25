package com.buysell.screens.welcome

import android.util.Log
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
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
class ViewModelWelcome @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    private val guestResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var guestResponse: StateFlow<ApiState> = guestResponseMutable

    fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogInWelcome -> {
                view.findNavController().navigate(R.id.action_welcomeFragment_to_logInFragment)
            }
            R.id.btnSignUpWelcome -> {
                view.findNavController().navigate(R.id.action_welcomeFragment_to_signUpFragment)
            }
            R.id.txtLoginAsGuestWelcomeScreen -> {
                val jsonObject=JsonObject()
                jsonObject.addProperty("loginType",5)
                jsonObject.addProperty("deviceType","android")
                hitGuestLoginApi(jsonObject)
            }
        }
    }


    fun hitGuestLoginApi(jsonObject: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            guestResponseMutable.value = ApiState.Loading
            repositoryImplementation.guestLogin(jsonObject)
                .catch { e ->
                    guestResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    guestResponseMutable.value = ApiState.Success(data)
                }
        }
    }
}