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
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import ovo.sypw.androidendproject.utils.PreferenceUtils
import kotlin.random.Random

enum class AdType {
    IMAGE, VIDEO
}

data class SplashAd(
    val type: AdType,
    val url: String,
    val duration: Int = 5 // 秒
)

@Composable
fun SplashScreen(
    onNavigateToIntro: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    var countdown by remember { mutableIntStateOf(5) }
    var adFinished by remember { mutableStateOf(false) }

    // 随机选择广告类型
//    val splashAd = remember {
//        if (Random.nextBoolean()) {
//            SplashAd(
//                type = AdType.IMAGE,
//                url = "https://picsum.photos/1080/1920?random=${System.currentTimeMillis()}",
//                duration = 5
//            )
//        }
//        else {
//            SplashAd(
//                type = AdType.VIDEO,
//                url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
//                duration = 5
//            )
//        }
//    }

    val splashAd = remember{
        SplashAd(
            type = AdType.IMAGE,
            url = "https://picsum.photos/1080/1920?random=${System.currentTimeMillis()}",
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
        // 广告内容
        when (splashAd.type) {
            AdType.IMAGE -> {
                AsyncImage(
                    model = splashAd.url,
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
                .padding(16.dp)
                .clickable {
                    adFinished = true
                    navigateNext(context, onNavigateToIntro, onNavigateToMain)
                },
            shape = RoundedCornerShape(80.dp),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Text(
                text = if (countdown > 0) "跳过 $countdown" else "跳过",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
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
