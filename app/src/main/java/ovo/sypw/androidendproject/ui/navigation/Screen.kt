package ovo.sypw.androidendproject.ui.navigation

import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Intro : Screen("intro")
    data object Main : Screen("main")
    data object Home : Screen("home")
    data object Video : Screen("video")
    data object Chart : Screen("chart")
    data object Me : Screen("me")
    data object Login : Screen("login")
    data object Map : Screen("map")

    // 通用 WebView 页面
    data object WebView : Screen("webview/{url}/{title}") {
        fun createRoute(url: String, title: String = ""): String {
            val encodedUrl = URLEncoder.encode(url, "UTF-8")
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            return "webview/$encodedUrl/$encodedTitle"
        }
    }

    // 保留 NewsDetail 作为别名以保持向后兼容
    data object NewsDetail : Screen("webview/{url}/{title}") {
        fun createRoute(url: String, title: String = ""): String {
            return WebView.createRoute(url, title)
        }
    }

    data object VideoDetail : Screen("video_detail/{videoId}") {
        fun createRoute(videoId: String) = "video_detail/$videoId"
    }

    data object Python : Screen("python")
    data object Settings : Screen("settings")
}
