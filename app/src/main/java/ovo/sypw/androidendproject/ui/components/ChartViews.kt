package ovo.sypw.androidendproject.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.androidendproject.utils.formatLargeNumber
import kotlin.math.roundToInt
import ovo.sypw.androidendproject.data.model.BarChartData as AppBarChartData
import ovo.sypw.androidendproject.data.model.LineChartData as AppLineChartData
import ovo.sypw.androidendproject.data.model.PieChartData as AppPieChartData

/**
 * 自定义可交互折线图
 */
@Composable
fun AppLineChart(
    data: AppLineChartData,
    modifier: Modifier = Modifier,
    onPointClick: ((Int, Float) -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline

    var selectedIndex by remember { mutableIntStateOf(-1) }
    var animationProgress by remember { mutableFloatStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 800),
        label = "lineAnimation"
    )

    LaunchedEffect(data) {
        animationProgress = 1f
    }

    val values = data.values
    val labels = data.labels

    if (values.isEmpty()) return

    val maxValue = values.maxOrNull() ?: 0f
    val range = if (maxValue > 0) maxValue * 1.1f else 1f
    val avgValue = values.average().toFloat()

    Column(modifier = modifier) {
        // 选中点详情
        if (selectedIndex in values.indices) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = primaryColor.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${selectedIndex + 1}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = labels.getOrElse(selectedIndex) { "第${selectedIndex + 1}名" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "播放量: ${values[selectedIndex].formatLargeNumber()}万",
                            style = MaterialTheme.typography.bodyLarge,
                            color = primaryColor
                        )
                    }
                }
            }
        }

        // 图例
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .background(outlineColor.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "平均值: ${avgValue.formatLargeNumber()}万",
                style = MaterialTheme.typography.labelMedium,
                color = outlineColor
            )
        }

        // 图表
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .pointerInput(values) {
                    detectTapGestures { offset ->
                        val chartWidth = size.width.toFloat()
                        val padding = 50f
                        val availableWidth = chartWidth - padding * 2
                        val pointSpacing =
                            if (values.size > 1) availableWidth / (values.size - 1) else availableWidth
                        val clickedIndex = ((offset.x - padding) / pointSpacing).roundToInt()
                            .coerceIn(0, values.size - 1)
                        selectedIndex = if (selectedIndex == clickedIndex) -1 else clickedIndex
                        onPointClick?.invoke(clickedIndex, values[clickedIndex])
                    }
                }
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val padding = 50f
            val topPadding = 20f
            val bottomPadding = 35f
            val availableWidth = chartWidth - padding * 2
            val availableHeight = chartHeight - topPadding - bottomPadding
            val pointSpacing =
                if (values.size > 1) availableWidth / (values.size - 1) else availableWidth

            // Y轴刻度
            for (i in 0..4) {
                val y = topPadding + availableHeight * (1 - i / 4f)
                val value = range * i / 4f
                drawLine(
                    color = outlineColor.copy(alpha = 0.15f),
                    start = Offset(padding, y),
                    end = Offset(chartWidth - padding / 2, y),
                    strokeWidth = 1f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    value.formatLargeNumber(),
                    padding - 8f, y + 5f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }

            // 平均线
            val avgY = topPadding + availableHeight * (1 - avgValue / range)
            drawLine(
                color = outlineColor.copy(alpha = 0.6f),
                start = Offset(padding, avgY),
                end = Offset(chartWidth - padding / 2, avgY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
            )

            // 计算点位置
            val points = values.mapIndexed { index, value ->
                val x = padding + index * pointSpacing
                val normalizedValue = (value / range).coerceIn(0f, 1f)
                val y = topPadding + availableHeight * (1 - normalizedValue * animatedProgress)
                Offset(x, y)
            }

            // 填充区域
            if (points.size > 1) {
                val fillPath = Path().apply {
                    moveTo(points[0].x, chartHeight - bottomPadding)
                    lineTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                    lineTo(points.last().x, chartHeight - bottomPadding)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f),
                            primaryColor.copy(alpha = 0.05f)
                        )
                    )
                )
            }

            // 折线
            if (points.size > 1) {
                val linePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(linePath, primaryColor, style = Stroke(width = 4f))
            }

            // 数据点
            points.forEachIndexed { index, point ->
                val isSelected = index == selectedIndex
                drawCircle(
                    color = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.8f),
                    radius = if (isSelected) 14f else 10f,
                    center = point
                )
                drawCircle(
                    color = surfaceColor,
                    radius = if (isSelected) 8f else 6f,
                    center = point
                )
                drawCircle(
                    color = primaryColor,
                    radius = if (isSelected) 5f else 3f,
                    center = point
                )
            }

            // X轴标签
            points.forEachIndexed { index, point ->
                if (index % 2 == 0 || points.size <= 6) {
                    drawContext.canvas.nativeCanvas.drawText(
                        labels.getOrElse(index) { "" },
                        point.x, chartHeight - 8f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

/**
 * 自定义可交互柱状图
 */
@Composable
fun AppBarChart(
    data: AppBarChartData,
    modifier: Modifier = Modifier,
    onBarClick: ((Int) -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val outlineColor = MaterialTheme.colorScheme.outline

    var selectedIndex by remember { mutableIntStateOf(-1) }
    var animationProgress by remember { mutableFloatStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 600),
        label = "barAnimation"
    )

    LaunchedEffect(data) {
        animationProgress = 1f
    }

    val values1 = data.values
    val values2 = data.values2
    val labels = data.labels
    val hasTwoGroups = values2 != null

    if (values1.isEmpty()) return

    val maxValue = if (hasTwoGroups) {
        maxOf(values1.maxOrNull() ?: 0f, values2?.maxOrNull() ?: 0f) * 1.1f
    } else {
        (values1.maxOrNull() ?: 0f) * 1.1f
    }

    Column(modifier = modifier) {
        // 选中详情
        if (selectedIndex in values1.indices) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = labels.getOrElse(selectedIndex) { "第${selectedIndex + 1}名" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(primaryColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "点赞: ${values1[selectedIndex].formatLargeNumber()}千",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (hasTwoGroups) {
                            Spacer(modifier = Modifier.width(24.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(secondaryColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "投币: ${
                                        (values2?.getOrNull(
                                            selectedIndex
                                        ) ?: 0f).formatLargeNumber()
                                    }千",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 图例
        if (hasTwoGroups) {
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(primaryColor, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("点赞", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(20.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(secondaryColor, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("投币", style = MaterialTheme.typography.labelMedium)
            }
        }

        // 图表
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .pointerInput(values1) {
                    detectTapGestures { offset ->
                        val chartWidth = size.width.toFloat()
                        val padding = 50f
                        val availableWidth = chartWidth - padding * 2
                        val barGroupWidth = availableWidth / values1.size
                        val clickedIndex = ((offset.x - padding) / barGroupWidth).toInt()
                            .coerceIn(0, values1.size - 1)
                        selectedIndex = if (selectedIndex == clickedIndex) -1 else clickedIndex
                        onBarClick?.invoke(clickedIndex)
                    }
                }
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val padding = 50f
            val topPadding = 10f
            val bottomPadding = 35f
            val availableWidth = chartWidth - padding * 2
            val availableHeight = chartHeight - topPadding - bottomPadding
            val barGroupWidth = availableWidth / values1.size
            val barWidth = if (hasTwoGroups) barGroupWidth * 0.35f else barGroupWidth * 0.6f
            val gap = barGroupWidth * 0.1f

            // Y轴刻度
            for (i in 0..4) {
                val y = topPadding + availableHeight * (1 - i / 4f)
                val value = maxValue * i / 4f
                drawLine(
                    color = outlineColor.copy(alpha = 0.15f),
                    start = Offset(padding, y),
                    end = Offset(chartWidth - padding / 2, y),
                    strokeWidth = 1f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    value.formatLargeNumber(),
                    padding - 8f, y + 5f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }

            // 柱状图
            values1.forEachIndexed { index, value1 ->
                val isSelected = index == selectedIndex
                val x = padding + index * barGroupWidth + gap

                // 第一组柱子
                val height1 = (value1 / maxValue) * availableHeight * animatedProgress
                val barColor1 = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.85f)
                drawRoundRect(
                    color = barColor1,
                    topLeft = Offset(x, topPadding + availableHeight - height1),
                    size = Size(barWidth, height1),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // 第二组柱子
                if (hasTwoGroups) {
                    val value2 = values2.getOrElse(index) { 0f }
                    val height2 = (value2 / maxValue) * availableHeight * animatedProgress
                    val barColor2 =
                        if (isSelected) secondaryColor else secondaryColor.copy(alpha = 0.85f)
                    drawRoundRect(
                        color = barColor2,
                        topLeft = Offset(x + barWidth + 4f, topPadding + availableHeight - height2),
                        size = Size(barWidth, height2),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }

                // 选中高亮
                if (isSelected) {
                    val totalBarWidth = if (hasTwoGroups) barWidth * 2 + 4f else barWidth
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.1f),
                        topLeft = Offset(x - 4f, topPadding),
                        size = Size(totalBarWidth + 8f, availableHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }

            // X轴标签
            values1.forEachIndexed { index, _ ->
                val x = padding + index * barGroupWidth + barGroupWidth / 2
                if (index % 2 == 0 || values1.size <= 6) {
                    drawContext.canvas.nativeCanvas.drawText(
                        labels.getOrElse(index) { "" },
                        x, chartHeight - 8f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

// formatLargeNumber moved to Extensions.kt as Float.formatLargeNumber()

/**
 * 自定义可交互饼图
 */
@Composable
fun AppPieChart(
    data: AppPieChartData,
    modifier: Modifier = Modifier,
    selectedIndex: Int = -1,
    onSliceClick: ((Int) -> Unit)? = null
) {
    val items = data.items
    if (items.isEmpty()) return

    var internalSelectedIndex by remember { mutableIntStateOf(selectedIndex) }

    // 同步外部选中状态
    LaunchedEffect(selectedIndex) {
        internalSelectedIndex = selectedIndex
    }

    val total = items.sumOf { it.value.toDouble() }.toFloat()

    Column(modifier = modifier) {
        // 选中详情
        if (internalSelectedIndex in items.indices) {
            val selectedItem = items[internalSelectedIndex]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(selectedItem.color).copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(selectedItem.color), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%.0f%%".format(selectedItem.value),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = selectedItem.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "占比 ${String.format("%.1f", selectedItem.value)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 饼图
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(items) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radius = minOf(centerX, centerY) * 0.8f

                        // 计算点击位置相对于中心的角度
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                        // 检查是否在饼图范围内
                        if (distance <= radius && distance >= radius * 0.3f) {
                            var angle = kotlin.math.atan2(dy, dx) * 180f / kotlin.math.PI.toFloat()
                            angle = (angle + 360f) % 360f  // 转换为正角度
                            angle = (angle + 90f) % 360f   // 从顶部开始

                            // 找到点击的扇形
                            var currentAngle = 0f
                            for (i in items.indices) {
                                val sweepAngle = items[i].value / total * 360f
                                if (angle >= currentAngle && angle < currentAngle + sweepAngle) {
                                    internalSelectedIndex =
                                        if (internalSelectedIndex == i) -1 else i
                                    onSliceClick?.invoke(i)
                                    break
                                }
                                currentAngle += sweepAngle
                            }
                        } else if (distance < radius * 0.3f) {
                            // 点击中心取消选中
                            internalSelectedIndex = -1
                            onSliceClick?.invoke(-1)
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = minOf(centerX, centerY) * 0.8f
            val innerRadius = radius * 0.5f

            var startAngle = -90f  // 从顶部开始

            items.forEachIndexed { index, item ->
                val sweepAngle = item.value / total * 360f
                val isSelected = index == internalSelectedIndex
                val scale = if (isSelected) 1.08f else 1f
                val actualRadius = radius * scale

                // 绘制扇形
                drawArc(
                    color = Color(item.color).let {
                        if (isSelected) it else it.copy(alpha = 0.9f)
                    },
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - actualRadius, centerY - actualRadius),
                    size = Size(actualRadius * 2, actualRadius * 2)
                )

                // 绘制边框
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - actualRadius, centerY - actualRadius),
                    size = Size(actualRadius * 2, actualRadius * 2),
                    style = Stroke(width = 3f)
                )

                startAngle += sweepAngle
            }

            // 绘制中心圆（环形效果）
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = Offset(centerX, centerY)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 图例
        items.forEachIndexed { index, item ->
            val isSelected = index == internalSelectedIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected) Color(item.color).copy(alpha = 0.1f)
                        else Color.Transparent
                    )
                    .clickable {
                        internalSelectedIndex = if (internalSelectedIndex == index) -1 else index
                        onSliceClick?.invoke(index)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(item.color), CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "%.1f%%".format(item.value),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(item.color) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
