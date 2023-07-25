package com.buysell.screens.productdetail

import android.util.JsonToken
import android.util.Log
import android.view.View
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
class ViewModelProductDetail @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    private val postDetailResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var postDetailResponse: StateFlow<ApiState> = postDetailResponseMutable

    private val incrementViewResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var incrementViewResponse: StateFlow<ApiState> = incrementViewResponseMutable

    private val likePostResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var likePostResponse: StateFlow<ApiState> = likePostResponseMutable

    private val likeRelatedAdsPostResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var likeRelatedAdsPostResponse: StateFlow<ApiState> = likeRelatedAdsPostResponseMutable

    private val deletePostResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var deletePostResponse: StateFlow<ApiState> = deletePostResponseMutable

    private val relatedAdsPostResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var relatedAdsPostResponse: StateFlow<ApiState> = relatedAdsPostResponseMutable


    fun hitPostDetailApi(token:String,id: Int) {
        Log.i(Extentions.TAG,"jsgon OBject ${token}\njsongObject is $id")
        CoroutineScope(Dispatchers.IO).launch {
            postDetailResponseMutable.value = ApiState.Loading
            repositoryImplementation.postDetail(token,id)
                .catch { e ->
                    postDetailResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    postDetailResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitLikePostApi(token:String,jsonToken: JsonObject) {
        Log.i(Extentions.TAG,"jsgon OBject ${token}\njsongObject is $jsonToken")
        CoroutineScope(Dispatchers.IO).launch {
            likePostResponseMutable.value = ApiState.Loading
            repositoryImplementation.likePost(token,jsonToken)
                .catch { e ->
                    likePostResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    likePostResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitRelatedLikePostApi(token:String,jsonToken: JsonObject) {
        Log.i(Extentions.TAG,"jsgon OBject ${token}\njsongObject is $jsonToken")
        CoroutineScope(Dispatchers.IO).launch {
            likeRelatedAdsPostResponseMutable.value = ApiState.Loading
            repositoryImplementation.likePost(token,jsonToken)
                .catch { e ->
                    likeRelatedAdsPostResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    likeRelatedAdsPostResponseMutable.value = ApiState.Success(data)
                }
        }
    }

    fun hitDeletePostApi(token:String,jsonToken: JsonObject) {
        Log.i(Extentions.TAG,"jsgon OBject ${token}\njsongObject is $jsonToken")
        CoroutineScope(Dispatchers.IO).launch {
            deletePostResponseMutable.value = ApiState.Loading
            repositoryImplementation.deleteMyPost(token,jsonToken)
                .catch { e ->
                    deletePostResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    deletePostResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitIncrementPostApi(token:String,jsonToken: JsonObject) {
        Log.i(Extentions.TAG,"jsgon OBject ${token}\njsongObject is $jsonToken")
        CoroutineScope(Dispatchers.IO).launch {
            incrementViewResponseMutable.value = ApiState.Loading
            repositoryImplementation.incrementViewCount(token,jsonToken)
                .catch { e ->
                    incrementViewResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    incrementViewResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitRelatedAdsPostApi(token:String,categoryId:Int,id:Int) {
        CoroutineScope(Dispatchers.IO).launch {
            relatedAdsPostResponseMutable.value = ApiState.Loading
            repositoryImplementation.relatedId(token,categoryId,id)
                .catch { e ->
                    relatedAdsPostResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    relatedAdsPostResponseMutable.value = ApiState.Success(data)
                }
        }
    }

    }