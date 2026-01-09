package ovo.sypw.androidendproject.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ovo.sypw.androidendproject.ui.screens.home.python.PythonScreen
import ovo.sypw.androidendproject.ui.screens.intro.IntroScreen
import ovo.sypw.androidendproject.ui.screens.login.LoginScreen
import ovo.sypw.androidendproject.ui.screens.main.MainScreen
import ovo.sypw.androidendproject.ui.screens.me.map.MapScreen
import ovo.sypw.androidendproject.ui.screens.splash.SplashScreen
import ovo.sypw.androidendproject.ui.screens.video.VideoDetailScreen
import ovo.sypw.androidendproject.utils.PreferenceUtils
import java.net.URLDecoder

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    // 根据广告偏好决定起始页面
    // 只有在广告启用 (PreferenceUtils.isAdEnabled) 时才显示 Splash 或 Intro
    val adEnabled = PreferenceUtils.isAdEnabled(context)
    val useGoogleAd = PreferenceUtils.useGoogleAd(context)
    
    val startDestination = if (!adEnabled) {
        // 广告已关闭，直接进入主页或引导页
        if (PreferenceUtils.isFirstLaunch(context)) Screen.Intro.route else Screen.Main.route
    } else if (useGoogleAd) {
        // Google 广告，也直接进入主页或引导页（广告由 AdMob 在 MainActivity 中管理，覆盖在 Activity 上）
        if (PreferenceUtils.isFirstLaunch(context)) Screen.Intro.route else Screen.Main.route
    } else {
        // 使用自定义启动屏（只有在开启广告且不使用 Google 广告时显示）
        Screen.Splash.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 启动页（仅在使用自定义启动屏时显示）
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToIntro = {
                    navController.navigate(Screen.Intro.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 引导页
        composable(Screen.Intro.route) {
            IntroScreen(
                onFinish = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Intro.route) { inclusive = true }
                    }
                }
            )
        }

        // 主页面 (底部导航)
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }

        // 登录页 (使用 FirebaseUI)
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // 通用 WebView 页面
        composable(
            route = Screen.WebView.route,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url = URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", "UTF-8")
            val title =
                URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8")
            ovo.sypw.androidendproject.ui.screens.web.WebViewScreen(
                url = url,
                title = title,
                onBack = { navController.popBackStack() }
            )
        }

        // 视频详情页
        composable(
            route = Screen.VideoDetail.route,
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            VideoDetailScreen(
                videoId = videoId,
                onBack = { navController.popBackStack() }
            )
        }

        // 地图页 (禁用动画，因为 AndroidView 不支持 Compose 动画)
        composable(
            route = Screen.Map.route,
            enterTransition = { androidx.compose.animation.EnterTransition.None },
            exitTransition = { androidx.compose.animation.ExitTransition.None },
            popEnterTransition = { androidx.compose.animation.EnterTransition.None },
            popExitTransition = { androidx.compose.animation.ExitTransition.None }
        ) {
            MapScreen(onBack = { navController.popBackStack() })
        }

        // Python课程页
        composable(Screen.Python.route) {
            PythonScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 设置页
        composable(Screen.Settings.route) {
            ovo.sypw.androidendproject.ui.screens.settings.SettingsScreen(
                onBack = { navController.popBackStack() },
                onDebugUrlOpen = { url ->
                    navController.navigate(Screen.NewsDetail.createRoute(url, "调试"))
                }
            )
        }

        // AI 对话页面
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ovo.sypw.androidendproject.ui.screens.chat.ChatScreen(
                conversationId = conversationId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
