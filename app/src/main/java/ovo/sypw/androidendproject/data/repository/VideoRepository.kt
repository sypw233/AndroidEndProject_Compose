package ovo.sypw.androidendproject.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ovo.sypw.androidendproject.data.model.Video
import ovo.sypw.androidendproject.data.model.VideoDetail
import ovo.sypw.androidendproject.data.remote.ApiService
import ovo.sypw.androidendproject.data.remote.VideoListData

class VideoRepository(private val apiService: ApiService) {

    fun getVideoList(page: Int = 1, pageSize: Int = 10): Flow<Result<VideoListData>> = flow {
        try {
            val response = apiService.getVideoList(page, pageSize)
            if (response.code == 200 && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            // 网络失败时使用 Mock 数据
            emit(Result.success(getMockData()))
        }
    }

    fun getVideoDetail(id: String): Flow<Result<Video>> = flow {
        try {
            val response = apiService.getVideoDetail(id)
            if (response.code == 200 && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            // 网络失败时使用 Mock 数据
            emit(Result.success(getMockVideoDetail(id)))
        }
    }

    private fun getMockData(): VideoListData {
        return VideoListData(
            videos = Video.mock(),
            hasMore = false
        )
    }

    private fun getMockVideoDetail(id: String): Video {
        // 从 Mock 数据中查找对应 ID 的视频
        return Video.mock().find { it.id == id } ?: Video(
            id = id,
            name = "视频详情",
            coverUrl = "https://picsum.photos/640/360?random=$id",
            intro = "这是视频的详细描述...",
            videoDetailList = listOf(
                VideoDetail("v1", "01-视频教程", "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")
            )
        )
    }
}
