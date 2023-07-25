package com.buysell.network

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiInterFace {
    @POST("/user/signUp")
    suspend fun signUp(@Body jsonObject: JsonObject): JsonObject

    @POST("/user/login")
    suspend fun logIn(@Body jsonObject: JsonObject): JsonObject

    @POST("/user/guestLogin")
    suspend fun guestLogIn(@Body jsonObject: JsonObject): JsonObject

    @POST("/user/loginwithSocialmedia")
    suspend fun loginWithSocialMedia(@Body jsonObject: JsonObject): JsonObject

    @POST("/user/forgotPassword")
    suspend fun forgorPassword(@Body jsonObject: JsonObject): JsonObject

    @GET("/user/postDetails/{id}")
    suspend fun postDetail( @Header("Authorization") token: String, @Path("id") id: Int): JsonObject

    @GET("user/userDetails/{userId}")
    suspend fun userDetail( @Header("Authorization") token: String, @Path("userId") id: Int): JsonObject

    @POST("/user/editProfile")
    suspend fun changeName(
        @Header("Authorization") token: String,
        @Body jsonObject: JsonObject,
    ): JsonObject

    @POST("/user/changePassword")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body jsonObject: JsonObject,
    ): JsonObject


    @POST("/user/verifyNumber")
    suspend fun verifyNumber(
        @Header("Authorization") token: String,
        @Body jsonObject: JsonObject,
    ): JsonObject

    @POST("/user/like")
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Body jsonObject: JsonObject,
    ): JsonObject

    @POST("/user/updateNumber")
    suspend fun updateNumber(
        @Header("Authorization") token: String,
        @Body jsonObject: JsonObject,
    ): JsonObject

    @POST("/user/updateLocation")
    suspend fun updateLocation(
        @Header("Authorization") token: String,
        @Body jsonObject: JsonObject,
    ): JsonObject

    @POST("/user/search")
    suspend fun searchProduct(
        @Header("Authorization") token: String,
        @Body jsonObject: JsonObject,
    ): JsonObject

    @POST("/user/addView")
    suspend fun incrementViewCount(
        @Header("Authorization") token: String,
        @Body jsonObject: JsonObject,
    ): JsonObject

    @Multipart
    @POST("/user/post")
    suspend fun addPost(
        @Header("Authorization") token: String,
        @Part("categoryId") categoryId: Int,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("minBid") minBid: RequestBody,
        @Part("location") location: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("year") year: RequestBody,
        @Part("fuel") fuel: RequestBody,
        @Part("kmDriven") kmDriven: RequestBody,
        @Part("transmission") transmission: RequestBody,
        @Part("numberOfowners") numberOfowners: RequestBody,
        @Part images: ArrayList<MultipartBody.Part?>,
    ): JsonObject

    @Multipart
    @POST("/user/editProfile")
    suspend fun updatePost(
        @Header("Authorization") token: String,
        @Part("categoryId") categoryId: Int,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("minBid") minBid: RequestBody,
        @Part("location") location: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("year") year: RequestBody,
        @Part("fuel") fuel: Int,
        @Part("kmDriven") kmDriven: RequestBody,
        @Part("transmission") transmission: Int,
        @Part("numberOfowners") numberOfowners: RequestBody,
        @Part images: ArrayList<MultipartBody.Part?>,
    ): JsonObject

    @Multipart
    @POST("/user/profilepicUpdate")
    suspend fun uploadProfilePic(
        @Header("Authorization") token: String,
        @Part images: ArrayList<MultipartBody.Part?>,
    ): JsonObject

    @GET("/user/getCategory")
    suspend fun getCategory(@Header("Authorization") token: String): JsonObject

    @POST("/user/browseCategory/{categoryId}")
    suspend fun browseCategory(@Header("Authorization") token: String,@Path("categoryId") categoryId: String,   @Body json:JsonObject): JsonObject

    @POST("/user/browseCategory/{categoryId}")
    suspend fun browseCategoryy(@Header("Authorization") token: String,
                                @Path("categoryId") categoryId: String,
                                @Query("priceFrom") priceFrom: Int,
                                @Query("priceTo") priceTo: Int,
                                @Body json:JsonObject ): JsonObject

    @POST("/user/getmyPosts")
    suspend fun getMyPosts(@Header("Authorization") token: String): JsonObject



    @GET("/user/favorites")
    suspend fun getMyFavoritesPosts(@Header("Authorization") token: String): JsonObject

    @GET("/user/getProfile")
    suspend fun getProfile(@Header("Authorization") token: String):JsonObject

    @GET("/user/getPosts")
    suspend fun getPostsA(@Header("Authorization") token: String
    ): JsonObject

    @GET("/user/getBrands/{categoryId}")
    suspend fun getBrands(@Path("categoryId") categoryId: Int): JsonObject

    @GET("/user/relatedAds/{categoryId}/{id}")
    suspend fun relatedAds(@Header("Authorization") token: String,@Path("categoryId") categoryId: Int,@Path("id") id: Int): JsonObject

    @GET("/user/getModels/{brandId}/{categoryId}")
    suspend fun getModels(
        @Path("brandId") brandId: Int,
        @Path("categoryId") categoryId: Int,
    ): JsonObject

    @GET("/user/getVariant/{modelId}/{categoryId}")
    suspend fun getVariant(
        @Path("modelId") modelId: Int,
        @Path("categoryId") categoryId: Int,
    ): JsonObject

    @HTTP(method = "DELETE", path = "/user/deletePost", hasBody = true)
    suspend fun deleteMyPost(@Header("Authorization") token: String ,  @Body id: JsonObject ): JsonObject

    @DELETE("/user/deleteAccount")
    suspend fun deleteAccount(@Header("Authorization") token: String): JsonObject

/*    @POST("/user/verifyNumber")
    suspend fun updateEmail(@Header("Authorization") token:String,@Body jsonObject: JsonObject): JsonObject*/

    @GET("/user/getLocation")
    suspend fun getLocation(@Header("Authorization") token: String ):JsonObject


    @POST("/user/help")
    suspend fun helpAndSupport(@Header("Authorization") token: String , @Body jsonObject: JsonObject):JsonObject

    @POST("/user/addReport/{id}")
    suspend fun reportAd(@Header("Authorization") token: String ,  @Path("id") id: Int,):JsonObject
}