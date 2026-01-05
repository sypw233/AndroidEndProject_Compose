package ovo.sypw.androidendproject.ui.screens.chart

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ovo.sypw.androidendproject.data.model.PieChartItem
import ovo.sypw.androidendproject.ui.components.AppBarChart
import ovo.sypw.androidendproject.ui.components.AppLineChart
import ovo.sypw.androidendproject.ui.components.AppPieChart
import ovo.sypw.androidendproject.ui.components.ErrorView
import ovo.sypw.androidendproject.ui.components.LoadingIndicator
import ovo.sypw.androidendproject.ui.components.SpeedDialFab

@Composable
fun ChartScreen(viewModel: ChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lineChartData by viewModel.lineChartData.collectAsStateWithLifecycle()
    val barChartData by viewModel.barChartData.collectAsStateWithLifecycle()
    val pieChartData by viewModel.pieChartData.collectAsStateWithLifecycle()

    var selectedChartType by remember { mutableStateOf(ChartType.LINE) }
    // 饼图选中的扇形索引 (-1 表示未选中)
    var selectedPieIndex by remember { mutableIntStateOf(-1) }

    Scaffold(
        floatingActionButton = {
            SpeedDialFab(
                onChartTypeSelected = { chartType ->
                    selectedChartType = chartType
                    selectedPieIndex = -1 // 切换图表时重置选中状态
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ChartUiState.Loading -> {
                LoadingIndicator()
            }

            is ChartUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 标题
                    Text(
                        text = when (selectedChartType) {
                            ChartType.LINE -> lineChartData?.title ?: "折线图"
                            ChartType.BAR -> barChartData?.title ?: "柱状图"
                            ChartType.PIE -> pieChartData?.title ?: "饼图"
                        },
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 图表内容区域
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        when (selectedChartType) {
                            ChartType.LINE -> {
                                lineChartData?.let { data ->
                                    Column {
                                        AppLineChart(data = data)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        ChartLegend(
                                            description = "Java工程师经验与工资的对应情况",
                                            labels = data.labels,
                                            values = data.values.map { "${it.toInt()}元" }
                                        )
                                    }
                                }
                            }

                            ChartType.BAR -> {
                                barChartData?.let { data ->
                                    Column {
                                        AppBarChart(data = data)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        ChartLegend(
                                            description = "Java/PHP工程师经验与工资的对应情况",
                                            labels = data.labels,
                                            values = data.values.map { "${it.toInt()}元" }
                                        )
                                    }
                                }
                            }

                            ChartType.PIE -> {
                                pieChartData?.let { data ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // 饼图 (带中心文字)
                                        PieChartWithCenter(
                                            data = data.items,
                                            selectedIndex = selectedPieIndex,
                                            onSliceClick = { index ->
                                                selectedPieIndex =
                                                    if (selectedPieIndex == index) -1 else index
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // 图例
                                        Text(
                                            text = "Android工程师薪资占比情况",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        data.items.forEachIndexed { index, item ->
                                            PieLegendItem(
                                                item = item,
                                                isSelected = selectedPieIndex == index,
                                                onClick = {
                                                    selectedPieIndex =
                                                        if (selectedPieIndex == index) -1 else index
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
private fun PieChartWithCenter(
    data: List<PieChartItem>,
    selectedIndex: Int,
    onSliceClick: (Int) -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        // 饼图
        AppPieChart(
            data = ovo.sypw.androidendproject.data.model.PieChartData(
                title = "",
                items = data
            ),
            modifier = Modifier.fillMaxSize()
        )

        // 中心文字
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
                    text = "${item.value}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(item.color)
                )
            } else {
                Text(
                    text = "点击显示",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "相关数据",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PieLegendItem(
    item: PieChartItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(item.color).copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 颜色指示器
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(item.color))
            )
            Spacer(modifier = Modifier.width(12.dp))
            // 标签
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            // 百分比
            Text(
                text = "${item.value}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(item.color) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ChartLegend(
    description: String,
    labels: List<String>,
    values: List<String>
) {
    Column {
        Text(
            text = description,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        labels.zip(values).forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "• $label",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
