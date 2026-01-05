package ovo.sypw.androidendproject.ui.screens.news

import androidx.compose.runtime.Composable
import ovo.sypw.androidendproject.ui.screens.web.WebViewScreen

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
