package ovo.sypw.androidendproject.ui.screens.video

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ovo.sypw.androidendproject.data.model.Video
import ovo.sypw.androidendproject.ui.components.ErrorView
import ovo.sypw.androidendproject.ui.components.LoadingIndicator
import ovo.sypw.androidendproject.ui.components.VideoItem

/**
 * 视频列表页 - 参考示例代码 VideoFragment.java
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    viewModel: VideoViewModel = koinViewModel(),
    onVideoClick: (Video) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val videoList by viewModel.videoList.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = uiState is VideoUiState.Loading,
        onRefresh = { viewModel.refresh() }
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
                    items(videoList, key = { it.id }) { video ->
                        VideoItem(
                            video = video,
                            onClick = { onVideoClick(video) }
                        )
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
