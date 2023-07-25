package com.buysell.screens.browsecategoryresult

import android.util.Log
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
import javax.inject.Inject

@HiltViewModel
class ViewModelBrowseCategoryResult @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    private val browseCategoryHomeResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var browseCategoryHomeResponse: StateFlow<ApiState> = browseCategoryHomeResponseMutable

    private val likePostBrowseCategoryResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var likePostBrowseCategoryResponse: StateFlow<ApiState> = likePostBrowseCategoryResponseMutable

    fun hitBrowseCategoryApi(token:String,categoryId:String,jsonToken: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            browseCategoryHomeResponseMutable.value = ApiState.Loading
            repositoryImplementation.browseCategory(token,categoryId,jsonToken)
                .catch { e ->
                    browseCategoryHomeResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    browseCategoryHomeResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitBrowseCategoryWithPriceApi(token:String,categoryId:String,priceFrom:Int,priceTo:Int,jsonToken: JsonObject) {
        Log.i("asbskanbjkajfnbdsa","\ncategoryId-$categoryId\npriceFrom-$priceFrom\npriceTo-$priceTo,jsonToken-$jsonToken")
        CoroutineScope(Dispatchers.IO).launch {
            browseCategoryHomeResponseMutable.value = ApiState.Loading
            repositoryImplementation.browseCategoryy(token,categoryId,priceFrom,priceTo,jsonToken)
                .catch { e ->
                    browseCategoryHomeResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    browseCategoryHomeResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitLikePostBrowseCategoryApi(token:String,jsonToken: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            likePostBrowseCategoryResponseMutable.value = ApiState.Loading
            repositoryImplementation.likePost(token,jsonToken)
                .catch { e ->
                    likePostBrowseCategoryResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    likePostBrowseCategoryResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    }