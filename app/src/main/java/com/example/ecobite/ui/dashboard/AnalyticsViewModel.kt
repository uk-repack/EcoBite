package com.example.ecobite.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecobite.data.repository.PantryRepository
import com.example.ecobite.domain.calculator.ChartDataHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: PantryRepository
) : ViewModel() {

    // ── Chart data states ─────────────────────────────────────────────────

    private val _weeklyWasteByDay = MutableStateFlow<Map<String, Float>>(emptyMap())
    val weeklyWasteByDay: StateFlow<Map<String, Float>> =
        _weeklyWasteByDay.asStateFlow()

    private val _weeklyCo2ByDay = MutableStateFlow<Map<String, Float>>(emptyMap())
    val weeklyCo2ByDay: StateFlow<Map<String, Float>> =
        _weeklyCo2ByDay.asStateFlow()

    private val _monthlyWasteByWeek = MutableStateFlow<Map<String, Float>>(emptyMap())
    val monthlyWasteByWeek: StateFlow<Map<String, Float>> =
        _monthlyWasteByWeek.asStateFlow()

    private val _wasteByCategoryByDay = MutableStateFlow<Map<String, Float>>(emptyMap())
    val wasteByCategoryByDay: StateFlow<Map<String, Float>> =
        _wasteByCategoryByDay.asStateFlow()

    private val _wasteByReason = MutableStateFlow<Map<String, Int>>(emptyMap())
    val wasteByReason: StateFlow<Map<String, Int>> =
        _wasteByReason.asStateFlow()

    private val _insights = MutableStateFlow<List<String>>(emptyList())
    val insights: StateFlow<List<String>> = _insights.asStateFlow()

    // ── Summary stats ─────────────────────────────────────────────────────

    private val _totalCo2AllTime = MutableStateFlow(0f)
    val totalCo2AllTime: StateFlow<Float> = _totalCo2AllTime.asStateFlow()

    private val _totalCostAllTime = MutableStateFlow(0f)
    val totalCostAllTime: StateFlow<Float> = _totalCostAllTime.asStateFlow()

    private val _totalWaterAllTime = MutableStateFlow(0f)
    val totalWaterAllTime: StateFlow<Float> = _totalWaterAllTime.asStateFlow()

    // ── Init — load everything on creation ────────────────────────────────

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            repository.allWasteLogs.collect { logs ->

                // chart data
                _weeklyWasteByDay.value   = ChartDataHelper.weeklyWasteByDay(logs)
                _weeklyCo2ByDay.value     = ChartDataHelper.weeklyCo2ByDay(logs)
                _monthlyWasteByWeek.value = ChartDataHelper.monthlyWasteByWeek(logs)
                _wasteByCategoryByDay.value = ChartDataHelper.wasteByCategoryThisWeek(logs)
                _wasteByReason.value      = ChartDataHelper.wasteByReason(logs)

                // insights
                _insights.value = ChartDataHelper.generateInsights(logs)

                // all time totals
                _totalCo2AllTime.value = logs.sumOf {
                    it.co2Kg.toDouble()
                }.toFloat()

                _totalCostAllTime.value = logs.sumOf {
                    it.costWasted.toDouble()
                }.toFloat()

                _totalWaterAllTime.value = logs.sumOf {
                    it.waterLitres.toDouble()
                }.toFloat()
            }
        }
    }

    fun refresh() {
        loadAnalytics()
    }
}