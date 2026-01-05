package ovo.sypw.androidendproject.ui.screens.video

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import ovo.sypw.androidendproject.data.model.Video
import ovo.sypw.androidendproject.data.model.VideoDetail
import kotlinx.coroutines.delay

/**
 * 视频详情页
 * 全屏逻辑：
 * - 自动模式：旋转至横屏时自动全屏
 * - 手动模式：点击全屏按钮后锁定横屏
 */
@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    videoId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current

    val video = remember(videoId) {
        Video.mock().find { it.id == videoId } ?: Video.mock().first()
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var currentPlayingIndex by remember { mutableIntStateOf(0) }
    var hasStartedPlaying by remember { mutableStateOf(false) }
    // 手动模式：点击全屏按钮后锁定，不再响应旋转
    var isManualMode by remember { mutableStateOf(false) }

    // 根据屏幕方向判断是否全屏
    val isFullscreen = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // ExoPlayer - 只创建一次
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItems = video.videoDetailList.map { MediaItem.fromUri(Uri.parse(it.videoUrl)) }
            setMediaItems(mediaItems)
            videoScalingMode = androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            prepare()
        }
    }

    val tabs = listOf("视频简介", "视频列表")

    // 全屏时隐藏系统UI
    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            hideSystemUI(activity)
        } else {
            showSystemUI(activity)
        }
    }

    // 切换视频
    LaunchedEffect(currentPlayingIndex) {
        if (currentPlayingIndex in 0 until exoPlayer.mediaItemCount) {
            if (currentPlayingIndex != exoPlayer.currentMediaItemIndex) {
                exoPlayer.seekTo(currentPlayingIndex, 0)
                if (hasStartedPlaying) {
                    exoPlayer.play()
                }
            }
        }
    }

    // 返回处理 - 全屏时返回退出全屏
    BackHandler(enabled = isFullscreen) {
        isManualMode = false
        // 先切换到竖屏，然后延迟恢复自由旋转
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // 手动模式退出后，延迟恢复自由旋转
    LaunchedEffect(isManualMode, isFullscreen) {
        if (!isManualMode && !isFullscreen) {
            delay(500) // 等待旋转完成
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // 清理
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            showSystemUI(activity)
        }
    }

    // 播放器视图
    val playerView = remember {
        PlayerView(context).apply {
            player = exoPlayer
            useController = true
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(android.graphics.Color.BLACK)
            setShutterBackgroundColor(android.graphics.Color.BLACK)
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
        }
    }

    // 更新全屏按钮回调
    LaunchedEffect(isFullscreen, isManualMode) {
        playerView.setFullscreenButtonClickListener {
            if (isFullscreen) {
                // 退出全屏 - 先切换竖屏
                isManualMode = false
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                // 进入全屏并锁定横屏
                isManualMode = true
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }

    if (isFullscreen) {
        // 全屏模式
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasStartedPlaying) {
                AndroidView(
                    factory = { playerView },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CoverWithPlayButton(
                    coverUrl = video.coverUrl,
                    onPlay = {
                        hasStartedPlaying = true
                        exoPlayer.play()
                    }
                )
            }
        }
    } else {
        // 普通模式
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(video.name) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 播放器
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    if (hasStartedPlaying) {
                        AndroidView(
                            factory = { playerView },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CoverWithPlayButton(
                            coverUrl = video.coverUrl,
                            onPlay = {
                                hasStartedPlaying = true
                                exoPlayer.play()
                            }
                        )
                    }
                }

                // TabLayout
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Tab 内容
                when (selectedTabIndex) {
                    0 -> VideoIntroContent(intro = video.intro)
                    1 -> VideoListContent(
                        videoList = video.videoDetailList,
                        currentPlayingIndex = currentPlayingIndex,
                        onVideoClick = { index -> currentPlayingIndex = index }
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverWithPlayButton(
    coverUrl: String,
    onPlay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onPlay),
        contentAlignment = Alignment.Center
    ) {
        coil3.compose.AsyncImage(
            model = coverUrl,
            contentDescription = "视频封面",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Icon(
            imageVector = Icons.Default.PlayCircleOutline,
            contentDescription = "播放",
            modifier = Modifier.size(72.dp),
            tint = Color.White.copy(alpha = 0.9f)
        )
    }
}

private fun hideSystemUI(activity: Activity?) {
    activity?.let {
        val window = it.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

private fun showSystemUI(activity: Activity?) {
    activity?.let {
        val window = it.window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
private fun VideoIntroContent(intro: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "课程简介",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.padding(6.dp))
        Text(
            text = intro,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VideoListContent(
    videoList: List<VideoDetail>,
    currentPlayingIndex: Int,
    onVideoClick: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(videoList) { index, videoDetail ->
            VideoListItem(
                videoDetail = videoDetail,
                isPlaying = index == currentPlayingIndex,
                onClick = { onVideoClick(index) }
            )
            if (index < videoList.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun VideoListItem(
    videoDetail: VideoDetail,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircleOutline,
                contentDescription = null,
                tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = videoDetail.videoName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
