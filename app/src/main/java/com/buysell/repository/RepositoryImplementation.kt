package com.buysell.repository

import android.util.Log
import androidx.lifecycle.flowWithLifecycle
import com.buysell.network.ApiInterFace
import com.buysell.network.NotificationInterface
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class RepositoryImplementation @Inject constructor(private val apiServicee: ApiInterFace,private val notificationService:NotificationInterface) {

    suspend fun signUp(
        jsonObject: JsonObject,
    ): Flow<JsonObject> = flow {
        emit(apiServicee.signUp(jsonObject))
    }.flowOn(Dispatchers.IO)
    suspend fun logIn(
        jsonObject: JsonObject,
    ): Flow<JsonObject> = flow {
        emit(apiServicee.logIn(jsonObject))
    }.flowOn(Dispatchers.IO)

    suspend fun logInWithSocialMedia(
        jsonObject: JsonObject,
    ): Flow<JsonObject> = flow {
        emit(apiServicee.loginWithSocialMedia(jsonObject))
    }.flowOn(Dispatchers.IO)

    suspend fun forgetPassword(
        jsonObject: JsonObject,
    ): Flow<JsonObject> = flow {
        emit(apiServicee.forgorPassword(jsonObject))
    }
    suspend fun changeName(token:String,
                           jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.changeName(token,jsonObject))
    }
    suspend fun changePassword(token:String,
                           jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.changePassword(token,jsonObject))
    }
    suspend fun postDetail(token:String,
                           id: Int
    ): Flow<JsonObject> = flow {
        emit(apiServicee.postDetail(token,id))
    }
    suspend fun userDetail(token:String,
                           id: Int
    ): Flow<JsonObject> = flow {
        emit(apiServicee.userDetail(token,id))
    }
    suspend fun guestLogin(
        jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.guestLogIn(jsonObject))
    }
    suspend fun deleteAccount(
        token:String
    ): Flow<JsonObject> = flow {
        emit(apiServicee.deleteAccount(token))
    }
    suspend fun deleteMyPost(
        token:String,jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.deleteMyPost(token,jsonObject))
    }
    suspend fun verifyNumber(
        token:String,jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.verifyNumber(token,jsonObject))
    }
    suspend fun likePost(
        token:String,jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.likePost(token,jsonObject))
    }
    suspend fun updateNumber(
        token:String,jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.updateNumber(token,jsonObject))
    }
    suspend fun updateLocation(
        token:String,jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.updateLocation(token,jsonObject))
    }
    suspend fun searchProduct(
        token:String,jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.searchProduct(token,jsonObject))
    }
    suspend fun incrementViewCount(
        token:String,jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.incrementViewCount(token,jsonObject))
    }
    suspend fun getCategory(
        token: String,
    ): Flow<JsonObject> = flow {
        emit(apiServicee.getCategory(token))
    }

    suspend fun browseCategory(
        token: String,
        categoryId:String,
        jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.browseCategory(token,categoryId,jsonObject))
    }
    suspend fun browseCategoryy(
        token: String,
        categoryId:String,
        priceFrom:Int,priceTo:Int,
        jsonObject: JsonObject
    ): Flow<JsonObject> = flow {
        emit(apiServicee.browseCategoryy(token,categoryId,priceFrom,priceTo,jsonObject))
    }
    suspend fun getMyPosts(
        token: String,
    ): Flow<JsonObject> = flow {
        emit(apiServicee.getMyPosts(token))
    }
    suspend fun getMyFavoritesPosts(
        token: String,
    ): Flow<JsonObject> = flow {
        emit(apiServicee.getMyFavoritesPosts(token))
    }
    suspend fun getPostsA(
        token: String
    ): Flow<JsonObject> = flow {
        emit(apiServicee.getPostsA(token))
    }

    suspend fun addPost(
        token: String,
        categoryId: Int, title:RequestBody, description :RequestBody, price:RequestBody, minBid:RequestBody, location:RequestBody, latitude:RequestBody, longitude :RequestBody, brand:RequestBody, year:RequestBody, fuel :RequestBody, kmDriven:RequestBody, transmission:RequestBody, numberOfowners:RequestBody, images:ArrayList<MultipartBody.Part?>
    ): Flow<JsonObject> = flow {
        emit(apiServicee.addPost(token, categoryId, title, description, price, minBid,location, latitude , longitude,brand, year, fuel, kmDriven, transmission, numberOfowners,images))
    }
    suspend fun updatePost(
        token: String,
        categoryId: Int, title:RequestBody, description :RequestBody, price:RequestBody, minBid:RequestBody, location:RequestBody, latitude:RequestBody, longitude :RequestBody, brand:RequestBody, year:RequestBody, fuel :Int, kmDriven:RequestBody, transmission:Int, numberOfowners:RequestBody, images:ArrayList<MultipartBody.Part?>
    ): Flow<JsonObject> = flow {
        emit(apiServicee.updatePost(token, categoryId, title, description, price, minBid,location, latitude , longitude,brand, year, fuel, kmDriven, transmission, numberOfowners,images))
    }
    suspend fun updateProfilePic(
        token: String,
    images:ArrayList<MultipartBody.Part?>
    ): Flow<JsonObject> = flow {
        emit(apiServicee.uploadProfilePic(token, images))
    }
    suspend fun getBrands(categoryId:Int): Flow<JsonObject> = flow {
        emit(apiServicee.getBrands(categoryId))
    }
    suspend fun relatedId(token: String,categoryId:Int,id:Int): Flow<JsonObject> = flow {
        emit(apiServicee.relatedAds(token,categoryId,id))
    }
    suspend fun getModels(brandId:Int,categoryId: Int): Flow<JsonObject> = flow {
        emit(apiServicee.getModels(brandId,categoryId))
    }

    suspend fun getVarient(brandId:Int,categoryId: Int): Flow<JsonObject> = flow {
        emit(apiServicee.getVariant(brandId,categoryId))
    }

    suspend fun helpAndSupport(token:String,jsonObject: JsonObject): Flow<JsonObject> = flow {
        emit(apiServicee.helpAndSupport(token,jsonObject))
    }
    suspend fun reportAd(token:String,id: Int): Flow<JsonObject> = flow {
        emit(apiServicee.reportAd(token,id))
    }
    suspend fun getLocation(token:String): Flow<JsonObject> = flow {
        emit(apiServicee.getLocation(token))
    }

    suspend fun getProfile(
        token: String,
    ): Flow<JsonObject> = flow {
        emit(apiServicee.getProfile(token))
    }
    suspend fun sendPushNotification(token: String, contentType: String, json: JsonObject):Flow<JsonObject> = flow{
        emit(notificationService.sendMessage(token,contentType,json))
    }
    suspend fun deleteSendPushNotification(token: String, contentType: String, json: JsonObject):Flow<JsonObject> = flow{
        emit(notificationService.sendMessage(token,contentType,json))

    }

    }


