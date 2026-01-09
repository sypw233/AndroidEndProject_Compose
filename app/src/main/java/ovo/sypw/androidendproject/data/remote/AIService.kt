package ovo.sypw.androidendproject.data.remote

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import ovo.sypw.androidendproject.data.model.ChatMessage
import ovo.sypw.androidendproject.utils.PreferenceUtils
import java.io.BufferedReader
import java.util.concurrent.TimeUnit

/**
 * AI 服务封装 - 使用 Kimi (Moonshot) API
 * 采用 OpenAI 兼容格式
 */
class AIService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        const val DEFAULT_BASE_URL = "https://api.moonshot.cn/v1"
        const val DEFAULT_MODEL = "moonshot-v1-8k"
    }

    /**
     * 发送消息并获取流式响应
     */
    fun sendMessageStream(
        messages: List<ChatMessage>,
        model: String = getModel(),
        enableThinking: Boolean = false
    ): Flow<StreamResponse> = flow {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()

        if (apiKey.isBlank()) {
            emit(StreamResponse.Error("请先在设置中配置 API Key"))
            return@flow
        }

        val messagesJson = JSONArray().apply {
            val lastIndex = messages.size - 1
            messages.forEachIndexed { index, msg ->
                put(JSONObject().apply {
                    put("role", msg.role)
                    if (msg.imageBase64 != null) {
                        // 只有最后一条消息发送完整图片，历史消息用占位符替代
                        if (index == lastIndex) {
                            // 最新消息 - 发送完整图片
                            put("content", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("type", "text")
                                    put("text", msg.content)
                                })
                                put(JSONObject().apply {
                                    put("type", "image_url")
                                    put("image_url", JSONObject().apply {
                                        put("url", "data:image/jpeg;base64,${msg.imageBase64}")
                                    })
                                })
                            })
                        } else {
                            // 历史消息 - 用占位符替代图片
                            put("content", "${msg.content}\n[user_image]")
                        }
                    } else {
                        put("content", msg.content)
                    }
                })
            }
        }

        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", messagesJson)
            put("stream", true)
            if (enableThinking) {
                put("temperature", 0.7)
            }
        }

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                emit(StreamResponse.Error("API 错误 (${response.code}): $errorBody"))
                return@flow
            }

            val reader = BufferedReader(response.body?.charStream())
            var line: String?
            val fullContent = StringBuilder()
            var thinkingContent: String? = null

            while (reader.readLine().also { line = it } != null) {
                if (line.isNullOrBlank()) continue
                if (line?.startsWith("data: ") != true) continue

                val data = line!!.removePrefix("data: ").trim()
                if (data == "[DONE]") {
                    emit(StreamResponse.Done(fullContent.toString(), thinkingContent))
                    break
                }

                try {
                    val json = JSONObject(data)
                    val choices = json.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val delta = choices.getJSONObject(0).optJSONObject("delta")
                        val content = delta?.optString("content", "") ?: ""

                        if (content.isNotEmpty()) {
                            fullContent.append(content)
                            emit(StreamResponse.Delta(content, fullContent.toString()))
                        }
                    }
                } catch (e: Exception) {
                    // 忽略解析错误，继续处理
                }
            }

            reader.close()
            response.close()
        } catch (e: Exception) {
            emit(StreamResponse.Error("网络错误: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 发送消息 (非流式)
     */
    suspend fun sendMessage(
        messages: List<ChatMessage>,
        model: String = getModel()
    ): Result<String> = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()

        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("请先在设置中配置 API Key"))
        }

        val messagesJson = JSONArray().apply {
            messages.forEach { msg ->
                put(JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                })
            }
        }

        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", messagesJson)
            put("stream", false)
        }

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API 错误 (${response.code}): $body"))
            }

            val json = JSONObject(body)
            val choices = json.optJSONArray("choices")
            val content = choices?.getJSONObject(0)
                ?.getJSONObject("message")
                ?.getString("content") ?: ""

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getBaseUrl(): String {
        return PreferenceUtils.getString(context, "ai_base_url", DEFAULT_BASE_URL)
    }

    private fun getApiKey(): String {
        return PreferenceUtils.getString(context, "ai_api_key", "")
    }

    private fun getModel(): String {
        return PreferenceUtils.getString(context, "ai_default_model", DEFAULT_MODEL)
    }

    /**
     * 将图片字节数组转换为 Base64
     */
    fun encodeImageToBase64(imageBytes: ByteArray): String {
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }
}

/**
 * 流式响应封装
 */
sealed class StreamResponse {
    data class Delta(val chunk: String, val fullContent: String) : StreamResponse()
    data class Done(val content: String, val thinkingContent: String? = null) : StreamResponse()
    data class Error(val message: String) : StreamResponse()
}
