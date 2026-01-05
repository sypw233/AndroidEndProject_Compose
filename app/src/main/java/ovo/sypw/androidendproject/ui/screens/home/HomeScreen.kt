package ovo.sypw.androidendproject.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ovo.sypw.androidendproject.data.model.Banner
import ovo.sypw.androidendproject.data.model.News
import ovo.sypw.androidendproject.ui.components.BannerCarousel
import ovo.sypw.androidendproject.ui.components.CategoryButtonRow
import ovo.sypw.androidendproject.ui.components.ErrorView
import ovo.sypw.androidendproject.ui.components.LoadingIndicator
import ovo.sypw.androidendproject.ui.components.NewsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNewsClick: (News) -> Unit,
    onBannerClick: (Banner) -> Unit,
    onCategoryClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val newsList by viewModel.newsList.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // 监听刷新事件
    LaunchedEffect(Unit) {
        viewModel.refreshEvent.collect { result ->
            val message = when (result) {
                is RefreshResult.Success -> result.message
                is RefreshResult.Empty -> result.message
                is RefreshResult.Error -> result.message
            }
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    LoadingIndicator()
                }

                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Banner 轮播图
                        if (banners.isNotEmpty()) {
                            item(key = "banner") {
                                BannerCarousel(
                                    banners = banners,
                                    onBannerClick = onBannerClick
                                )
                            }
                        }

                        // 分类按钮 (Android, Java, PHP, Python)
                        item(key = "categories") {
                            CategoryButtonRow(
                                onCategoryClick = onCategoryClick
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }

                        // 新闻列表
                        items(newsList, key = { it.id }) { news ->
                            NewsItem(
                                news = news,
                                onClick = { onNewsClick(news) }
                            )
                        }

                        // 加载更多
                        if (state.hasMore) {
                            item(key = "load_more") {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMore()
                                }
                                LoadingIndicator(modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }

                is HomeUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadData() }
                    )
                }
            }
        }

        // Snackbar 放在底部
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
