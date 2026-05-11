package com.example.ecobite.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecobite.ui.navigation.EcoBiteTopBar
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val weeklyWaste   by viewModel.weeklyWasteByDay.collectAsState()
    val weeklyCo2     by viewModel.weeklyCo2ByDay.collectAsState()
    val monthlyWaste  by viewModel.monthlyWasteByWeek.collectAsState()
    val wasteByReason by viewModel.wasteByReason.collectAsState()
    val insights      by viewModel.insights.collectAsState()
    val totalCo2      by viewModel.totalCo2AllTime.collectAsState()
    val totalCost     by viewModel.totalCostAllTime.collectAsState()
    val totalWater    by viewModel.totalWaterAllTime.collectAsState()

    val weeklyWasteProducer = remember { ChartEntryModelProducer() }
    val weeklyCo2Producer   = remember { ChartEntryModelProducer() }
    val monthlyProducer     = remember { ChartEntryModelProducer() }

    LaunchedEffect(weeklyWaste) {
        if (weeklyWaste.isNotEmpty()) {
            weeklyWasteProducer.setEntries(
                weeklyWaste.values.mapIndexed { index, value ->
                    entryOf(index.toFloat(), value)
                }
            )
        }
    }

    LaunchedEffect(weeklyCo2) {
        if (weeklyCo2.isNotEmpty()) {
            weeklyCo2Producer.setEntries(
                weeklyCo2.values.mapIndexed { index, value ->
                    entryOf(index.toFloat(), value)
                }
            )
        }
    }

    LaunchedEffect(monthlyWaste) {
        if (monthlyWaste.isNotEmpty()) {
            monthlyProducer.setEntries(
                monthlyWaste.values.mapIndexed { index, value ->
                    entryOf(index.toFloat(), value)
                }
            )
        }
    }

    Scaffold(
        topBar = {
            EcoBiteTopBar(title = "Analytics")
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            item {
                Text(
                    text = "All Time Impact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AllTimeCard(
                        emoji = "🌍",
                        label = "CO2",
                        value = "${String.format("%.1f", totalCo2)} kg",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    AllTimeCard(
                        emoji = "💸",
                        label = "Money",
                        value = "₹${String.format("%.0f", totalCost)}",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    AllTimeCard(
                        emoji = "💧",
                        label = "Water",
                        value = "${String.format("%.0f", totalWater)}L",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Weekly Waste (kg)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                ChartCard {
                    if (weeklyWaste.values.any { it > 0f }) {
                        Chart(
                            chart = columnChart(),
                            chartModelProducer = weeklyWasteProducer,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        EmptyChartState("No waste logged this week")
                    }
                }
            }

            item {
                Text(
                    text = "Weekly CO2 Footprint (kg)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                ChartCard {
                    if (weeklyCo2.values.any { it > 0f }) {
                        Chart(
                            chart = lineChart(),
                            chartModelProducer = weeklyCo2Producer,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        EmptyChartState("No data yet")
                    }
                }
            }

            item {
                Text(
                    text = "Monthly Waste Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                ChartCard {
                    if (monthlyWaste.values.any { it > 0f }) {
                        Chart(
                            chart = columnChart(),
                            chartModelProducer = monthlyProducer,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        EmptyChartState("No data for this month")
                    }
                }
            }

            item {
                Text(
                    text = "Why You Waste",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                ChartCard {
                    if (wasteByReason.isEmpty()) {
                        EmptyChartState("No waste logged yet")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val total = wasteByReason.values.sum().toFloat().coerceAtLeast(1f)
                            wasteByReason.forEach { (reason, count) ->
                                val percent = (count / total) * 100f
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(reason)
                                        Text("${String.format("%.0f", percent)}%")
                                    }
                                    LinearProgressIndicator(
                                        progress = { percent / 100f },
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (insights.isNotEmpty()) {
                item {
                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        insights.forEach {
                            InsightCard(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChartCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}

@Composable
fun EmptyChartState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AllTimeCard(
    emoji: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji)
            Text(value, fontWeight = FontWeight.Bold)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InsightCard(insight: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("💡")
            Text(
                text = insight,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}