package ovo.sypw.androidendproject.data.model

import com.google.gson.annotations.SerializedName

/**
 * B站排行榜响应
 */
data class BilibiliRankingResponse(
    val code: Int,
    val message: String,
    val data: BilibiliRankingData?
)

data class BilibiliRankingData(
    val note: String?,
    val list: List<BilibiliRankingItem>?
)

data class BilibiliRankingItem(
    val aid: Long,
    val bvid: String,
    val title: String,
    val pic: String,
    val tname: String?,  // 分区名称
    val tid: Int,   // 分区 ID
    val duration: Int,  // 时长（秒）
    val pubdate: Long,  // 发布时间戳
    val desc: String?,
    val owner: BilibiliOwner,
    val stat: BilibiliStat,
    @SerializedName("short_link")
    val shortLink: String?
)


