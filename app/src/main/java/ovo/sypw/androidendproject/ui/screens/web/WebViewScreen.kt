package ovo.sypw.androidendproject.ui.screens.web

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.kevinnzou.web.AccompanistWebViewClient
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState

/**
 * 通用 WebView 页面 - 使用 compose-webview 库
 * 
 * 支持拦截自定义 URL Scheme（如 bilibili://），首次加载阻止跳转，
 * 后续用户主动点击时弹出确认对话框后才允许跳转到外部应用
 *
 * @param url 要打开的网页 URL
 * @param title 页面标题（可选，为空时从网页获取）
 * @param onBack 关闭页面回调
 */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    title: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state = rememberWebViewState(url = url)
    val navigator = rememberWebViewNavigator()
    
    var pageTitle by remember { mutableStateOf(title) }
    
    // 跟踪页面加载次数，用于区分首次加载和后续导航
    var pageLoadCount by remember { mutableIntStateOf(0) }
    
    // 外部应用跳转确认对话框状态
    var showExternalAppDialog by remember { mutableStateOf(false) }
    var pendingExternalUrl by remember { mutableStateOf<String?>(null) }

    // 监听页面标题变化
    LaunchedEffect(state.pageTitle) {
        if (title.isEmpty() && !state.pageTitle.isNullOrEmpty()) {
            pageTitle = state.pageTitle ?: ""
        }
    }

    // 处理系统返回键
    BackHandler(enabled = navigator.canGoBack) {
        navigator.navigateBack()
    }

    val loadingState = state.loadingState

    // 自定义 WebViewClient 处理 URL Scheme
    val webViewClient = remember {
        object : AccompanistWebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: android.webkit.WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val requestUrl = request?.url?.toString() ?: return false
                Log.d("WebViewScreen", "shouldOverrideUrlLoading: $requestUrl, pageLoadCount=$pageLoadCount")
                
                // 检查是否是自定义 URL Scheme（非 http/https）
                if (!requestUrl.startsWith("http://") && !requestUrl.startsWith("https://")) {
                    // 首次加载时阻止跳转到自定义协议（如 bilibili://）
                    if (pageLoadCount <= 1) {
                        Log.d("WebViewScreen", "阻止首次跳转到自定义协议: $requestUrl")
                        return true // 阻止跳转
                    }
                    
                    // 后续用户主动点击时，显示确认对话框
                    Log.d("WebViewScreen", "请求确认跳转到外部应用: $requestUrl")
                    pendingExternalUrl = requestUrl
                    showExternalAppDialog = true
                    return true // 阻止直接跳转，等待用户确认
                }
                
                // 允许 http/https 链接在 WebView 内加载
                return false
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                pageLoadCount++
                Log.d("WebViewScreen", "onPageFinished: $url, pageLoadCount=$pageLoadCount")
            }
        }
    }

    // 外部应用跳转确认对话框
    if (showExternalAppDialog && pendingExternalUrl != null) {
        val externalUrl = pendingExternalUrl!!
        val appName = getAppNameFromScheme(externalUrl)
        
        AlertDialog(
            onDismissRequest = {
                showExternalAppDialog = false
                pendingExternalUrl = null
            },
            title = { Text("打开外部应用") },
            text = { 
                Text("是否打开 $appName？\n\n将要跳转到：${externalUrl.take(50)}${if (externalUrl.length > 50) "..." else ""}") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExternalAppDialog = false
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.w("WebViewScreen", "无法打开外部应用: $externalUrl", e)
                        }
                        pendingExternalUrl = null
                    }
                ) {
                    Text("打开")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExternalAppDialog = false
                        pendingExternalUrl = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = pageTitle.ifEmpty { "加载中..." },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    // 后退按钮
                    IconButton(
                        onClick = { navigator.navigateBack() },
                        enabled = navigator.canGoBack
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "后退",
                            tint = if (navigator.canGoBack)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                    // 前进按钮
                    IconButton(
                        onClick = { navigator.navigateForward() },
                        enabled = navigator.canGoForward
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "前进",
                            tint = if (navigator.canGoForward)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                    // 刷新按钮
                    IconButton(onClick = { navigator.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    // 在浏览器中打开
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.lastLoadedUrl ?: url))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "在浏览器中打开")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            WebView(
                state = state,
                navigator = navigator,
                client = webViewClient,
                modifier = Modifier.fillMaxSize(),
                onCreated = { webView ->
                    webView.settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                        javaScriptCanOpenWindowsAutomatically = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        allowContentAccess = true
                        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    }
                    webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                }
            )

            // 显示加载进度
            if (loadingState is LoadingState.Loading) {
                LinearProgressIndicator(
                    progress = { loadingState.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * 根据 URL Scheme 获取应用名称
 */
private fun getAppNameFromScheme(url: String): String {
    return when {
        url.startsWith("bilibili://") -> "哔哩哔哩"
        url.startsWith("weixin://") || url.startsWith("wechat://") -> "微信"
        url.startsWith("alipay://") || url.startsWith("alipays://") -> "支付宝"
        url.startsWith("taobao://") -> "淘宝"
        url.startsWith("jd://") -> "京东"
        url.startsWith("douyin://") -> "抖音"
        url.startsWith("snssdk://") -> "今日头条"
        url.startsWith("weibo://") -> "微博"
        url.startsWith("zhihu://") -> "知乎"
        url.startsWith("mqq://") || url.startsWith("mqqapi://") -> "QQ"
        url.startsWith("intent://") -> "外部应用"
        else -> {
            // 尝试从 URL 中提取 scheme 作为应用名
            val scheme = url.substringBefore("://")
            if (scheme.isNotEmpty()) "${scheme}应用" else "外部应用"
        }
    }
}
