package ovo.sypw.androidendproject.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ovo.sypw.androidendproject.data.model.BarChartData
import ovo.sypw.androidendproject.data.model.BilibiliRankingItem
import ovo.sypw.androidendproject.data.model.LineChartData
import ovo.sypw.androidendproject.data.model.PieChartData
import ovo.sypw.androidendproject.data.model.PieChartItem
import ovo.sypw.androidendproject.data.remote.ApiService
import ovo.sypw.androidendproject.utils.PreferenceUtils

class ChartRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    /**
     * 获取存储的 B站 Cookies
     */
    private fun getCookies(): String {
        return PreferenceUtils.getBilibiliCookies(context)
    }

    /**
     * 获取 B站排行榜原始数据
     */
    fun getBilibiliRankingData(): Flow<Result<List<BilibiliRankingItem>>> = flow {
        try {
            val cookies = getCookies()
            val response = apiService.getBilibiliRanking(rid = 0, type = "all", cookie = cookies)
            if (response.code == 0 && response.data?.list != null) {
                emit(Result.success(response.data.list))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取折线图数据 - Top 10 视频播放量趋势
     */
    fun getLineChartData(): Flow<Result<LineChartData>> = flow {
        try {
            val cookies = getCookies()
            val response = apiService.getBilibiliRanking(rid = 0, type = "all", cookie = cookies)
            if (response.code == 0 && response.data?.list != null) {
                val top10 = response.data.list.take(10)
                val data = LineChartData(
                    title = "B站排行榜 Top10 播放量",
                    labels = top10.mapIndexed { index, _ -> "第${index + 1}名" },
                    values = top10.map { (it.stat.view / 10000f) } // 以万为单位
                )
                emit(Result.success(data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取柱状图数据 - Top 10 视频互动数据对比（点赞 vs 投币）
     */
    fun getBarChartData(): Flow<Result<BarChartData>> = flow {
        try {
            val cookies = getCookies()
            val response = apiService.getBilibiliRanking(rid = 0, type = "all", cookie = cookies)
            if (response.code == 0 && response.data?.list != null) {
                val top10 = response.data.list.take(10)
                val data = BarChartData(
                    title = "B站排行榜 Top10 互动对比",
                    labels = top10.mapIndexed { index, _ -> "第${index + 1}名" },
                    values = top10.map { (it.stat.like / 1000f) },  // 点赞数（千）
                    values2 = top10.map { (it.stat.coin / 1000f) } // 投币数（千）
                )
                emit(Result.success(data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取饼图数据 - 分区分布
     */
    fun getPieChartData(): Flow<Result<PieChartData>> = flow {
        try {
            val cookies = getCookies()
            val response = apiService.getBilibiliRanking(rid = 0, type = "all", cookie = cookies)
            if (response.code == 0 && response.data?.list != null) {
                // 按分区统计数量
                val categoryCount = response.data.list
                    .groupBy { it.tname ?: "其他" }
                    .mapValues { it.value.size }
                    .entries
                    .sortedByDescending { it.value }
                    .take(8) // 取前8个分区
                
                val total = categoryCount.sumOf { it.value }
                val colors = listOf(
                    0xFFE91E63.toInt(),  // 粉色
                    0xFF2196F3.toInt(),  // 蓝色
                    0xFF4CAF50.toInt(),  // 绿色
                    0xFFFF9800.toInt(),  // 橙色
                    0xFF9C27B0.toInt(),  // 紫色
                    0xFF00BCD4.toInt(),  // 青色
                    0xFFFF5722.toInt(),  // 深橙
                    0xFF795548.toInt()   // 棕色
                )
                
                val items = categoryCount.mapIndexed { index, entry ->
                    PieChartItem(
                        label = entry.key,
                        value = (entry.value * 100f / total),
                        color = colors.getOrElse(index) { 0xFF888888.toInt() }
                    )
                }
                
                val data = PieChartData(
                    title = "B站排行榜分区分布",
                    items = items
                )
                emit(Result.success(data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
