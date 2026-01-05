package ovo.sypw.androidendproject.ui.screens.home

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
import ovo.sypw.androidendproject.data.model.Banner
import ovo.sypw.androidendproject.data.model.News
import ovo.sypw.androidendproject.data.repository.NewsRepository

class HomeViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    private val _newsList = MutableStateFlow<List<News>>(emptyList())
    val newsList: StateFlow<List<News>> = _newsList.asStateFlow()

    // 独立的刷新状态
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // 刷新结果事件
    private val _refreshEvent = MutableSharedFlow<RefreshResult>()
    val refreshEvent: SharedFlow<RefreshResult> = _refreshEvent.asSharedFlow()

    private var currentPage = 1
    private var isLoadingMore = false

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            currentPage = 1
            _uiState.value = HomeUiState.Loading
            newsRepository.getNewsList(currentPage)
                .catch { e ->
                    loadMockData()
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { data ->
                            _banners.value = data.banners
                            _newsList.value = data.news
                            _uiState.value = HomeUiState.Success(
                                banners = _banners.value,
                                news = _newsList.value,
                                hasMore = data.hasMore
                            )
                        },
                        onFailure = { e ->
                            loadMockData()
                        }
                    )
                }
        }
    }

    private fun loadMockData() {
        _banners.value = Banner.mock()
        _newsList.value = News.mock(1)
        _uiState.value = HomeUiState.Success(
            banners = _banners.value,
            news = _newsList.value,
            hasMore = false
        )
    }

    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            currentPage = 1

            try {
                newsRepository.refreshNews()
                    .catch { e ->
                        _isRefreshing.value = false
                        _refreshEvent.emit(RefreshResult.Error("刷新失败: ${e.message}"))
                    }
                    .collect { result ->
                        _isRefreshing.value = false
                        result.fold(
                            onSuccess = { refreshData ->
                                val data = refreshData.newsListData
                                val newCount = refreshData.newItemsCount

                                _banners.value = data.banners
                                _newsList.value = data.news
                                _uiState.value = HomeUiState.Success(
                                    banners = _banners.value,
                                    news = _newsList.value,
                                    hasMore = data.hasMore
                                )

                                when {
                                    newCount > 0 -> _refreshEvent.emit(RefreshResult.Success("发现 $newCount 条新内容"))
                                    data.news.isNotEmpty() -> _refreshEvent.emit(
                                        RefreshResult.Empty(
                                            "暂无新内容"
                                        )
                                    )

                                    else -> _refreshEvent.emit(RefreshResult.Empty("暂无内容"))
                                }
                            },
                            onFailure = { e ->
                                _refreshEvent.emit(RefreshResult.Error("刷新失败"))
                            }
                        )
                    }
            } catch (e: Exception) {
                _isRefreshing.value = false
                _refreshEvent.emit(RefreshResult.Error("刷新失败: ${e.message}"))
            }
        }
    }

    fun loadMore() {
        if (isLoadingMore) return
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success && !currentState.hasMore) return

        isLoadingMore = true

        viewModelScope.launch {
            currentPage++
            newsRepository.getNewsList(currentPage)
                .catch { isLoadingMore = false }
                .collect { result ->
                    result.onSuccess { data ->
                        val currentNews = _newsList.value.toMutableList()
                        currentNews.addAll(data.news)
                        _newsList.value = currentNews
                        _uiState.value = HomeUiState.Success(
                            banners = _banners.value,
                            news = currentNews,
                            hasMore = data.hasMore
                        )
                    }
                    isLoadingMore = false
                }
        }
    }
}

sealed interface RefreshResult {
    data class Success(val message: String) : RefreshResult
    data class Empty(val message: String) : RefreshResult
    data class Error(val message: String) : RefreshResult
}
