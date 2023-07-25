package com.buysell.screens.myads

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import com.buysell.R
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
class ViewModelAdsFragment  @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {

    private val myPostsResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var myPostsResponse: StateFlow<ApiState> = myPostsResponseMutable

    private val myFavouritePostsResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var myFavouritePostsResponse: StateFlow<ApiState> = myFavouritePostsResponseMutable

    private val myFavouriteLikeResponseMutable: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    var myFavouriteLikePostsResponse: StateFlow<ApiState> = myFavouriteLikeResponseMutable

    fun onClick(view:View){
        when(view.id){
            R.id.txtTempMyAdsMyAds ->{

            }
            R.id.txtTempMyFavouriteMyAds ->{

            }
        }
    }

    fun hitMyPostsApi(token:String) {
        CoroutineScope(Dispatchers.IO).launch {
            myPostsResponseMutable.value = ApiState.Loading
            repositoryImplementation.getMyPosts(token)
                .catch { e ->
                    myPostsResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    myPostsResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitMyFavoritesPostsApi(token:String) {
        CoroutineScope(Dispatchers.IO).launch {
            myFavouritePostsResponseMutable.value = ApiState.Loading
            repositoryImplementation.getMyFavoritesPosts(token)
                .catch { e ->
                    myFavouritePostsResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    myFavouritePostsResponseMutable.value = ApiState.Success(data)
                }
        }
    }
    fun hitLikePostAdsApi(token:String,jsonToken: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            myFavouriteLikeResponseMutable.value = ApiState.Loading
            repositoryImplementation.likePost(token,jsonToken)
                .catch { e ->
                    myFavouriteLikeResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    myFavouriteLikeResponseMutable.value = ApiState.Success(data)
                }
        }
    }
}