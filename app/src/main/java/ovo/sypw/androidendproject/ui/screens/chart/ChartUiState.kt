package ovo.sypw.androidendproject.ui.screens.chart

import ovo.sypw.androidendproject.data.model.BarChartData
import ovo.sypw.androidendproject.data.model.LineChartData
import ovo.sypw.androidendproject.data.model.PieChartData

sealed interface ChartUiState {
    data object Loading : ChartUiState
    data class Success(
        val lineChartData: LineChartData?,
        val barChartData: BarChartData?,
        val pieChartData: PieChartData?
    ) : ChartUiState
    data class Error(val message: String) : ChartUiState
}

enum class ChartType {
    LINE, BAR, PIE
}
