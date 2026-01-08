package ovo.sypw.androidendproject.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PreferenceUtils {
    private const val PREF_NAME = "app_preferences"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    private const val KEY_USE_GOOGLE_AD = "use_google_ad"
    private const val KEY_BILIBILI_COOKIES = "bilibili_cookies"


    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstLaunch(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(context: Context, isFirst: Boolean) {
        getPreferences(context).edit { putBoolean(KEY_FIRST_LAUNCH, isFirst) }
    }

    /**
     * 是否使用 Google AdMob 开屏广告
     * true = Google AdMob 开屏广告
     * false = 自定义启动屏（图片/视频）
     */
    fun useGoogleAd(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_USE_GOOGLE_AD, false)
    }

    fun setUseGoogleAd(context: Context, useGoogle: Boolean) {
        getPreferences(context).edit { putBoolean(KEY_USE_GOOGLE_AD, useGoogle) }
    }

    fun getString(context: Context, key: String, defaultValue: String = ""): String {
        return getPreferences(context).getString(key, defaultValue) ?: defaultValue
    }

    fun putString(context: Context, key: String, value: String) {
        getPreferences(context).edit().putString(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return getPreferences(context).getBoolean(key, defaultValue)
    }

    fun putBoolean(context: Context, key: String, value: Boolean) {
        getPreferences(context).edit().putBoolean(key, value).apply()
    }

    fun clear(context: Context) {
        getPreferences(context).edit().clear().apply()
    }

    /**
     * 获取 B站 Cookies
     */
    fun getBilibiliCookies(context: Context): String {
        return getPreferences(context).getString(KEY_BILIBILI_COOKIES, "") ?: ""
    }

    /**
     * 设置 B站 Cookies
     */
    fun setBilibiliCookies(context: Context, cookies: String) {
        getPreferences(context).edit { putString(KEY_BILIBILI_COOKIES, cookies) }
    }

    // ========== AI 配置 ==========
    private const val KEY_AI_BASE_URL = "ai_base_url"
    private const val KEY_AI_API_KEY = "ai_api_key"
    private const val KEY_AI_DEFAULT_MODEL = "ai_default_model"
    private const val KEY_AI_MODELS = "ai_models"

    fun getAIBaseUrl(context: Context): String {
        return getString(context, KEY_AI_BASE_URL, "https://api.moonshot.ai/v1")
    }

    fun setAIBaseUrl(context: Context, url: String) {
        putString(context, KEY_AI_BASE_URL, url)
    }

    fun getAIApiKey(context: Context): String {
        return getString(context, KEY_AI_API_KEY, "")
    }

    fun setAIApiKey(context: Context, key: String) {
        putString(context, KEY_AI_API_KEY, key)
    }

    fun getAIDefaultModel(context: Context): String {
        return getString(context, KEY_AI_DEFAULT_MODEL, "moonshot-v1-8k")
    }

    fun setAIDefaultModel(context: Context, model: String) {
        putString(context, KEY_AI_DEFAULT_MODEL, model)
    }

    fun getAIModelsJson(context: Context): String {
        return getString(context, KEY_AI_MODELS, "")
    }

    fun setAIModelsJson(context: Context, json: String) {
        putString(context, KEY_AI_MODELS, json)
    }
}

