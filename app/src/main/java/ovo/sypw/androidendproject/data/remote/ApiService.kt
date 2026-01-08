package ovo.sypw.androidendproject.data.remote

import ovo.sypw.androidendproject.data.model.Banner
import ovo.sypw.androidendproject.data.model.News
import ovo.sypw.androidendproject.data.model.Video
import retrofit2.http.GET
import retrofit2.http.Header
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

    @retrofit2.http.Headers("User-Agent: Mozilla/5.0")
    @GET("https://api.bilibili.com/x/web-interface/popular")
    suspend fun getBilibiliPopular(
        @Query("pn") page: Int = 1,
        @Query("ps") pageSize: Int = 20
    ): ovo.sypw.androidendproject.data.model.BilibiliPopularResponse

    /**
     * 获取 B站分区排行榜
     * @param rid 分区 ID，0 为全站
     * @param type 排行榜类型：all=全部, origin=原创, rookie=新人
     */
    @retrofit2.http.Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    @GET("https://api.bilibili.com/x/web-interface/ranking/v2")
    suspend fun getBilibiliRanking(
        @Query("rid") rid: Int = 0,
        @Query("type") type: String = "all",
        @Header("Cookie") cookie: String = ""
    ): ovo.sypw.androidendproject.data.model.BilibiliRankingResponse
}
