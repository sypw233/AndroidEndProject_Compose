package ovo.sypw.androidendproject.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.bytebeats.views.charts.bar.BarChart
import me.bytebeats.views.charts.bar.BarChartData
import me.bytebeats.views.charts.bar.render.label.SimpleLabelDrawer
import me.bytebeats.views.charts.bar.render.bar.SimpleBarDrawer
import me.bytebeats.views.charts.line.LineChart
import me.bytebeats.views.charts.line.LineChartData
import me.bytebeats.views.charts.line.render.line.SolidLineDrawer
import me.bytebeats.views.charts.line.render.point.FilledCircularPointDrawer
import me.bytebeats.views.charts.pie.PieChart
import me.bytebeats.views.charts.pie.PieChartData
import me.bytebeats.views.charts.pie.render.SimpleSliceDrawer
import ovo.sypw.androidendproject.data.model.BarChartData as AppBarChartData
import ovo.sypw.androidendproject.data.model.LineChartData as AppLineChartData
import ovo.sypw.androidendproject.data.model.PieChartData as AppPieChartData

@Composable
fun AppLineChart(
    data: AppLineChartData,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    LineChart(
        lineChartData = LineChartData(
            points = data.values.mapIndexed { index, value ->
                LineChartData.Point(value, data.labels.getOrElse(index) { "" })
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(16.dp),
        pointDrawer = FilledCircularPointDrawer(color = primaryColor),
        lineDrawer = SolidLineDrawer(color = primaryColor)
    )
}

@Composable
fun AppBarChart(
    data: AppBarChartData,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    BarChart(
        barChartData = BarChartData(
            bars = data.values.mapIndexed { index, value ->
                BarChartData.Bar(
                    value = value,
                    label = data.labels.getOrElse(index) { "" },
                    color = primaryColor
                )
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(16.dp),
        barDrawer = SimpleBarDrawer(),
        labelDrawer = SimpleLabelDrawer()
    )
}

@Composable
fun AppPieChart(
    data: AppPieChartData,
    modifier: Modifier = Modifier
) {
    PieChart(
        pieChartData = PieChartData(
            slices = data.items.map { item ->
                PieChartData.Slice(
                    value = item.value,
                    color = Color(item.color)
                )
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(16.dp),
        sliceDrawer = SimpleSliceDrawer()
    )
}
