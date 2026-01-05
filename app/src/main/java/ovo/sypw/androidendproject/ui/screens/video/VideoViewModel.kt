package ovo.sypw.androidendproject.ui.screens.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ovo.sypw.androidendproject.data.model.BilibiliVideo
import ovo.sypw.androidendproject.data.model.Video
import ovo.sypw.androidendproject.data.repository.VideoRepository

class VideoViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {

    // 当前选中的 Tab
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // B站热门状态
    private val _bilibiliUiState = MutableStateFlow<BilibiliUiState>(BilibiliUiState.Loading)
    val bilibiliUiState: StateFlow<BilibiliUiState> = _bilibiliUiState.asStateFlow()

    private val _bilibiliVideos = MutableStateFlow<List<BilibiliVideo>>(emptyList())
    val bilibiliVideos: StateFlow<List<BilibiliVideo>> = _bilibiliVideos.asStateFlow()

    private val _isRefreshingBilibili = MutableStateFlow(false)
    val isRefreshingBilibili: StateFlow<Boolean> = _isRefreshingBilibili.asStateFlow()

    // 原始视频状态
    private val _uiState = MutableStateFlow<VideoUiState>(VideoUiState.Loading)
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    private val _videoList = MutableStateFlow<List<Video>>(emptyList())
    val videoList: StateFlow<List<Video>> = _videoList.asStateFlow()

    // 刷新事件
    private val _refreshEvent = MutableSharedFlow<String>()
    val refreshEvent: SharedFlow<String> = _refreshEvent.asSharedFlow()

    private var bilibiliPage = 1
    private var originalPage = 1
    private var isLoadingMore = false

    init {
        loadBilibiliPopular()
        loadData()
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    // ============ B站热门相关 ============

    fun loadBilibiliPopular() {
        viewModelScope.launch {
            bilibiliPage = 1
            _bilibiliUiState.value = BilibiliUiState.Loading
            videoRepository.getBilibiliPopular(bilibiliPage)
                .catch { e ->
                    _bilibiliUiState.value = BilibiliUiState.Error(e.message ?: "加载失败")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { data ->
                            _bilibiliVideos.value = data.videos
                            _bilibiliUiState.value = BilibiliUiState.Success(
                                videos = data.videos,
                                hasMore = data.hasMore
                            )
                        },
                        onFailure = { e ->
                            _bilibiliUiState.value = BilibiliUiState.Error(e.message ?: "加载失败")
                        }
                    )
                }
        }
    }

    fun refreshBilibili() {
        if (_isRefreshingBilibili.value) return

        viewModelScope.launch {
            _isRefreshingBilibili.value = true
            bilibiliPage = 1

            videoRepository.getBilibiliPopular(bilibiliPage)
                .catch { e ->
                    _isRefreshingBilibili.value = false
                    _refreshEvent.emit("刷新失败: ${e.message}")
                }
                .collect { result ->
                    _isRefreshingBilibili.value = false
                    result.fold(
                        onSuccess = { data ->
                            _bilibiliVideos.value = data.videos
                            _bilibiliUiState.value = BilibiliUiState.Success(
                                videos = data.videos,
                                hasMore = data.hasMore
                            )
                            _refreshEvent.emit("已刷新 ${data.videos.size} 条热门视频")
                        },
                        onFailure = { e ->
                            _refreshEvent.emit("刷新失败: ${e.message}")
                        }
                    )
                }
        }
    }

    fun loadMoreBilibili() {
        if (isLoadingMore) return
        val currentState = _bilibiliUiState.value
        if (currentState is BilibiliUiState.Success && !currentState.hasMore) return

        isLoadingMore = true
        viewModelScope.launch {
            bilibiliPage++
            videoRepository.getBilibiliPopular(bilibiliPage)
                .catch { isLoadingMore = false }
                .collect { result ->
                    result.onSuccess { data ->
                        val current = _bilibiliVideos.value.toMutableList()
                        current.addAll(data.videos)
                        _bilibiliVideos.value = current
                        _bilibiliUiState.value = BilibiliUiState.Success(
                            videos = current,
                            hasMore = data.hasMore
                        )
                    }
                    isLoadingMore = false
                }
        }
    }

    // ============ 原始视频相关 ============

    fun loadData() {
        viewModelScope.launch {
            originalPage = 1
            _uiState.value = VideoUiState.Loading
            videoRepository.getVideoList(originalPage)
                .catch { e ->
                    _uiState.value = VideoUiState.Error(e.message ?: "加载失败")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { data ->
                            _videoList.value = data.videos
                            _uiState.value = VideoUiState.Success(
                                videos = data.videos,
                                hasMore = data.hasMore
                            )
                        },
                        onFailure = { e ->
                            _uiState.value = VideoUiState.Error(e.message ?: "加载失败")
                        }
                    )
                }
        }
    }

    fun refresh() {
        when (_selectedTab.value) {
            0 -> refreshBilibili()
            1 -> loadData()
        }
    }

    fun loadMore() {
        when (_selectedTab.value) {
            0 -> loadMoreBilibili()
            1 -> loadMoreOriginal()
        }
    }

    private fun loadMoreOriginal() {
        if (isLoadingMore) return
        val currentState = _uiState.value
        if (currentState is VideoUiState.Success && !currentState.hasMore) return

        isLoadingMore = true
        viewModelScope.launch {
            originalPage++
            videoRepository.getVideoList(originalPage)
                .catch { isLoadingMore = false }
                .collect { result ->
                    result.onSuccess { data ->
                        val current = _videoList.value.toMutableList()
                        current.addAll(data.videos)
                        _videoList.value = current
                        _uiState.value = VideoUiState.Success(
                            videos = current,
                            hasMore = data.hasMore
                        )
                    }
                    isLoadingMore = false
                }
        }
    }
}


sealed interface BilibiliUiState {
    data object Loading : BilibiliUiState
    data class Success(val videos: List<BilibiliVideo>, val hasMore: Boolean) : BilibiliUiState
    data class Error(val message: String) : BilibiliUiState
}
