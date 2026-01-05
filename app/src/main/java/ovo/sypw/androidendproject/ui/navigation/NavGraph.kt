package ovo.sypw.androidendproject.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ovo.sypw.androidendproject.ui.screens.intro.IntroScreen
import ovo.sypw.androidendproject.ui.screens.login.LoginScreen
import ovo.sypw.androidendproject.ui.screens.main.MainScreen
import ovo.sypw.androidendproject.ui.screens.map.MapScreen
import ovo.sypw.androidendproject.ui.screens.news.NewsDetailScreen
import ovo.sypw.androidendproject.ui.screens.splash.SplashScreen
import ovo.sypw.androidendproject.ui.screens.video.VideoDetailScreen
import java.net.URLDecoder

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 启动页
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

        // 登录页
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // 新闻详情页
        composable(
            route = Screen.NewsDetail.route,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url = URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", "UTF-8")
            val title = URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8")
            NewsDetailScreen(
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

        // 地图页
        composable(Screen.Map.route) {
            MapScreen(onBack = { navController.popBackStack() })
        }

        // Python课程页
        composable(Screen.Python.route) {
            ovo.sypw.androidendproject.ui.screens.python.PythonScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
