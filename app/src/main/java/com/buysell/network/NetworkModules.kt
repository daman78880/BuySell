package com.buysell.network


import android.content.Context
import com.buysell.BuildConfig
import com.buysell.utils.MyApplication
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModules {
    private val READ_TIMEOUT = 30L  // amit sir set 200 in all field insted cache size bytes // by default set by web is 30,30,10 mints
    private val WRITE_TIMEOUT = 30L
    private val CONNECTION_TIMEOUT = 20L
    private val CACHE_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB
    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): MyApplication{
        return app as MyApplication
        }
        @Provides
        @Singleton
        internal fun provideCache(context: Context): Cache {
            val httpCacheDirectory = File(context.cacheDir.absolutePath, "HttpCache")
            return Cache(httpCacheDirectory, CACHE_SIZE_BYTES)
        }
    @Provides
    @Singleton
    fun provideContext(application: MyApplication): Context {
        return application.applicationContext
    }
    @Singleton
    @Provides
    fun provideGsonBuilder(): Gson {
        return GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
    }
    @Singleton
    @Provides
    fun getClient(cache: Cache):OkHttpClient{
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().
            connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .cache(cache)
            .addInterceptor(interceptor).build()
        return client
    }
    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson,client:OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
    }

    @Singleton
    @Provides
    fun provideApiInterface(retrofit: Retrofit.Builder): ApiInterFace {
        return retrofit.baseUrl(BuildConfig.BASE_URL)
            .build()
            .create(ApiInterFace::class.java)
    }

 @Singleton
    @Provides
    fun provideNotificationApiInterface(retrofit: Retrofit.Builder): NotificationInterface {
        return retrofit.baseUrl(BuildConfig.NOTIFICATION_BASE_URL)
            .build()
            .create(NotificationInterface::class.java)
    }


}
