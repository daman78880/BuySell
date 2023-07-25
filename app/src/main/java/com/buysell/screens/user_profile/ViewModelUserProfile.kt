package com.buysell.screens.user_profile

import android.util.Log
import androidx.lifecycle.ViewModel
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelUserProfile @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    private val userDetailResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var userDetailResponse: StateFlow<ApiState> = userDetailResponseMutable




    fun hitPostDetailApi(token:String,userId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            userDetailResponseMutable.value = ApiState.Loading
            repositoryImplementation.userDetail(token,userId)
                .catch { e ->
                    userDetailResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    userDetailResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    }