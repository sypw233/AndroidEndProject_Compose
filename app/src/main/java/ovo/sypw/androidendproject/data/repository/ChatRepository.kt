package ovo.sypw.androidendproject.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ovo.sypw.androidendproject.data.model.ChatConversation
import ovo.sypw.androidendproject.data.model.ChatMessage
import java.util.UUID

/**
 * 聊天对话本地存储 Repository
 */
class ChatRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("chat_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_CONVERSATIONS = "conversations"
    }

    /**
     * 保存对话
     */
    fun saveConversation(conversation: ChatConversation) {
        val conversations = loadConversations().toMutableList()
        val existingIndex = conversations.indexOfFirst { it.id == conversation.id }

        val updatedConversation = conversation.copy(updatedAt = System.currentTimeMillis())

        if (existingIndex >= 0) {
            conversations[existingIndex] = updatedConversation
        } else {
            conversations.add(0, updatedConversation)
        }

        saveAllConversations(conversations)
    }

    /**
     * 加载所有对话
     */
    fun loadConversations(): List<ChatConversation> {
        val json = prefs.getString(KEY_CONVERSATIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<ChatConversation>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取单个对话
     */
    fun getConversation(conversationId: String): ChatConversation? {
        return loadConversations().find { it.id == conversationId }
    }

    /**
     * 删除对话
     */
    fun deleteConversation(conversationId: String) {
        val conversations = loadConversations().filter { it.id != conversationId }
        saveAllConversations(conversations)
    }

    /**
     * 创建新对话
     */
    fun createConversation(modelId: String = "moonshot-v1-8k"): ChatConversation {
        val conversation = ChatConversation(
            id = UUID.randomUUID().toString(),
            title = "新对话",
            modelId = modelId
        )
        saveConversation(conversation)
        return conversation
    }

    /**
     * 向对话添加消息
     */
    fun addMessage(conversationId: String, message: ChatMessage) {
        val conversation = getConversation(conversationId) ?: return
        conversation.messages.add(message)

        // 如果是第一条用户消息，更新对话标题
        if (conversation.messages.size == 1 && message.role == "user") {
            val title = message.content.take(20) + if (message.content.length > 20) "..." else ""
            saveConversation(conversation.copy(title = title))
        } else {
            saveConversation(conversation)
        }
    }

    /**
     * 更新对话中的最后一条消息 (用于流式更新)
     */
    fun updateLastMessage(
        conversationId: String,
        content: String,
        thinkingContent: String? = null
    ) {
        val conversation = getConversation(conversationId) ?: return
        if (conversation.messages.isEmpty()) return

        val lastIndex = conversation.messages.lastIndex
        val lastMessage = conversation.messages[lastIndex]
        conversation.messages[lastIndex] = lastMessage.copy(
            content = content,
            thinkingContent = thinkingContent
        )
        saveConversation(conversation)
    }

    private fun saveAllConversations(conversations: List<ChatConversation>) {
        val json = gson.toJson(conversations)
        prefs.edit().putString(KEY_CONVERSATIONS, json).apply()
    }
}
