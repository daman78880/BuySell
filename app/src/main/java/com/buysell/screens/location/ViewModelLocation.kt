package com.buysell.screens.location

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ViewModelLocation @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    val location=ObservableField<String>("")
    private val addPostResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var addPostResponse: StateFlow<ApiState> = addPostResponseMutable
    private val updatePostResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var updatePostResponse: StateFlow<ApiState> = updatePostResponseMutable
    private val updateLocationResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var updateLocationResponse: StateFlow<ApiState> = updateLocationResponseMutable

    fun hitAddPostApi(token: String, categoryId:Int , title:RequestBody, description :RequestBody, price:RequestBody , minBid:RequestBody , location:RequestBody , latitude:RequestBody , longitude :RequestBody, brand:RequestBody , year:RequestBody , fuel :RequestBody, kmDriven:RequestBody , transmission:RequestBody , numberOfowners:RequestBody, images:ArrayList<MultipartBody.Part?>) {
        Log.i(Extentions.TAG, "location $location latitude $latitude , longitute $longitude")
        CoroutineScope(Dispatchers.IO).launch {
            addPostResponseMutable.value = ApiState.Loading
            repositoryImplementation.addPost(token, categoryId, title,
                description, price, minBid, location,
                latitude, longitude, brand, year, fuel, kmDriven,
                transmission, numberOfowners, images)
                .catch { e ->
                    addPostResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    addPostResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitUpdatePostApi(token: String, categoryId:Int , title:RequestBody, description :RequestBody, price:RequestBody , minBid:RequestBody , location:RequestBody , latitude:RequestBody , longitude :RequestBody, brand:RequestBody , year:RequestBody , fuel :Int, kmDriven:RequestBody , transmission:Int , numberOfowners:RequestBody, images:ArrayList<MultipartBody.Part?>) {
        Log.i(Extentions.TAG, "location $location latitude $latitude , longitute $longitude")
        CoroutineScope(Dispatchers.IO).launch {
            addPostResponseMutable.value = ApiState.Loading
            repositoryImplementation.updatePost(token, categoryId, title,
                description, price, minBid, location,
                latitude, longitude, brand, year, fuel, kmDriven,
                transmission, numberOfowners, images)
                .catch { e ->
                    addPostResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    addPostResponseMutable.value = ApiState.Success(data)
                }
        }
    }

    fun hitUpdateLocationLocationApi(token:String,jsonToken: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            updateLocationResponseMutable.value = ApiState.Loading
            repositoryImplementation.updateLocation(token,jsonToken)
                .catch { e ->
                    updateLocationResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    updateLocationResponseMutable.value = ApiState.Success(data)
                }
        }
    }

    }