package ovo.sypw.androidendproject.ui.screens.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
    AIChat(Screen.ChatList.route, "AI", Icons.Default.Chat),
    Me(Screen.Me.route, "我的", Icons.Default.Person)
}

@Composable
fun MainScreen(navController: NavHostController) {
    val bottomNavController = rememberNavController()
    val context = LocalContext.current

    // 双击退出逻辑
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val backPressInterval = 2000L // 2秒内双击退出

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < backPressInterval) {
            // 双击退出应用
            (context as? android.app.Activity)?.finish()
        } else {
            // 第一次按返回键，显示提示
            lastBackPressTime = currentTime
            Toast.makeText(context, "再按一次退出应用", Toast.LENGTH_SHORT).show()
        }
    }

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
                        navController.navigate(
                            Screen.NewsDetail.createRoute(
                                news.sourceUrl,
                                news.title
                            )
                        )
                    },
                    onBannerClick = { banner ->
                        navController.navigate(
                            Screen.NewsDetail.createRoute(
                                banner.linkUrl,
                                banner.title
                            )
                        )
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
                ChartScreen(
                    onVideoClick = { rankingItem ->
                        val videoUrl = "https://www.bilibili.com/video/${rankingItem.bvid}"
                        navController.navigate(
                            Screen.NewsDetail.createRoute(videoUrl, rankingItem.title)
                        )
                    }
                )
            }
            composable(Screen.Video.route) {
                VideoScreen(
                    onVideoClick = { video ->
                        navController.navigate(Screen.VideoDetail.createRoute(video.id))
                    },
                    onBilibiliVideoClick = { bilibiliVideo ->
                        val bilibiliUrl = "https://www.bilibili.com/video/${bilibiliVideo.bvid}"
                        navController.navigate(
                            Screen.NewsDetail.createRoute(bilibiliUrl, bilibiliVideo.title)
                        )
                    }
                )
            }
            composable(Screen.ChatList.route) {
                ovo.sypw.androidendproject.ui.screens.chat.ChatListScreen(
                    onConversationClick = { conversationId ->
                        navController.navigate(Screen.Chat.createRoute(conversationId))
                    }
                )
            }
            composable(Screen.Me.route) {
                MeScreen(
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onMapClick = { navController.navigate(Screen.Map.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) }
                )
            }
        }
    }
}
