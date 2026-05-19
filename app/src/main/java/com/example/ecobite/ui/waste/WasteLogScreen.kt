package com.example.ecobite.ui.waste

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecobite.data.local.entities.PantryItem
import com.example.ecobite.domain.calculator.Co2Calculator
import com.example.ecobite.ui.navigation.EcoBiteTopBar
import com.example.ecobite.ui.pantry.PantryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteLogScreen(
    viewModel: PantryViewModel,
    onNavigateBack: () -> Unit
) {
    val pantryItems    by viewModel.allPantryItems.collectAsState()
    val snackbarState  = remember { SnackbarHostState() }
    val uiMessage      by viewModel.uiMessage.collectAsState()
    val smartWasteReasonState by viewModel.smartWasteReasonState.collectAsState()

    var selectedItem   by remember { mutableStateOf<PantryItem?>(null) }
    var quantity       by remember { mutableStateOf("") }
    var useAllQuantity by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var preventionTip  by remember { mutableStateOf("") }
    var showError      by remember { mutableStateOf(false) }

    // Preview of CO2 + cost impact
    val previewCo2 = remember(selectedItem, quantity) {
        val item = selectedItem ?: return@remember null
        val qty  = quantity.toFloatOrNull() ?: return@remember null
        Co2Calculator.calculateCo2(item.category, qty, item.unit)
    }
    val previewWater = remember(selectedItem, quantity) {
        val item = selectedItem ?: return@remember null
        val qty  = quantity.toFloatOrNull() ?: return@remember null
        Co2Calculator.calculateWater(item.category, qty, item.unit)
    }

    val reasonTags = listOf(
        "🙈  Forgot about it",
        "🛒  Bought too much",
        "⏰  Went bad faster",
        "😕  Didn't like it"
    )
    val reasonKeys = listOf("forgot", "too_much", "went_bad", "disliked")

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(smartWasteReasonState.suggestion) {
        smartWasteReasonState.suggestion?.let { suggestion ->
            selectedReason = suggestion.reasonKey
            preventionTip = suggestion.preventionTip
            viewModel.clearSmartWasteReasonSuggestion()
        }
    }

    LaunchedEffect(selectedItem, useAllQuantity) {
        if (useAllQuantity) {
            quantity = selectedItem?.quantity?.toString().orEmpty()
        }
    }

    Scaffold(
        topBar = {
            EcoBiteTopBar(
                title = "Log Waste",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Select item from pantry ───────────────────────────────────
            Text(
                text = "What did you waste?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (pantryItems.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No items in pantry. Add items first.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedItem?.name ?: "Select an item",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pantry Item *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = showError && selectedItem == null
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        pantryItems.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text("${item.name} — ${item.quantity} ${item.unit}")
                                },
                                onClick = {
                                    selectedItem = item
                                    if (useAllQuantity) {
                                        quantity = item.quantity.toString()
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Quantity wasted ───────────────────────────────────────────
            Text(
                text = "How much was wasted?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        useAllQuantity = false
                    },
                    label = {
                        Text("Quantity (${selectedItem?.unit ?: "units"})")
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text
                        .KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                    isError = showError && quantity.isBlank(),
                    supportingText = {
                        if (showError && quantity.isBlank())
                            Text("Quantity is required")
                    }
                )
                FilterChip(
                    selected = useAllQuantity,
                    onClick = {
                        useAllQuantity = !useAllQuantity
                        if (useAllQuantity) {
                            quantity = selectedItem?.quantity?.toString().orEmpty()
                        }
                    },
                    enabled = selectedItem != null,
                    label = { Text("All") },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // ── Reason tags ───────────────────────────────────────────────
            Text(
                text = "Why was it wasted?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = {
                    viewModel.generateSmartWasteReason(
                        pantryItem = selectedItem,
                        quantityWasted = quantity
                    )
                },
                enabled = selectedItem != null && !smartWasteReasonState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                if (smartWasteReasonState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choosing reason...")
                } else {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Smart Reason")
                }
            }

            if (preventionTip.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Prevention Tip",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = preventionTip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reasonTags.forEachIndexed { index, label ->
                    val key = reasonKeys[index]
                    val isSelected = selectedReason == key
                    OutlinedButton(
                        onClick = { selectedReason = key },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = if (isSelected) 2.dp else 1.dp
                        )
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (showError && selectedReason.isBlank()) {
                    Text(
                        text = "Please select a reason",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ── Live impact preview ───────────────────────────────────────
            if (previewCo2 != null && previewWater != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Environmental Impact",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "CO2: ${String.format("%.2f", previewCo2)} kg " +
                                    "≈ ${Co2Calculator.co2Equivalency(previewCo2)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF388E3C)
                        )
                        Text(
                            text = "Water: ${String.format("%.0f", previewWater)}L " +
                                    "≈ ${Co2Calculator.waterEquivalency(previewWater)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF388E3C)
                        )
                        selectedItem?.let { item ->
                            val qty = quantity.toFloatOrNull() ?: 0f
                            val cost = if (item.quantity > 0)
                                (qty / item.quantity) * item.purchasePrice else 0f
                            Text(
                                text = "Cost wasted: ₹${String.format("%.2f", cost)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF388E3C),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ── Log waste button ──────────────────────────────────────────
            Button(
                onClick = {
                    if (selectedItem == null ||
                        quantity.isBlank() ||
                        selectedReason.isBlank()) {
                        showError = true
                        return@Button
                    }
                    viewModel.logWaste(
                        pantryItem     = selectedItem!!,
                        quantityWasted = quantity.toFloatOrNull() ?: 0f,
                        reasonTag      = selectedReason
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Log Waste", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
//```

//---
//
//## What's Happening Here
//
//**Live impact preview** — as soon as the user selects an item and enters a quantity, the CO2, water, and cost impact updates in real time before they even submit. This is the moment of reflection that drives behaviour change — seeing *"₹80 wasted, equal to driving 15 km"* before confirming makes it real.
//
//**`remember(selectedItem, quantity)`** — recalculates the preview only when those two values change, not on every recomposition. This is how you keep Compose performant.
//
//**Reason tags as buttons** — instead of a dropdown, reasons are full-width buttons that highlight when selected. Faster and more satisfying to tap than a dropdown for a 4-option choice.
//
//**No delete from pantry here** — logging waste is a separate action from deleting a pantry item. A user might waste half their spinach but still have some left. The two actions are intentionally decoupled.
//
//---
//
//## Your `ui.waste` package now has:
//```
//ui.waste/
//WasteLogScreen.kt
