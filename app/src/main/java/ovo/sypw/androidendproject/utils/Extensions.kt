package ovo.sypw.androidendproject.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Toast 扩展
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * 时间格式化扩展
 */
fun Long.toDateString(pattern: String = "yyyy-MM-dd HH:mm"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
}

fun String.toTimestamp(pattern: String = "yyyy-MM-dd HH:mm"): Long {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

/**
 * 字符串扩展
 */
fun String?.orDefault(default: String = ""): String {
    return if (this.isNullOrBlank()) default else this
}

/**
 * 数字格式化
 */
fun Int.formatCount(): String {
    return when {
        this >= 10000 -> String.format("%.1f万", this / 10000.0)
        this >= 1000 -> String.format("%.1fk", this / 1000.0)
        else -> this.toString()
    }
}

/**
 * 时长格式化 (秒转 mm:ss)
 */
fun Int.formatDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
