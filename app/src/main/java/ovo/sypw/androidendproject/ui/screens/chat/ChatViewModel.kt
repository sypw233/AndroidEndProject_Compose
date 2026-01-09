package ovo.sypw.androidendproject.ui.screens.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ovo.sypw.androidendproject.data.model.ChatConversation
import ovo.sypw.androidendproject.data.model.ChatMessage
import ovo.sypw.androidendproject.data.remote.AIService
import ovo.sypw.androidendproject.data.remote.StreamResponse
import ovo.sypw.androidendproject.data.repository.ChatRepository
import ovo.sypw.androidendproject.utils.PreferenceUtils
import java.util.UUID

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val aiService: AIService,
    private val context: Context
) : ViewModel() {

    // 当前对话
    private val _currentConversation = MutableStateFlow<ChatConversation?>(null)
    val currentConversation: StateFlow<ChatConversation?> = _currentConversation.asStateFlow()

    // 所有对话列表
    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 当前流式响应内容
    private val _streamingContent = MutableStateFlow("")
    val streamingContent: StateFlow<String> = _streamingContent.asStateFlow()

    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 当前选择的模型
    private val _currentModel = MutableStateFlow(PreferenceUtils.getAILastModel(context))
    val currentModel: StateFlow<String> = _currentModel.asStateFlow()

    // 可用模型列表
    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    // 思考模式
    private val _thinkingEnabled = MutableStateFlow(false)
    val thinkingEnabled: StateFlow<Boolean> = _thinkingEnabled.asStateFlow()

    // 待发送的图片
    private val _pendingImageBase64 = MutableStateFlow<String?>(null)
    val pendingImageBase64: StateFlow<String?> = _pendingImageBase64.asStateFlow()

    // 节流保存任务
    private var saveJob: Job? = null
    private var lastSaveTime = 0L
    private val SAVE_THROTTLE_MS = 500L

    init {
        loadConversations()
        loadAvailableModels()
    }

    /**
     * 加载可用模型列表
     */
    private fun loadAvailableModels() {
        viewModelScope.launch(Dispatchers.IO) {
            val modelsJson = PreferenceUtils.getAIModelsJson(context)
            val models = if (modelsJson.isBlank()) {
                listOf("kimi-latest", "moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k")
            } else {
                modelsJson.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }
            withContext(Dispatchers.Main) {
                _availableModels.value = models
            }
        }
    }

    /**
     * 加载所有对话
     */
    fun loadConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            val convs = chatRepository.loadConversations()
            withContext(Dispatchers.Main) {
                _conversations.value = convs
            }
        }
    }

    /**
     * 加载指定对话
     */
    fun loadConversation(conversationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val conversation = chatRepository.getConversation(conversationId)
            withContext(Dispatchers.Main) {
                _currentConversation.value = conversation
                conversation?.modelId?.let { modelId ->
                    _currentModel.value = modelId
                }
            }
        }
    }

    /**
     * 创建新对话 - 同步执行以确保返回正确ID
     */
    fun createNewConversation(): String {
        // 同步创建对话，确保导航使用正确的ID
        val conversation = chatRepository.createConversation(_currentModel.value)
        _currentConversation.value = conversation
        // 异步刷新列表
        viewModelScope.launch(Dispatchers.IO) {
            val convs = chatRepository.loadConversations()
            withContext(Dispatchers.Main) {
                _conversations.value = convs
            }
        }
        return conversation.id
    }

    /**
     * 删除对话
     */
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.deleteConversation(conversationId)
            val convs = chatRepository.loadConversations()
            withContext(Dispatchers.Main) {
                _conversations.value = convs
                if (_currentConversation.value?.id == conversationId) {
                    _currentConversation.value = null
                }
            }
        }
    }

    /**
     * 发送消息
     */
    fun sendMessage(content: String) {
        val conversation = _currentConversation.value ?: return
        if (content.isBlank() && _pendingImageBase64.value == null) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _streamingContent.value = ""

            // 创建用户消息
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "user",
                content = content,
                imageBase64 = _pendingImageBase64.value
            )
            _pendingImageBase64.value = null

            // 在 IO 线程添加用户消息并获取更新后的对话
            val updatedConversation = withContext(Dispatchers.IO) {
                chatRepository.addMessage(conversation.id, userMessage)
                chatRepository.getConversation(conversation.id)
            }

            if (updatedConversation == null) {
                _isLoading.value = false
                _errorMessage.value = "对话加载失败"
                return@launch
            }

            _currentConversation.value = updatedConversation

            // 获取消息历史（过滤掉空的助手消息）
            val messages = updatedConversation.messages
                .filter { it.content.isNotBlank() || it.role == "user" }
                .toList()

            // 预先添加空的 AI 回复消息
            val aiMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "assistant",
                content = ""
            )

            val conversationWithAiMessage = withContext(Dispatchers.IO) {
                chatRepository.addMessage(conversation.id, aiMessage)
                chatRepository.getConversation(conversation.id)
            }

            _currentConversation.value = conversationWithAiMessage

            var hasReceivedContent = false

            // 发起流式请求
            aiService.sendMessageStream(
                messages = messages,
                model = _currentModel.value,
                enableThinking = _thinkingEnabled.value
            ).collect { response ->
                when (response) {
                    is StreamResponse.Delta -> {
                        hasReceivedContent = true
                        _streamingContent.value = response.fullContent
                        // 节流保存
                        throttleSave(conversation.id, response.fullContent)
                    }

                    is StreamResponse.Done -> {
                        _streamingContent.value = ""
                        val finalContent = response.content.ifBlank {
                            "[请求完成，但未返回内容]"
                        }
                        // 最终保存
                        withContext(Dispatchers.IO) {
                            chatRepository.updateLastMessage(
                                conversation.id,
                                finalContent,
                                response.thinkingContent
                            )
                        }
                        // 刷新对话
                        val finalConversation = withContext(Dispatchers.IO) {
                            chatRepository.getConversation(conversation.id)
                        }
                        _currentConversation.value = finalConversation
                        loadConversations()
                    }

                    is StreamResponse.Error -> {
                        _errorMessage.value = response.message
                        if (!hasReceivedContent) {
                            withContext(Dispatchers.IO) {
                                chatRepository.updateLastMessage(
                                    conversation.id,
                                    "[请求失败: ${response.message}]"
                                )
                            }
                        }
                        val errorConversation = withContext(Dispatchers.IO) {
                            chatRepository.getConversation(conversation.id)
                        }
                        _currentConversation.value = errorConversation
                    }
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * 节流保存
     */
    private fun throttleSave(conversationId: String, content: String) {
        val now = System.currentTimeMillis()
        if (now - lastSaveTime < SAVE_THROTTLE_MS) {
            saveJob?.cancel()
            saveJob = viewModelScope.launch(Dispatchers.IO) {
                delay(SAVE_THROTTLE_MS)
                chatRepository.updateLastMessage(conversationId, content)
                lastSaveTime = System.currentTimeMillis()
            }
        } else {
            lastSaveTime = now
            viewModelScope.launch(Dispatchers.IO) {
                chatRepository.updateLastMessage(conversationId, content)
            }
        }
    }

    /**
     * 切换模型
     */
    fun switchModel(modelId: String) {
        _currentModel.value = modelId
        viewModelScope.launch(Dispatchers.IO) {
            PreferenceUtils.setAILastModel(context, modelId)
            _currentConversation.value?.let { conversation ->
                val updated = conversation.copy(modelId = modelId)
                chatRepository.saveConversation(updated)
                withContext(Dispatchers.Main) {
                    _currentConversation.value = updated
                }
            }
        }
    }

    /**
     * 切换思考模式
     */
    fun toggleThinking() {
        _thinkingEnabled.value = !_thinkingEnabled.value
    }

    /**
     * 设置待发送的图片
     */
    fun setImage(imageContext: Context, uri: Uri?) {
        if (uri == null) {
            _pendingImageBase64.value = null
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = imageContext.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val base64 = aiService.encodeImageToBase64(bytes)
                    withContext(Dispatchers.Main) {
                        _pendingImageBase64.value = base64
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "图片加载失败: ${e.message}"
                }
            }
        }
    }

    /**
     * 清除待发送的图片
     */
    fun clearImage() {
        _pendingImageBase64.value = null
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 刷新可用模型列表
     */
    fun refreshModels() {
        loadAvailableModels()
    }
}
