package ovo.sypw.androidendproject.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ovo.sypw.androidendproject.utils.PreferenceUtils

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onDebugUrlOpen: (String) -> Unit
) {
    val context = LocalContext.current

    // 广告设置状态
    var adEnabled by remember {
        mutableStateOf(
            PreferenceUtils.getBoolean(
                context,
                "ad_enabled",
                true
            )
        )
    }
    var useGoogleAd by remember { mutableStateOf(PreferenceUtils.useGoogleAd(context)) }

    // Debug URL 对话框状态
    var showDebugDialog by remember { mutableStateOf(false) }
    var debugUrl by remember { mutableStateOf("https://www.bilibili.com/video/BV1tz421i7zb") }

    // Debug URL 对话框
    if (showDebugDialog) {
        AlertDialog(
            onDismissRequest = { showDebugDialog = false },
            title = { Text("调试 URL") },
            text = {
                OutlinedTextField(
                    value = debugUrl,
                    onValueChange = { debugUrl = it },
                    label = { Text("输入 URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            },
            confirmButton = {
                Button(onClick = {
                    showDebugDialog = false
                    onDebugUrlOpen(debugUrl)
                }) {
                    Text("打开")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDebugDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ========== 广告设置 ==========
            SettingsSectionHeader(title = "广告设置")

            SettingsToggleItem(
                icon = Icons.Default.AdsClick,
                title = "开屏广告",
                subtitle = if (adEnabled) "已启用" else "已禁用",
                checked = adEnabled,
                onCheckedChange = { enabled ->
                    adEnabled = enabled
                    PreferenceUtils.putBoolean(context, "ad_enabled", enabled)
                    if (!enabled) {
                        PreferenceUtils.setUseGoogleAd(context, false)
                    }
                }
            )

            // 只有在广告开启时才显示 Google 广告选项
            if (adEnabled) {
                SettingsToggleItem(
                    icon = Icons.Default.PlayCircle,
                    title = "使用 Google 开屏广告",
                    subtitle = if (useGoogleAd) "Google AdMob 广告" else "自定义启动屏图片",
                    checked = useGoogleAd,
                    onCheckedChange = { enabled ->
                        useGoogleAd = enabled
                        PreferenceUtils.setUseGoogleAd(context, enabled)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ========== B站设置 ==========
            SettingsSectionHeader(title = "B站设置")

            // B站 Cookies 状态
            var bilibiliCookies by remember {
                mutableStateOf(PreferenceUtils.getBilibiliCookies(context))
            }
            var showCookiesDialog by remember { mutableStateOf(false) }
            var tempCookies by remember { mutableStateOf("") }

            // B站 Cookies 对话框
            if (showCookiesDialog) {
                AlertDialog(
                    onDismissRequest = { showCookiesDialog = false },
                    title = { Text("设置 B站 Cookies") },
                    text = {
                        Column {
                            Text(
                                text = "在浏览器登录 B站后，从开发者工具复制 Cookie 值粘贴到此处。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = tempCookies,
                                onValueChange = { tempCookies = it },
                                label = { Text("Cookies") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = false,
                                maxLines = 5,
                                placeholder = { Text("SESSDATA=xxx; bili_jct=xxx; ...") }
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            bilibiliCookies = tempCookies
                            PreferenceUtils.setBilibiliCookies(context, tempCookies)
                            showCookiesDialog = false
                        }) {
                            Text("保存")
                        }
                    },
                    dismissButton = {
                        Row {
                            if (bilibiliCookies.isNotBlank()) {
                                TextButton(onClick = {
                                    tempCookies = ""
                                    bilibiliCookies = ""
                                    PreferenceUtils.setBilibiliCookies(context, "")
                                    showCookiesDialog = false
                                }) {
                                    Text("清除", color = MaterialTheme.colorScheme.error)
                                }
                            }
                            TextButton(onClick = { showCookiesDialog = false }) {
                                Text("取消")
                            }
                        }
                    }
                )
            }

            SettingsClickItem(
                icon = Icons.Default.Cookie,
                title = "B站 Cookies",
                subtitle = if (bilibiliCookies.isBlank()) "未设置" else "已设置 (${
                    bilibiliCookies.take(
                        20
                    )
                }...)",
                onClick = {
                    tempCookies = bilibiliCookies
                    showCookiesDialog = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ========== AI 设置 ==========
            SettingsSectionHeader(title = "AI 设置")

            // AI API 配置状态
            var aiBaseUrl by remember {
                mutableStateOf(PreferenceUtils.getAIBaseUrl(context))
            }
            var aiApiKey by remember {
                mutableStateOf(PreferenceUtils.getAIApiKey(context))
            }
            var aiDefaultModel by remember {
                mutableStateOf(PreferenceUtils.getAIDefaultModel(context))
            }
            var showAISettingsDialog by remember { mutableStateOf(false) }

            // AI 设置对话框
            if (showAISettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showAISettingsDialog = false },
                    title = { Text("AI 设置") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = aiBaseUrl,
                                onValueChange = { aiBaseUrl = it },
                                label = { Text("API Base URL") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("https://api.moonshot.ai/v1") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = aiApiKey,
                                onValueChange = { aiApiKey = it },
                                label = { Text("API Key") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("sk-...") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = aiDefaultModel,
                                onValueChange = { aiDefaultModel = it },
                                label = { Text("默认模型") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("moonshot-v1-8k") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Kimi 可用模型: moonshot-v1-8k, moonshot-v1-32k, moonshot-v1-128k",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            PreferenceUtils.setAIBaseUrl(context, aiBaseUrl)
                            PreferenceUtils.setAIApiKey(context, aiApiKey)
                            PreferenceUtils.setAIDefaultModel(context, aiDefaultModel)
                            showAISettingsDialog = false
                        }) {
                            Text("保存")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAISettingsDialog = false }) {
                            Text("取消")
                        }
                    }
                )
            }

            SettingsClickItem(
                icon = Icons.Default.Psychology,
                title = "AI 配置",
                subtitle = if (aiApiKey.isBlank()) "未配置 API Key" else "已配置 ($aiDefaultModel)",
                onClick = { showAISettingsDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ========== 开发者选项 ==========
            SettingsSectionHeader(title = "开发者选项")

            SettingsClickItem(
                icon = Icons.Default.BugReport,
                title = "调试 URL",
                subtitle = "在 WebView 中打开自定义链接",
                onClick = { showDebugDialog = true }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
