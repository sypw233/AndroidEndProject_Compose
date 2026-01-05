package ovo.sypw.androidendproject.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ovo.sypw.androidendproject.ui.navigation.Screen
import ovo.sypw.androidendproject.ui.screens.chart.ChartScreen
import ovo.sypw.androidendproject.ui.screens.home.HomeScreen
import ovo.sypw.androidendproject.ui.screens.me.MeScreen
import ovo.sypw.androidendproject.ui.screens.video.VideoScreen

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Home(Screen.Home.route, "首页", Icons.Default.Home),
    Chart(Screen.Chart.route, "图表", Icons.Default.BarChart),
    Video(Screen.Video.route, "视频", Icons.Default.PlayCircle),
    Me(Screen.Me.route, "我的", Icons.Default.Person)
}

@Composable
fun MainScreen(navController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                BottomNavItem.entries.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNewsClick = { news ->
                        navController.navigate(Screen.NewsDetail.createRoute(news.sourceUrl, news.title))
                    },
                    onBannerClick = { banner ->
                        navController.navigate(Screen.NewsDetail.createRoute(banner.linkUrl, banner.title))
                    },
                    onCategoryClick = { categoryId ->
                        // Python 分类跳转到 PythonScreen
                        if (categoryId == "python") {
                            navController.navigate(Screen.Python.route)
                        }
                        // 其他分类暂时不处理
                    }
                )
            }
            composable(Screen.Chart.route) {
                ChartScreen()
            }
            composable(Screen.Video.route) {
                VideoScreen(
                    onVideoClick = { video ->
                        navController.navigate(Screen.VideoDetail.createRoute(video.id))
                    }
                )
            }
            composable(Screen.Me.route) {
                MeScreen(
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onMapClick = { navController.navigate(Screen.Map.route) }
                )
            }
        }
    }
}
