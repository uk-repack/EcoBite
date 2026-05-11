package com.example.ecobite.domain.calculator

import com.example.ecobite.data.local.entities.WasteLog
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object ChartDataHelper {

    // ── Weekly waste by day ───────────────────────────────────────────────
    // Returns a map of day label → total kg wasted that day
    fun weeklyWasteByDay(logs: List<WasteLog>): Map<String, Float> {
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val weekAgo   = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

        val result = linkedMapOf(
            "Mon" to 0f, "Tue" to 0f, "Wed" to 0f,
            "Thu" to 0f, "Fri" to 0f, "Sat" to 0f, "Sun" to 0f
        )

        logs.filter { it.dateWasted >= weekAgo }
            .forEach { log ->
                val day = dayFormat.format(Date(log.dateWasted))
                result[day] = (result[day] ?: 0f) + log.quantityWasted
            }

        return result
    }

    // ── Weekly CO2 by day ─────────────────────────────────────────────────
    fun weeklyCo2ByDay(logs: List<WasteLog>): Map<String, Float> {
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val weekAgo   = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

        val result = linkedMapOf(
            "Mon" to 0f, "Tue" to 0f, "Wed" to 0f,
            "Thu" to 0f, "Fri" to 0f, "Sat" to 0f, "Sun" to 0f
        )

        logs.filter { it.dateWasted >= weekAgo }
            .forEach { log ->
                val day = dayFormat.format(Date(log.dateWasted))
                result[day] = (result[day] ?: 0f) + log.co2Kg
            }

        return result
    }

    // ── Monthly waste by week ─────────────────────────────────────────────
    fun monthlyWasteByWeek(logs: List<WasteLog>): Map<String, Float> {
        val monthAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        val result   = linkedMapOf(
            "Week 1" to 0f,
            "Week 2" to 0f,
            "Week 3" to 0f,
            "Week 4" to 0f
        )

        logs.filter { it.dateWasted >= monthAgo }
            .forEach { log ->
                val daysAgo = TimeUnit.MILLISECONDS.toDays(
                    System.currentTimeMillis() - log.dateWasted
                )
                val weekLabel = when {
                    daysAgo <= 7  -> "Week 4"
                    daysAgo <= 14 -> "Week 3"
                    daysAgo <= 21 -> "Week 2"
                    else          -> "Week 1"
                }
                result[weekLabel] = (result[weekLabel] ?: 0f) + log.quantityWasted
            }

        return result
    }

    // ── Waste by category ─────────────────────────────────────────────────
    fun wasteByCategoryThisWeek(logs: List<WasteLog>): Map<String, Float> {
        val weekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        return logs
            .filter { it.dateWasted >= weekAgo }
            .groupBy { it.category }
            .mapValues { (_, logs) -> logs.sumOf { it.quantityWasted.toDouble() }.toFloat() }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    // ── Waste by reason ───────────────────────────────────────────────────
    fun wasteByReason(logs: List<WasteLog>): Map<String, Int> {
        val weekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        return logs
            .filter { it.dateWasted >= weekAgo }
            .groupBy { it.reasonTag }
            .mapValues { (_, logs) -> logs.size }
    }

    // ── Insight engine ────────────────────────────────────────────────────
    fun generateInsights(logs: List<WasteLog>): List<String> {
        val insights = mutableListOf<String>()
        if (logs.isEmpty()) return insights

        val weekAgo  = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        val monthAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)

        val weekLogs  = logs.filter { it.dateWasted >= weekAgo }
        val monthLogs = logs.filter { it.dateWasted >= monthAgo }

        // most wasted category
        val topCategory = wasteByCategoryThisWeek(logs).keys.firstOrNull()
        if (topCategory != null) {
            insights.add("You waste the most $topCategory — consider buying less.")
        }

        // most common reason
        val topReason = wasteByReason(logs)
            .maxByOrNull { it.value }?.key
        if (topReason != null) {
            val message = when (topReason) {
                "forgot"    -> "Most of your waste is forgotten items — try moving expiring food to the front."
                "too_much"  -> "You often buy too much — try smaller quantities more frequently."
                "went_bad"  -> "Food is going bad faster than expected — check your fridge temperature."
                "disliked"  -> "You're wasting food you don't enjoy — experiment with new recipes."
                else        -> "Review your shopping habits to reduce waste."
            }
            insights.add(message)
        }

        // most wasteful day
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val worstDay  = weekLogs
            .groupBy { dayFormat.format(Date(it.dateWasted)) }
            .mapValues { (_, l) -> l.sumOf { it.quantityWasted.toDouble() }.toFloat() }
            .maxByOrNull { it.value }?.key
        if (worstDay != null && weekLogs.size >= 3) {
            insights.add("You waste the most food on $worstDay.")
        }

        // cost this month
        val monthlyCost = monthLogs.sumOf { it.costWasted.toDouble() }.toFloat()
        if (monthlyCost > 200f) {
            insights.add(
                "You've wasted ₹${String.format("%.0f", monthlyCost)} worth of food this month."
            )
        }

        return insights
    }
}