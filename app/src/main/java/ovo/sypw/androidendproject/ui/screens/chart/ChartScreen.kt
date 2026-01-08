package ovo.sypw.androidendproject.ui.screens.chart

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import ovo.sypw.androidendproject.data.model.BilibiliRankingItem
import ovo.sypw.androidendproject.data.model.PieChartItem
import ovo.sypw.androidendproject.ui.components.AppBarChart
import ovo.sypw.androidendproject.ui.components.AppLineChart
import ovo.sypw.androidendproject.ui.components.AppPieChart
import ovo.sypw.androidendproject.ui.components.ErrorView
import ovo.sypw.androidendproject.ui.components.LoadingIndicator
import ovo.sypw.androidendproject.ui.components.SpeedDialFab

@Composable
fun ChartScreen(
    viewModel: ChartViewModel = koinViewModel(),
    onVideoClick: ((BilibiliRankingItem) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lineChartData by viewModel.lineChartData.collectAsStateWithLifecycle()
    val barChartData by viewModel.barChartData.collectAsStateWithLifecycle()
    val pieChartData by viewModel.pieChartData.collectAsStateWithLifecycle()
    val rankingList by viewModel.rankingList.collectAsStateWithLifecycle()

    var selectedChartType by remember { mutableStateOf(ChartType.LINE) }
    var selectedPieIndex by remember { mutableIntStateOf(-1) }

    Scaffold(
        floatingActionButton = {
            SpeedDialFab(
                onChartTypeSelected = { chartType ->
                    selectedChartType = chartType
                    selectedPieIndex = -1
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ChartUiState.Loading -> {
                LoadingIndicator()
            }

            is ChartUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    // 标题
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (selectedChartType) {
                                ChartType.LINE -> lineChartData?.title ?: "B站排行榜数据"
                                ChartType.BAR -> barChartData?.title ?: "互动数据对比"
                                ChartType.PIE -> pieChartData?.title ?: "分区分布"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 图表区域
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (selectedChartType) {
                                    ChartType.LINE -> {
                                        lineChartData?.let { data ->
                                            Column {
                                                AppLineChart(data = data)
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    text = "播放量（万）",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    ChartType.BAR -> {
                                        barChartData?.let { data ->
                                            Column {
                                                AppBarChart(data = data)
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    LegendItem(color = Color(0xFF2196F3), label = "点赞（千）")
                                                    LegendItem(color = Color(0xFFFF9800), label = "投币（千）")
                                                }
                                            }
                                        }
                                    }

                                    ChartType.PIE -> {
                                        pieChartData?.let { data ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                PieChartWithCenter(
                                                    data = data.items,
                                                    selectedIndex = selectedPieIndex,
                                                    onSliceClick = { index ->
                                                        selectedPieIndex =
                                                            if (selectedPieIndex == index) -1 else index
                                                    }
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                // 分区图例
                                                data.items.forEach { item ->
                                                    PieLegendRow(item = item)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 排行榜数据标题
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "排行榜详情",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "共 ${rankingList.size} 个视频",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 排行榜列表
                    itemsIndexed(rankingList.take(20)) { index, item ->
                        RankingVideoItem(
                            rank = index + 1,
                            item = item,
                            onClick = { onVideoClick?.invoke(item) }
                        )
                        if (index < 19) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            is ChartUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadData() }
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PieLegendRow(item: PieChartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color(item.color), CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%.1f%%".format(item.value),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RankingVideoItem(
    rank: Int,
    item: BilibiliRankingItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 排名
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    when (rank) {
                        1 -> Color(0xFFFFD700)  // 金
                        2 -> Color(0xFFC0C0C0)  // 银
                        3 -> Color(0xFFCD7F32)  // 铜
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 封面
        AsyncImage(
            model = item.pic,
            contentDescription = item.title,
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 视频信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.owner.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 播放量
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCount(item.stat.view),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 点赞
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCount(item.stat.like),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatCount(count: Long): String {
    return when {
        count >= 100_000_000 -> "%.1f亿".format(count / 100_000_000f)
        count >= 10_000 -> "%.1f万".format(count / 10_000f)
        else -> count.toString()
    }
}

@Composable
private fun PieChartWithCenter(
    data: List<PieChartItem>,
    selectedIndex: Int,
    onSliceClick: (Int) -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
    ) {
        AppPieChart(
            data = ovo.sypw.androidendproject.data.model.PieChartData(
                title = "",
                items = data
            ),
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .animateContentSize()
                .padding(32.dp)
        ) {
            if (selectedIndex >= 0 && selectedIndex < data.size) {
                val item = data[selectedIndex]
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "%.1f%%".format(item.value),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(item.color)
                )
            } else {
                Text(
                    text = "分区分布",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
