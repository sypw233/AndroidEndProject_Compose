package ovo.sypw.androidendproject.ui.screens.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ovo.sypw.androidendproject.ui.screens.chat.components.ChatInput
import ovo.sypw.androidendproject.ui.screens.chat.components.ChatMessageBubble

/**
 * 聊天对话页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val conversation by viewModel.currentConversation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val streamingContent by viewModel.streamingContent.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentModel by viewModel.currentModel.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val thinkingEnabled by viewModel.thinkingEnabled.collectAsState()
    val pendingImageBase64 by viewModel.pendingImageBase64.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var showModelMenu by remember { mutableStateOf(false) }

    // 加载对话
    LaunchedEffect(conversationId) {
        viewModel.loadConversation(conversationId)
    }

    // 显示错误
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 自动滚动到底部
    LaunchedEffect(conversation?.messages?.size, streamingContent) {
        if ((conversation?.messages?.size ?: 0) > 0) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = conversation?.title ?: "新对话",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = currentModel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 模型选择菜单
                    Box {
                        IconButton(onClick = { showModelMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = showModelMenu,
                            onDismissRequest = { showModelMenu = false }
                        ) {
                            availableModels.forEach { model ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = model,
                                            color = if (model == currentModel) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.switchModel(model)
                                        showModelMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                val messages = conversation?.messages?.asReversed() ?: emptyList()
                
                items(messages, key = { it.id }) { message ->
                    val isLastAssistantMessage = messages.firstOrNull()?.id == message.id 
                            && message.role == "assistant"
                    val isStreaming = isLastAssistantMessage && isLoading
                    
                    ChatMessageBubble(
                        message = if (isStreaming && streamingContent.isNotEmpty()) {
                            message.copy(content = streamingContent)
                        } else {
                            message
                        },
                        isStreaming = isStreaming
                    )
                }
            }

            // 输入区域
            ChatInput(
                onSendMessage = { content ->
                    viewModel.sendMessage(content)
                },
                onImageSelected = { uri ->
                    viewModel.setImage(context, uri)
                },
                onThinkingToggle = {
                    viewModel.toggleThinking()
                },
                isLoading = isLoading,
                thinkingEnabled = thinkingEnabled,
                pendingImageBase64 = pendingImageBase64,
                onClearImage = {
                    viewModel.clearImage()
                }
            )
        }
    }
}
