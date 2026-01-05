package ovo.sypw.androidendproject.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtils {
    private const val PREF_NAME = "app_preferences"
    private const val KEY_FIRST_LAUNCH = "first_launch"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstLaunch(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(context: Context, isFirst: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_FIRST_LAUNCH, isFirst).apply()
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
}
