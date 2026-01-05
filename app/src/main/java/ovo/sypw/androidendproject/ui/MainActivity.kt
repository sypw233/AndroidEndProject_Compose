package ovo.sypw.androidendproject.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import coil3.ImageLoader
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import ovo.sypw.androidendproject.ui.navigation.AppNavigation
import ovo.sypw.androidendproject.ui.theme.AndroidEndProject_ComposeTheme

class MainActivity : ComponentActivity() {

    private var isReady = false

    // 预定义的广告图片 URL
    private val adImageUrls = listOf(
        "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1080&q=80",
        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1080&q=80",
        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1080&q=80",
        "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=1080&q=80",
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080&q=80"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 保持原生 Splash Screen 直到内容准备好
        splashScreen.setKeepOnScreenCondition { !isReady }

        // 预加载广告图片
        preloadAdImages()

        enableEdgeToEdge()

        setContent {
            AndroidEndProject_ComposeTheme {
                AppNavigation()
            }
        }
    }

    private fun preloadAdImages() {
        lifecycleScope.launch {
            try {
                val imageLoader = ImageLoader(this@MainActivity)
                // 预加载所有广告图片
                adImageUrls.forEach { url ->
                    val request = ImageRequest.Builder(this@MainActivity)
                        .data(url)
                        .build()
                    imageLoader.enqueue(request)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // 无论成功与否，都标记为准备完成
                isReady = true
            }
        }
    }
}
