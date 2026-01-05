package ovo.sypw.androidendproject.ui.screens.splash

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.delay
import ovo.sypw.androidendproject.utils.PreferenceUtils
import androidx.core.net.toUri

enum class AdType {
    IMAGE, VIDEO
}

data class SplashAd(
    val type: AdType,
    val url: String,
    val duration: Int = 5 // 秒
)

// 预定义的广告图片 URL（与 MainActivity 预加载的相同）
private val preloadedAdImages = listOf(
    "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1080&q=80",
    "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1080&q=80",
    "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1080&q=80",
    "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=1080&q=80",
    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080&q=80"
)

@Composable
fun SplashScreen(
    onNavigateToIntro: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    var countdown by remember { mutableIntStateOf(5) }
    var adFinished by remember { mutableStateOf(false) }

    // 选择一个预加载的图片 URL（图片已在 MainActivity 中预加载到缓存）
    val splashAd = remember {
        val index = (System.currentTimeMillis() % preloadedAdImages.size).toInt()
        SplashAd(
            type = AdType.IMAGE,
            url = preloadedAdImages[index],
            duration = 5
        )
    }

    // 倒计时
    LaunchedEffect(Unit) {
        while (countdown > 0 && !adFinished) {
            delay(1000L)
            countdown--
        }
        if (!adFinished) {
            adFinished = true
            navigateNext(context, onNavigateToIntro, onNavigateToMain)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 广告内容（图片已预加载，会立即显示）
        when (splashAd.type) {
            AdType.IMAGE -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(splashAd.url)
                        .crossfade(200)
                        .build(),
                    contentDescription = "开屏广告",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            AdType.VIDEO -> {
                SplashVideoPlayer(
                    videoUrl = splashAd.url,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 跳过按钮
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(48.dp)
                .clickable {
                    adFinished = true
                    navigateNext(context, onNavigateToIntro, onNavigateToMain)
                },
            shape = RoundedCornerShape(64.dp),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Text(
                text = if (countdown > 0) "跳过 $countdown" else "跳过",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 80.sp,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
            )
        }
    }
}

private fun navigateNext(
    context: android.content.Context,
    onNavigateToIntro: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    if (PreferenceUtils.isFirstLaunch(context)) {
        onNavigateToIntro()
    } else {
        onNavigateToMain()
    }
}

@OptIn(UnstableApi::class)
@Composable
fun SplashVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl.toUri()))
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )
    }
}
