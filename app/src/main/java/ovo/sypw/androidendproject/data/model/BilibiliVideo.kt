package ovo.sypw.androidendproject.data.model

import com.google.gson.annotations.SerializedName

/**
 * B站热门视频响应
 */
data class BilibiliPopularResponse(
    val code: Int,
    val message: String,
    val data: BilibiliPopularData?
)

data class BilibiliPopularData(
    val list: List<BilibiliVideo>,
    @SerializedName("no_more")
    val noMore: Boolean
)

/**
 * B站视频数据模型
 */
data class BilibiliVideo(
    val aid: Long,
    val bvid: String,
    val title: String,
    val pic: String,  // 封面图
    val desc: String,
    val duration: Int,  // 秒
    val pubdate: Long,  // 发布时间戳
    val owner: BilibiliOwner,
    val stat: BilibiliStat,
    @SerializedName("rcmd_reason")
    val rcmdReason: BilibiliRcmdReason?
)

data class BilibiliOwner(
    val mid: Long,
    val name: String,
    val face: String  // 头像
)

data class BilibiliStat(
    val view: Long,      // 播放量
    val danmaku: Long,   // 弹幕数
    val reply: Long,     // 评论数
    val favorite: Long,  // 收藏数
    val coin: Long,      // 投币数
    val share: Long,     // 分享数
    val like: Long,       // 点赞数
    @SerializedName("his_rank")
    val hisRank: Int     // 历史最高排名
)

data class BilibiliRcmdReason(
    val content: String?
)

/**
 * 扩展函数：格式化播放量
 */
fun Long.formatViewCount(): String {
    return when {
        this >= 100000000 -> String.format("%.1f亿", this / 100000000.0)
        this >= 10000 -> String.format("%.1f万", this / 10000.0)
        else -> this.toString()
    }
}

/**
 * 扩展函数：格式化时长
 */
fun Int.formatDuration(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
