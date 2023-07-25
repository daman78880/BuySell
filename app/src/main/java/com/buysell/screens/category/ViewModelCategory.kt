package com.buysell.screens.category

import android.util.Log
import androidx.lifecycle.ViewModel
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
class ViewModelCategory @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    private val categoryResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var categoryResponse: StateFlow<ApiState> = categoryResponseMutable

    fun hitGetCategoryApi(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            categoryResponseMutable.value = ApiState.Loading
            repositoryImplementation.getCategory(token)
                .catch { e ->
                    categoryResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    categoryResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    }