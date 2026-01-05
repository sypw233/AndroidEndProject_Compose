package ovo.sypw.androidendproject.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ovo.sypw.androidendproject.data.model.BilibiliVideo
import ovo.sypw.androidendproject.data.model.Video
import ovo.sypw.androidendproject.data.remote.ApiService

class VideoRepository(private val apiService: ApiService) {

    /**
     * 获取B站热门视频
     */
    fun getBilibiliPopular(page: Int = 1, pageSize: Int = 20): Flow<Result<BilibiliPopularResult>> =
        flow {
            try {
                val response = apiService.getBilibiliPopular(page, pageSize)
                if (response.code == 0 && response.data != null) {
                    emit(
                        Result.success(
                            BilibiliPopularResult(
                                videos = response.data.list,
                                hasMore = !response.data.noMore
                            )
                        )
                    )
                } else {
                    emit(Result.failure(Exception(response.message)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.failure(e))
            }
        }

    /**
     * 获取原始视频列表（Mock 数据）
     */
    fun getVideoList(page: Int = 1, pageSize: Int = 10): Flow<Result<VideoListResult>> = flow {
        try {
            val videos = Video.mock()
            emit(
                Result.success(
                    VideoListResult(
                        videos = videos,
                        hasMore = false  // Mock 数据不支持分页
                    )
                )
            )
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

data class BilibiliPopularResult(
    val videos: List<BilibiliVideo>,
    val hasMore: Boolean
)

data class VideoListResult(
    val videos: List<Video>,
    val hasMore: Boolean
)
