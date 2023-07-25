package com.buysell.screens.search_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ViewModelSearch @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {

    private val postsResponseSearchMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var postsResponseSearch: StateFlow<ApiState> = postsResponseSearchMutable


 fun hitSearchApi(token:String,jsonToken: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            postsResponseSearchMutable.value = ApiState.Loading
            repositoryImplementation.searchProduct(token,jsonToken)
                .catch { e ->
                    postsResponseSearchMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    postsResponseSearchMutable.value = ApiState.Success(data)
                }
        }
    }

    }