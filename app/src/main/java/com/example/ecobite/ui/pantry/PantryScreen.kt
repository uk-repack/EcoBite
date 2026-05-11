package com.example.ecobite.ui.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecobite.data.local.entities.PantryItem
import com.example.ecobite.ui.navigation.EcoBiteTopBar
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel,
    onAddItem: () -> Unit
) {
    val pantryItems by viewModel.allPantryItems.collectAsState()
    val uiMessage   by viewModel.uiMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            EcoBiteTopBar(title = "My Pantry")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItem,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Item",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (pantryItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🥦", style = MaterialTheme.typography.displayLarge)
                    Text(
                        "Your pantry is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap + to add your first item",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(
                    items = pantryItems,
                    key = { it.id }
                ) { item ->
                    PantryItemCard(
                        item = item,
                        onDelete = { viewModel.deleteItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun PantryItemCard(
    item: PantryItem,
    onDelete: () -> Unit
) {
    val daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(
        item.expiryDate - System.currentTimeMillis()
    )
    val isExpiringSoon = daysUntilExpiry <= 2
    val isExpired      = daysUntilExpiry < 0

    val isDark = MaterialTheme.colorScheme.background == Color.Black

    val cardColor = when {
        isExpired -> {
            if (isDark)
                MaterialTheme.colorScheme.error.copy(alpha = 0.22f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        }
        isExpiringSoon -> {
            if (isDark)
                MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f)
        }
        else -> MaterialTheme.colorScheme.surface
    }

    val expiryColor = when {
        isExpired      -> MaterialTheme.colorScheme.error
        isExpiringSoon -> MaterialTheme.colorScheme.error
        else           -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryEmoji(item.category),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.quantity} ${item.unit}  •  ₹${item.purchasePrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isExpiringSoon || isExpired) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = expiryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = when {
                            isExpired -> "Expired ${-daysUntilExpiry}d ago"
                            daysUntilExpiry == 0L -> "Expires today!"
                            daysUntilExpiry == 1L -> "Expires tomorrow"
                            else -> "Expires ${dateFormat.format(Date(item.expiryDate))}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = expiryColor,
                        fontWeight = if (isExpiringSoon || isExpired)
                            FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun categoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "vegetable" -> "🥦"
        "fruit"     -> "🍎"
        "dairy"     -> "🥛"
        "meat"      -> "🥩"
        "chicken"   -> "🍗"
        "fish"      -> "🐟"
        "grain"     -> "🌾"
        "rice"      -> "🍚"
        "legume"    -> "🫘"
        "egg"       -> "🥚"
        "tofu"      -> "🧈"
        "beef"      -> "🥩"
        "pork"      -> "🥩"
        else        -> "🍽️"
    }
}