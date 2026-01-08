package ovo.sypw.androidendproject.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ovo.sypw.androidendproject.utils.Constants
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.example.com/" // TODO: 替换为实际 API 地址

    val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.NETWORK_CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.NETWORK_READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.NETWORK_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
