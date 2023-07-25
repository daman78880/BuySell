package com.buysell.screens.chat

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ViewModelChatActivity@Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {

    private val msgNotificationResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var msgNotificationResponse: StateFlow<ApiState> = msgNotificationResponseMutable


     fun sendMessage(token: String, contentType: String, json: JsonObject) {
         Log.i(Extentions.TAG,"inside notification funcation called")
         CoroutineScope(Dispatchers.IO).launch {
            msgNotificationResponseMutable.value = ApiState.Loading
            repositoryImplementation.sendPushNotification(token,contentType,json)
                .catch { e ->
                    msgNotificationResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    msgNotificationResponseMutable.value = ApiState.Success(data)
                }
        }
    }

}