package ovo.sypw.androidendproject.utils

/**
 * 应用常量定义
 */
object Constants {
    // ========== 网络相关 ==========
    /** 连接超时时间（秒） */
    const val NETWORK_CONNECT_TIMEOUT = 30L

    /** 读取超时时间（秒） */
    const val NETWORK_READ_TIMEOUT = 30L

    /** 写入超时时间（秒） */
    const val NETWORK_WRITE_TIMEOUT = 30L

    // ========== 分页相关 ==========
    /** 默认分页大小 */
    const val DEFAULT_PAGE_SIZE = 10

    /** B站视频分页大小 */
    const val BILIBILI_PAGE_SIZE = 20

    // ========== 缓存相关 ==========
    /** 缓存过期时间（毫秒）- 30分钟 */
    const val CACHE_EXPIRE_TIME = 30 * 60 * 1000L

    // ========== 日期格式 ==========
    /** 标准日期时间格式 */
    const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"

    /** RSS 日期格式 */
    const val RSS_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z"

    // ========== UserAgent ==========
    /** WebView UserAgent */
    const val USER_AGENT_WEBVIEW =
        "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    // ========== 图表相关 ==========
    /** 图表默认高度 dp */
    const val CHART_DEFAULT_HEIGHT = 220

    /** 饼图默认高度 dp */
    const val PIE_CHART_HEIGHT = 200

    /** 图表左右内边距 */
    const val CHART_PADDING = 50f

    /** 图表底部内边距 */
    const val CHART_BOTTOM_PADDING = 35f

    /** 图表顶部内边距 */
    const val CHART_TOP_PADDING = 20f

    // ========== 动画相关 ==========
    /** 折线图动画时长（毫秒） */
    const val LINE_CHART_ANIMATION_DURATION = 800

    /** 柱状图动画时长（毫秒） */
    const val BAR_CHART_ANIMATION_DURATION = 600

    // ========== UI相关 ==========
    /** 双击退出间隔（毫秒） */
    const val BACK_PRESS_INTERVAL = 2000L
}
