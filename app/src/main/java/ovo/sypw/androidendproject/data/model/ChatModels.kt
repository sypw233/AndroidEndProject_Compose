package ovo.sypw.androidendproject.data.model

import com.google.gson.annotations.SerializedName

/**
 * 聊天消息
 */
data class ChatMessage(
    val id: String,
    val role: String,  // "user" | "assistant" | "system"
    val content: String,
    @SerializedName("image_base64")
    val imageBase64: String? = null,
    @SerializedName("thinking_content")
    val thinkingContent: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 聊天对话
 */
data class ChatConversation(
    val id: String,
    val title: String,
    val messages: MutableList<ChatMessage> = mutableListOf(),
    @SerializedName("model_id")
    val modelId: String = "kimi-latest",
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at")
    var updatedAt: Long = System.currentTimeMillis()
)

/**
 * AI 模型配置
 */
data class AIModelConfig(
    val id: String,
    val name: String,
    @SerializedName("supports_vision")
    val supportsVision: Boolean = false,
    @SerializedName("supports_thinking")
    val supportsThinking: Boolean = false
) {
    companion object {
        // Kimi 模型列表
        val DEFAULT_MODELS = listOf(
            AIModelConfig("kimi-latest", "kimi-latest", true, false),
            AIModelConfig("moonshot-v1-8k", "Moonshot V1 8K", false, false),
            AIModelConfig("moonshot-v1-32k", "Moonshot V1 32K", false, false),
            AIModelConfig("moonshot-v1-128k", "Moonshot V1 128K", false, false)
        )
    }
}
