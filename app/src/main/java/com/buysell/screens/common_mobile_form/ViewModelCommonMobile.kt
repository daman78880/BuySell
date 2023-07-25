package com.buysell.screens.common_mobile_form

import android.util.Log
import android.view.View
import androidx.databinding.ObservableField
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
class ViewModelCommonForm @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    val brandName=ObservableField<String>("Select Brand")

    private val brandResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var brandResponse: StateFlow<ApiState> = brandResponseMutable
    private val varientResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var varientResponse: StateFlow<ApiState> = varientResponseMutable
    private val modelResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var modelResponse: StateFlow<ApiState> = modelResponseMutable
    private val postDetailResponseCommonMobileMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var postDetailResponseCommonMobileResponse: StateFlow<ApiState> = postDetailResponseCommonMobileMutable

    fun hitGetBrandsApi(id:Int) {
        CoroutineScope(Dispatchers.IO).launch {
            brandResponseMutable.value = ApiState.Loading
            repositoryImplementation.getBrands(id)
                .catch { e ->
                    brandResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    brandResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitGetVarientApi(id:Int,categoryId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            varientResponseMutable.value = ApiState.Loading
            repositoryImplementation.getVarient(id,categoryId)
                .catch { e ->
                    varientResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    varientResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitPostDetailCommonMobileApi(token:String,id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            postDetailResponseCommonMobileMutable.value = ApiState.Loading
            repositoryImplementation.postDetail(token,id)
                .catch { e ->
                    postDetailResponseCommonMobileMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    postDetailResponseCommonMobileMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitGetModelApi(id:Int,categoryId:Int) {
        CoroutineScope(Dispatchers.IO).launch {
            modelResponseMutable.value = ApiState.Loading
            repositoryImplementation.getModels(id,categoryId)
                .catch { e ->
                    modelResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    modelResponseMutable.value = ApiState.Success(data)
                }
        }
    }

    }