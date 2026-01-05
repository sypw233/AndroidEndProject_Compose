package ovo.sypw.androidendproject.ui.screens.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ovo.sypw.androidendproject.data.model.BarChartData
import ovo.sypw.androidendproject.data.model.LineChartData
import ovo.sypw.androidendproject.data.model.PieChartData
import ovo.sypw.androidendproject.data.repository.ChartRepository

class ChartViewModel(
    private val chartRepository: ChartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Loading)
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    private val _lineChartData = MutableStateFlow<LineChartData?>(null)
    val lineChartData: StateFlow<LineChartData?> = _lineChartData.asStateFlow()

    private val _barChartData = MutableStateFlow<BarChartData?>(null)
    val barChartData: StateFlow<BarChartData?> = _barChartData.asStateFlow()

    private val _pieChartData = MutableStateFlow<PieChartData?>(null)
    val pieChartData: StateFlow<PieChartData?> = _pieChartData.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Loading

            // 加载折线图数据
            chartRepository.getLineChartData()
                .catch { }
                .collect { result ->
                    result.fold(
                        onSuccess = { _lineChartData.value = it },
                        onFailure = { }
                    )
                }
        }

        viewModelScope.launch {
            chartRepository.getBarChartData()
                .catch { }
                .collect { result ->
                    result.fold(
                        onSuccess = { _barChartData.value = it },
                        onFailure = { }
                    )
                }
        }

        viewModelScope.launch {
            chartRepository.getPieChartData()
                .catch { }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _pieChartData.value = it
                            _uiState.value = ChartUiState.Success(
                                lineChartData = _lineChartData.value,
                                barChartData = _barChartData.value,
                                pieChartData = it
                            )
                        },
                        onFailure = { e ->
                            _uiState.value = ChartUiState.Error(e.message ?: "加载失败")
                        }
                    )
                }
        }
    }
}
