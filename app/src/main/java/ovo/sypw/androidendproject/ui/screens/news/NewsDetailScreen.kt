package ovo.sypw.androidendproject.ui.screens.news

import ovo.sypw.androidendproject.ui.screens.web.WebViewScreen
import androidx.compose.runtime.Composable

/**
 * 新闻详情页 - 复用 WebViewScreen
 */
@Composable
fun NewsDetailScreen(
    url: String,
    title: String,
    onBack: () -> Unit
) {
    WebViewScreen(
        url = url,
        title = title,
        onBack = onBack
    )
}
