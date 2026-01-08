package ovo.sypw.androidendproject.ui.screens.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.androidendproject.data.model.AIModelConfig
import ovo.sypw.androidendproject.data.model.ChatConversation
import ovo.sypw.androidendproject.data.model.ChatMessage
import ovo.sypw.androidendproject.data.remote.AIService
import ovo.sypw.androidendproject.data.remote.StreamResponse
import ovo.sypw.androidendproject.data.repository.ChatRepository
import java.util.UUID

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val aiService: AIService
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
    private val _currentModel = MutableStateFlow("moonshot-v1-8k")
    val currentModel: StateFlow<String> = _currentModel.asStateFlow()

    // 思考模式
    private val _thinkingEnabled = MutableStateFlow(false)
    val thinkingEnabled: StateFlow<Boolean> = _thinkingEnabled.asStateFlow()

    // 待发送的图片
    private val _pendingImageBase64 = MutableStateFlow<String?>(null)
    val pendingImageBase64: StateFlow<String?> = _pendingImageBase64.asStateFlow()

    init {
        loadConversations()
    }

    /**
     * 加载所有对话
     */
    fun loadConversations() {
        _conversations.value = chatRepository.loadConversations()
    }

    /**
     * 加载指定对话
     */
    fun loadConversation(conversationId: String) {
        _currentConversation.value = chatRepository.getConversation(conversationId)
    }

    /**
     * 创建新对话
     */
    fun createNewConversation(): String {
        val conversation = chatRepository.createConversation(_currentModel.value)
        _currentConversation.value = conversation
        loadConversations()
        return conversation.id
    }

    /**
     * 删除对话
     */
    fun deleteConversation(conversationId: String) {
        chatRepository.deleteConversation(conversationId)
        loadConversations()
        if (_currentConversation.value?.id == conversationId) {
            _currentConversation.value = null
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

            // 添加用户消息
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "user",
                content = content,
                imageBase64 = _pendingImageBase64.value
            )
            chatRepository.addMessage(conversation.id, userMessage)
            _pendingImageBase64.value = null

            // 刷新当前对话
            loadConversation(conversation.id)
            loadConversations()

            // 获取完整消息历史
            val updatedConversation = _currentConversation.value ?: return@launch
            val messages = updatedConversation.messages.toList()

            // 预先添加一个空的 AI 回复消息
            val aiMessageId = UUID.randomUUID().toString()
            val aiMessage = ChatMessage(
                id = aiMessageId,
                role = "assistant",
                content = ""
            )
            chatRepository.addMessage(conversation.id, aiMessage)
            loadConversation(conversation.id)

            // 发起流式请求
            aiService.sendMessageStream(
                messages = messages,
                model = _currentModel.value,
                enableThinking = _thinkingEnabled.value
            ).collect { response ->
                when (response) {
                    is StreamResponse.Delta -> {
                        _streamingContent.value = response.fullContent
                        // 更新最后一条消息
                        chatRepository.updateLastMessage(conversation.id, response.fullContent)
                        loadConversation(conversation.id)
                    }
                    is StreamResponse.Done -> {
                        _streamingContent.value = ""
                        chatRepository.updateLastMessage(
                            conversation.id,
                            response.content,
                            response.thinkingContent
                        )
                        loadConversation(conversation.id)
                        loadConversations()
                    }
                    is StreamResponse.Error -> {
                        _errorMessage.value = response.message
                        // 移除空的 AI 回复
                        loadConversation(conversation.id)
                    }
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * 切换模型
     */
    fun switchModel(modelId: String) {
        _currentModel.value = modelId
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
    fun setImage(context: Context, uri: Uri?) {
        if (uri == null) {
            _pendingImageBase64.value = null
            return
        }
        
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    _pendingImageBase64.value = aiService.encodeImageToBase64(bytes)
                }
            } catch (e: Exception) {
                _errorMessage.value = "图片加载失败: ${e.message}"
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
}
