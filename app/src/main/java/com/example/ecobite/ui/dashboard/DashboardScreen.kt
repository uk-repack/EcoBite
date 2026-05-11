package com.example.ecobite.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecobite.domain.calculator.Co2Calculator
import com.example.ecobite.ui.navigation.EcoBiteTopBar
import com.example.ecobite.ui.pantry.PantryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PantryViewModel,
    onGoToPantry: () -> Unit
) {
    val pantryItems    by viewModel.allPantryItems.collectAsState()
    val wasteLogs      by viewModel.allWasteLogs.collectAsState()
    val expiringItems  by viewModel.expiringItems.collectAsState()
    val weeklyCost     by viewModel.weeklyTotalCost.collectAsState()
    val weeklyCo2      by viewModel.weeklyTotalCo2.collectAsState()
    val weeklyWater    by viewModel.weeklyTotalWater.collectAsState()

    val healthScore = remember(pantryItems, expiringItems, wasteLogs) {
        calculateHealthScore(
            totalItems     = pantryItems.size,
            expiringCount  = expiringItems.size,
            weeklyWasteCount = wasteLogs.count {
                it.dateWasted >= System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            }
        )
    }

    Scaffold(
        topBar = {
            EcoBiteTopBar(title = "EcoBite")
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            HealthScoreCard(score = healthScore)

            if (expiringItems.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "⚠️ Expiring Soon",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${expiringItems.size} item${
                                    if (expiringItems.size > 1) "s" else ""
                                } expire within 48 hours",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = expiringItems.take(3)
                                    .joinToString(", ") { it.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = onGoToPantry) {
                            Text("View")
                        }
                    }
                }
            }

            Text(
                text = "This Week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    emoji = "💸",
                    label = "Wasted",
                    value = "₹${String.format("%.0f", weeklyCost)}",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    emoji = "🌍",
                    label = "CO2",
                    value = "${String.format("%.2f", weeklyCo2)} kg",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    emoji = "💧",
                    label = "Water",
                    value = "${String.format("%.0f", weeklyWater)}L",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    emoji = "🗑️",
                    label = "Waste logs",
                    value = "${wasteLogs.count {
                        it.dateWasted >= System.currentTimeMillis() -
                                7 * 24 * 60 * 60 * 1000L
                    }}",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            if (weeklyCo2 > 0f) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🌱 This week's food waste is equivalent to",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = Co2Calculator.co2Equivalency(weeklyCo2),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Water: ${Co2Calculator.waterEquivalency(weeklyWater)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Text(
                text = "Pantry Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    emoji = "📦",
                    label = "Total items",
                    value = "${pantryItems.size}",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    emoji = "⏰",
                    label = "Expiring soon",
                    value = "${expiringItems.size}",
                    containerColor = if (expiringItems.isNotEmpty())
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun HealthScoreCard(score: Int) {
    val scoreColor = when {
        score >= 80 -> MaterialTheme.colorScheme.primary
        score >= 60 -> MaterialTheme.colorScheme.secondary
        score >= 40 -> MaterialTheme.colorScheme.tertiary
        else        -> MaterialTheme.colorScheme.error
    }

    val scoreLabel = when {
        score >= 80 -> "Excellent 🌟"
        score >= 60 -> "Good 👍"
        score >= 40 -> "Fair ⚠️"
        else        -> "Needs attention 🚨"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Pantry Health Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = scoreLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scoreColor
                )
            }
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
        }
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .height(8.dp),
            color = scoreColor,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

@Composable
fun StatCard(
    emoji: String,
    label: String,
    value: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun calculateHealthScore(
    totalItems: Int,
    expiringCount: Int,
    weeklyWasteCount: Int
): Int {
    if (totalItems == 0) return 100

    val expiryPenalty = ((expiringCount.toFloat() / totalItems) * 40).toInt()
    val wastePenalty  = (weeklyWasteCount * 5).coerceAtMost(40)
    return (100 - expiryPenalty - wastePenalty).coerceIn(0, 100)
}