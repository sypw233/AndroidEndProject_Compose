package ovo.sypw.androidendproject.ui.screens.video

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import ovo.sypw.androidendproject.data.model.BilibiliVideo
import ovo.sypw.androidendproject.data.model.Video
import ovo.sypw.androidendproject.data.model.formatDuration
import ovo.sypw.androidendproject.data.model.formatViewCount
import ovo.sypw.androidendproject.ui.components.BilibiliVideoItem
import ovo.sypw.androidendproject.ui.components.ErrorView
import ovo.sypw.androidendproject.ui.components.LoadingIndicator
import ovo.sypw.androidendproject.ui.components.VideoItem

/**
 * 视频列表页 - 带 Tab 切换
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    viewModel: VideoViewModel = koinViewModel(),
    onVideoClick: (Video) -> Unit,
    onBilibiliVideoClick: (BilibiliVideo) -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 监听刷新事件
    LaunchedEffect(Unit) {
        viewModel.refreshEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    val tabs = listOf("B站热门", "示例视频")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab 栏
            TabRow(
                selectedTabIndex = selectedTab
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) }
                    )
                }
            }

            // 内容区
            when (selectedTab) {
                0 -> BilibiliPopularTab(
                    viewModel = viewModel,
                    onVideoClick = onBilibiliVideoClick,
                    modifier = Modifier.fillMaxSize()
                )

                1 -> OriginalVideoTab(
                    viewModel = viewModel,
                    onVideoClick = onVideoClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BilibiliPopularTab(
    viewModel: VideoViewModel,
    onVideoClick: (BilibiliVideo) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.bilibiliUiState.collectAsStateWithLifecycle()
    val videos by viewModel.bilibiliVideos.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshingBilibili.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshBilibili() },
        modifier = modifier
    ) {
        when (val state = uiState) {
            is BilibiliUiState.Loading -> {
                LoadingIndicator()
            }

            is BilibiliUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(videos, key = { "bilibili_${it.bvid}" }) { video ->
                        BilibiliVideoItem(
                            video = video,
                            onClick = { onVideoClick(video) }
                        )
                    }

                    // 加载更多
                    if (state.hasMore) {
                        item(key = "bilibili_load_more") {
                            LaunchedEffect(Unit) {
                                viewModel.loadMoreBilibili()
                            }
                            LoadingIndicator(modifier = Modifier.padding(16.dp))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            is BilibiliUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadBilibiliPopular() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OriginalVideoTab(
    viewModel: VideoViewModel,
    onVideoClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val videoList by viewModel.videoList.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = uiState is VideoUiState.Loading,
        onRefresh = { viewModel.loadData() },
        modifier = modifier
    ) {
        when (val state = uiState) {
            is VideoUiState.Loading -> {
                LoadingIndicator()
            }

            is VideoUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                ) {
                    items(videoList, key = { "original_${it.id}" }) { video ->
                        VideoItem(
                            video = video,
                            onClick = { onVideoClick(video) }
                        )
                    }

                    if (state.hasMore) {
                        item(key = "original_load_more") {
                            LaunchedEffect(Unit) {
                                viewModel.loadMore()
                            }
                            LoadingIndicator(modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }

            is VideoUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadData() }
                )
            }
        }
    }
}

