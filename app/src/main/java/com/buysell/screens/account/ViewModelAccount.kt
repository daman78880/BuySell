package com.buysell.screens.account

import android.util.Log
import androidx.lifecycle.ViewModel
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ViewModelAccount  @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    private val updateProfilePicResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var updateProfilePicResponse: StateFlow<ApiState> = updateProfilePicResponseMutable

    fun hitUpdateProfileApi(token: String, images:ArrayList<MultipartBody.Part?>) {
        CoroutineScope(Dispatchers.IO).launch {
            updateProfilePicResponseMutable.value = ApiState.Loading
            repositoryImplementation.updateProfilePic(token,images)
                .catch { e ->
                    updateProfilePicResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    updateProfilePicResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    }