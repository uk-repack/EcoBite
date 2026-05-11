package com.example.ecobite.ui.pantry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ecobite.data.local.entities.PantryItem
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    viewModel: PantryViewModel,
    onNavigateBack: () -> Unit,
    onOpenScanner: () -> Unit
) {
    val scanResult by viewModel.scanResult.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val uiMessage  by viewModel.uiMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Form state ────────────────────────────────────────────────────────
    var name       by remember { mutableStateOf("") }
    var quantity   by remember { mutableStateOf("") }
    var unit       by remember { mutableStateOf("units") }
    var category   by remember { mutableStateOf("vegetable") }
    var price      by remember { mutableStateOf("") }
    var expiryDays by remember { mutableStateOf("") }
    var showError  by remember { mutableStateOf(false) }

    val units = listOf("units", "kg", "g", "litres")
    val categories = listOf(
        "vegetable", "fruit", "dairy", "meat",
        "chicken", "fish", "grain", "rice",
        "legume", "egg", "tofu", "beef", "pork"
    )

    // ── Auto-fill from scan result ────────────────────────────────────────
    LaunchedEffect(scanResult) {
        scanResult?.let { food ->
            name     = food.name
            category = food.category
            viewModel.clearScanResult()
        }
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Pantry Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenScanner) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan barcode",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Scan banner ───────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                ),
                onClick = onOpenScanner
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "Looking up product...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32)
                        )
                    } else {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32)
                        )
                        Column {
                            Text(
                                text = "Scan a barcode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "Auto-fills name, category & nutrition",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF388E3C)
                            )
                        }
                    }
                }
            }

            // ── Name ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = showError && name.isBlank(),
                supportingText = {
                    if (showError && name.isBlank())
                        Text("Name is required")
                }
            )

            // ── Quantity + Unit ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = showError && quantity.isBlank()
                )
                DropdownField(
                    label    = "Unit",
                    options  = units,
                    selected = unit,
                    onSelected = { unit = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Category ──────────────────────────────────────────────────
            DropdownField(
                label    = "Category",
                options  = categories,
                selected = category,
                onSelected = { category = it },
                modifier = Modifier.fillMaxWidth()
            )

            // ── Purchase Price ────────────────────────────────────────────
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Purchase Price (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = showError && price.isBlank(),
                supportingText = {
                    if (showError && price.isBlank())
                        Text("Price is required")
                }
            )

            // ── Expiry ────────────────────────────────────────────────────
            OutlinedTextField(
                value = expiryDays,
                onValueChange = { expiryDays = it },
                label = { Text("Expires in how many days? *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = showError && expiryDays.isBlank(),
                supportingText = {
                    if (showError && expiryDays.isBlank())
                        Text("Expiry is required")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Save button ───────────────────────────────────────────────
            Button(
                onClick = {
                    if (name.isBlank() || quantity.isBlank() ||
                        price.isBlank() || expiryDays.isBlank()) {
                        showError = true
                        return@Button
                    }
                    val daysFromNow = expiryDays.toIntOrNull() ?: 0
                    val expiryMs = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, daysFromNow)
                    }.timeInMillis

                    viewModel.addItem(
                        PantryItem(
                            name          = name.trim(),
                            quantity      = quantity.toFloatOrNull() ?: 1f,
                            unit          = unit,
                            category      = category,
                            expiryDate    = expiryMs,
                            purchasePrice = price.toFloatOrNull() ?: 0f
                        )
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Add to Pantry", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DropdownField(
        label: String,
        options: List<String>,
        selected: String,
        onSelected: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}