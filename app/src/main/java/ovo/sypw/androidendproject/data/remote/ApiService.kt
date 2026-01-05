package ovo.sypw.androidendproject.data.remote

import ovo.sypw.androidendproject.data.model.Banner
import ovo.sypw.androidendproject.data.model.News
import ovo.sypw.androidendproject.data.model.Video
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API 响应包装类
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

data class NewsListData(
    val banners: List<Banner>,
    val news: List<News>,
    val hasMore: Boolean
)

data class VideoListData(
    val videos: List<Video>,
    val hasMore: Boolean
)

/**
 * API 服务接口
 */
interface ApiService {

    @GET("news/list")
    suspend fun getNewsList(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): ApiResponse<NewsListData>

    @GET("news/banners")
    suspend fun getBanners(): ApiResponse<List<Banner>>

    @GET("video/list")
    suspend fun getVideoList(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): ApiResponse<VideoListData>

    @GET("video/detail")
    suspend fun getVideoDetail(@Query("id") id: String): ApiResponse<Video>

    @GET
    suspend fun getRssFeed(@retrofit2.http.Url url: String): okhttp3.ResponseBody
}
