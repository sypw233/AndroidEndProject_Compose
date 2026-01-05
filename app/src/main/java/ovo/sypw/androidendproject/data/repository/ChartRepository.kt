package ovo.sypw.androidendproject.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ovo.sypw.androidendproject.data.model.BarChartData
import ovo.sypw.androidendproject.data.model.LineChartData
import ovo.sypw.androidendproject.data.model.PieChartData

class ChartRepository {

    fun getLineChartData(): Flow<Result<LineChartData>> = flow {
        // 实际项目中可从网络或数据库获取
        emit(Result.success(LineChartData.mock()))
    }

    fun getBarChartData(): Flow<Result<BarChartData>> = flow {
        emit(Result.success(BarChartData.mock()))
    }

    fun getPieChartData(): Flow<Result<PieChartData>> = flow {
        emit(Result.success(PieChartData.mock()))
    }
}
