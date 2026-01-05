package ovo.sypw.androidendproject.ui.screens.me

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import ovo.sypw.androidendproject.data.model.User
import ovo.sypw.androidendproject.ui.components.LoadingIndicator
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

@Composable
fun MeScreen(
    viewModel: MeViewModel = koinViewModel(),
    onLoginClick: () -> Unit,
    onMapClick: () -> Unit,
    onDebugUrlClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDebugDialog by remember { mutableStateOf(false) }
    var debugUrl by remember { mutableStateOf("https://www.bilibili.com/video/BV1tz421i7zb") }

    // Debug URL Dialog
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
                    onDebugUrlClick(debugUrl)
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

    when (val state = uiState) {
        is MeUiState.Loading -> {
            LoadingIndicator()
        }
        is MeUiState.LoggedIn -> {
            LoggedInContent(
                user = state.user,
                onMapClick = onMapClick,
                onDebugClick = { showDebugDialog = true },
                onLogout = { viewModel.logout() }
            )
        }
        is MeUiState.NotLoggedIn -> {
            NotLoggedInContent(
                onLoginClick = onLoginClick,
                onMapClick = onMapClick,
                onDebugClick = { showDebugDialog = true }
            )
        }
    }
}

@Composable
private fun LoggedInContent(
    user: User,
    onMapClick: () -> Unit,
    onDebugClick: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 用户信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                if (user.avatarUrl != null) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    // 优先显示 displayName，其次是 email 的用户名部分
                    val displayNameText = user.displayName?.takeIf { it.isNotBlank() }
                        ?: user.email.substringBefore("@")
                        ?: "用户"
                    Text(
                        text = displayNameText,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 功能列表
        MeMenuItem(
            icon = Icons.Default.Map,
            title = "附近地图",
            onClick = onMapClick
        )
        MeMenuItem(
            icon = Icons.Default.Settings,
            title = "设置",
            onClick = { }
        )
        MeMenuItem(
            icon = Icons.Default.BugReport,
            title = "调试 URL",
            onClick = onDebugClick
        )

        Spacer(modifier = Modifier.weight(1f))

        // 退出登录
        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("退出登录")
        }
    }
}

@Composable
private fun NotLoggedInContent(
    onLoginClick: () -> Unit,
    onMapClick: () -> Unit,
    onDebugClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // 默认头像
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "登录后查看更多内容",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onLoginClick) {
            Text("登录 / 注册")
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 功能列表
        MeMenuItem(
            icon = Icons.Default.Map,
            title = "附近地图",
            onClick = onMapClick
        )
        MeMenuItem(
            icon = Icons.Default.BugReport,
            title = "调试 URL",
            onClick = onDebugClick
        )
    }
}

@Composable
private fun MeMenuItem(
    icon: ImageVector,
    title: String,
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
