package com.buysell.screens.home

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buysell.MainActivity
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
class ViewModelHome @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {

    private val postsResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var postsResponse: StateFlow<ApiState> = postsResponseMutable

    private val categoryHomeResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var categoryHomeResponse: StateFlow<ApiState> =categoryHomeResponseMutable

    private val likePostHomeResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var likePostHomeResponse: StateFlow<ApiState> = likePostHomeResponseMutable

    private val locationHomeResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var locationResponse: StateFlow<ApiState> = locationHomeResponseMutable

    private val locationUpdateHomeResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var locationUpdateResponse: StateFlow<ApiState> = locationUpdateHomeResponseMutable

    fun hitPostsApii(token:String) {
        CoroutineScope(Dispatchers.IO).launch {
            postsResponseMutable.value = ApiState.Loading
            repositoryImplementation.getPostsA(token)
                .catch { e ->
                    postsResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    postsResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun getCategoryHome(token:String) {
        Log.i(Extentions.TAG,"getCategoryHome jsgon OBject ${token}")
        CoroutineScope(Dispatchers.IO).launch {

            categoryHomeResponseMutable.value = ApiState.Loading
            repositoryImplementation.getCategory(token)
                .catch { e ->
                    categoryHomeResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    categoryHomeResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitLikePostHomeApi(token:String,jsonToken: JsonObject) {
        Log.i(Extentions.TAG,"hitLikePostHomeApi jsgon OBject ${token}\njsongObject is $jsonToken")
        CoroutineScope(Dispatchers.IO).launch {
            likePostHomeResponseMutable.value = ApiState.Loading
            repositoryImplementation.likePost(token,jsonToken)
                .catch { e ->
                    likePostHomeResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    likePostHomeResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitLocationGetHomeApi(token:String) {
        CoroutineScope(Dispatchers.IO).launch {
            locationHomeResponseMutable.value = ApiState.Loading
            repositoryImplementation.getLocation(token)
                .catch { e ->
                    locationHomeResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    locationHomeResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitUpdateLocationHomeApi(token:String,jsonToken: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            locationUpdateHomeResponseMutable.value = ApiState.Loading
            repositoryImplementation.updateLocation(token,jsonToken)
                .catch { e ->
                    locationUpdateHomeResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    locationUpdateHomeResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    }