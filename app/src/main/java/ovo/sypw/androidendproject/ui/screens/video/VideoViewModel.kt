package ovo.sypw.androidendproject.ui.screens.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.androidendproject.data.model.Video
import ovo.sypw.androidendproject.data.repository.VideoRepository

/**
 * 视频ViewModel - 参考示例代码 VideoViewModel.java
 */
class VideoViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VideoUiState>(VideoUiState.Loading)
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    private val _videoList = MutableStateFlow<List<Video>>(emptyList())
    val videoList: StateFlow<List<Video>> = _videoList.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = VideoUiState.Loading
            try {
                // 使用 Mock 数据
                val videos = Video.mock()
                _videoList.value = videos
                _uiState.value = VideoUiState.Success(
                    videos = videos,
                    hasMore = false
                )
            } catch (e: Exception) {
                _uiState.value = VideoUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun refresh() = loadData()
}
