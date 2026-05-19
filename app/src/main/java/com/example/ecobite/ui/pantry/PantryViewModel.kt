package com.example.ecobite.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecobite.data.local.entities.PantryItem
import com.example.ecobite.data.local.entities.WasteLog
import com.example.ecobite.data.remote.FoodFactsRepository
import com.example.ecobite.data.remote.FoodResult
import com.example.ecobite.data.remote.gemini.GeminiRepository
import com.example.ecobite.data.remote.gemini.GeminiResult
import com.example.ecobite.data.remote.gemini.SmartFillResult
import com.example.ecobite.data.remote.gemini.SmartFillSuggestion
import com.example.ecobite.data.remote.gemini.SmartPantrySuggestion
import com.example.ecobite.data.remote.gemini.SmartRecipeResult
import com.example.ecobite.data.remote.gemini.SmartRecipeSuggestion
import com.example.ecobite.data.remote.gemini.SmartWasteReasonResult
import com.example.ecobite.data.remote.gemini.SmartWasteReasonSuggestion
import com.example.ecobite.data.repository.PantryRepository
import com.example.ecobite.domain.calculator.Co2Calculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.example.ecobite.worker.NotificationScheduler

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val repository: PantryRepository,
    private val foodFactsRepository: FoodFactsRepository,
    private val geminiRepository: GeminiRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    // ── Pantry state ──────────────────────────────────────────────────────

    val allPantryItems: StateFlow<List<PantryItem>> = repository
        .allPantryItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expiringItems: StateFlow<List<PantryItem>> = repository
        .getItemsExpiringSoon(
            thresholdDate = System.currentTimeMillis() +
                    TimeUnit.HOURS.toMillis(48)
        )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ── Waste log state ───────────────────────────────────────────────────

    val allWasteLogs: StateFlow<List<WasteLog>> = repository
        .allWasteLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val weekStartMs =
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

    val weeklyTotalCost: StateFlow<Float> =
        MutableStateFlow(0f).also { mutable ->
            viewModelScope.launch {
                repository.getTotalCostWastedFrom(weekStartMs)
                    .collect { value -> mutable.value = value ?: 0f }
            }
        }

    val weeklyTotalCo2: StateFlow<Float> =
        MutableStateFlow(0f).also { mutable ->
            viewModelScope.launch {
                repository.getTotalCo2From(weekStartMs)
                    .collect { value -> mutable.value = value ?: 0f }
            }
        }

    val weeklyTotalWater: StateFlow<Float> =
        MutableStateFlow(0f).also { mutable ->
            viewModelScope.launch {
                repository.getTotalWaterFrom(weekStartMs)
                    .collect { value -> mutable.value = value ?: 0f }
            }
        }

    // ── UI message state ──────────────────────────────────────────────────

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // ── Barcode scan state ────────────────────────────────────────────────

    private val _scanResult = MutableStateFlow<ScannedFood?>(null)
    val scanResult: StateFlow<ScannedFood?> = _scanResult.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // ── Smart Pantry state ────────────────────────────────────────────────

    private val _smartPantryState = MutableStateFlow(SmartPantryUiState())
    val smartPantryState: StateFlow<SmartPantryUiState> =
        _smartPantryState.asStateFlow()

    private val _smartFillState = MutableStateFlow(SmartFillUiState())
    val smartFillState: StateFlow<SmartFillUiState> =
        _smartFillState.asStateFlow()

    private val _smartWasteReasonState =
        MutableStateFlow(SmartWasteReasonUiState())
    val smartWasteReasonState: StateFlow<SmartWasteReasonUiState> =
        _smartWasteReasonState.asStateFlow()

    private val _smartRecipeState = MutableStateFlow(SmartRecipeUiState())
    val smartRecipeState: StateFlow<SmartRecipeUiState> =
        _smartRecipeState.asStateFlow()

    // ── Pantry actions ────────────────────────────────────────────────────

    fun addItem(item: PantryItem) {
        viewModelScope.launch {
            repository.insertItem(item)
            notificationScheduler.scheduleExpiryNotification(item)
            _uiMessage.value = "${item.name} added to pantry"
        }
    }

    fun updateItem(item: PantryItem) {
        viewModelScope.launch {
            repository.updateItem(item)
            _uiMessage.value = "${item.name} updated"
        }
    }

    fun deleteItem(item: PantryItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            notificationScheduler.cancelNotification(item.id)
            _uiMessage.value = "${item.name} removed from pantry"
        }
    }

    fun markRecipeCooked(usedItems: List<PantryItem>) {
        if (usedItems.isEmpty()) {
            _uiMessage.value = "No matching pantry items were found."
            return
        }

        viewModelScope.launch {
            usedItems.forEach { item ->
                repository.deleteItem(item)
                notificationScheduler.cancelNotification(item.id)
            }
            _uiMessage.value = "Cooked recipe: removed ${
                usedItems.joinToString { it.name }
            } from pantry"
        }
    }

    // ── Barcode lookup ────────────────────────────────────────────────────

    fun lookupBarcode(barcode: String) {
        viewModelScope.launch {
            _isScanning.value = true
            when (val result = foodFactsRepository.getProductInfo(barcode)) {
                is FoodResult.Success -> {
                    _scanResult.value = ScannedFood(
                        name     = result.name,
                        category = result.category,
                        protein  = result.protein,
                        carbs    = result.carbs,
                        fat      = result.fat,
                        barcode  = barcode
                    )
                    _uiMessage.value = "Found: ${result.name}"
                }
                is FoodResult.NotFound -> {
                    _scanResult.value = null
                    _uiMessage.value =
                        "Product not found — please enter details manually"
                }
                is FoodResult.Error -> {
                    _scanResult.value = null
                    _uiMessage.value = "Network error — please try again"
                }
            }
            _isScanning.value = false
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
    }

    // ── Smart Pantry actions ──────────────────────────────────────────────

    fun generateSmartPantrySuggestions() {
        viewModelScope.launch {
            _smartPantryState.value = SmartPantryUiState(isLoading = true)
            when (
                val result = geminiRepository.generatePantrySuggestions(
                    allPantryItems.value
                )
            ) {
                is GeminiResult.Success -> {
                    _smartPantryState.value = SmartPantryUiState(
                        suggestion = result.suggestion
                    )
                }
                is GeminiResult.Error -> {
                    _smartPantryState.value = SmartPantryUiState(
                        errorMessage = result.message
                    )
                    _uiMessage.value = result.message
                }
            }
        }
    }

    fun generateSmartRecipe() {
        viewModelScope.launch {
            _smartRecipeState.value = SmartRecipeUiState(isLoading = true)
            when (
                val result = geminiRepository.generateSmartRecipe(
                    allPantryItems.value
                )
            ) {
                is SmartRecipeResult.Success -> {
                    _smartRecipeState.value = SmartRecipeUiState(
                        recipe = result.recipe
                    )
                }
                is SmartRecipeResult.Error -> {
                    _smartRecipeState.value = SmartRecipeUiState(
                        errorMessage = result.message
                    )
                    _uiMessage.value = result.message
                }
            }
        }
    }

    fun generateSmartFill(itemName: String) {
        viewModelScope.launch {
            _smartFillState.value = SmartFillUiState(isLoading = true)
            when (val result = geminiRepository.generateSmartFill(itemName)) {
                is SmartFillResult.Success -> {
                    _smartFillState.value = SmartFillUiState(
                        suggestion = result.suggestion
                    )
                    _uiMessage.value = "Smart Fill applied"
                }
                is SmartFillResult.Error -> {
                    _smartFillState.value = SmartFillUiState(
                        errorMessage = result.message
                    )
                    _uiMessage.value = result.message
                }
            }
        }
    }

    fun clearSmartFillSuggestion() {
        _smartFillState.value = _smartFillState.value.copy(suggestion = null)
    }

    // ── Waste log actions ─────────────────────────────────────────────────

    fun generateSmartWasteReason(
        pantryItem: PantryItem?,
        quantityWasted: String
    ) {
        if (pantryItem == null) {
            _uiMessage.value = "Select a pantry item before using Smart Reason."
            return
        }

        viewModelScope.launch {
            _smartWasteReasonState.value = SmartWasteReasonUiState(isLoading = true)
            when (
                val result = geminiRepository.generateSmartWasteReason(
                    item = pantryItem,
                    quantityWasted = quantityWasted.toFloatOrNull()
                )
            ) {
                is SmartWasteReasonResult.Success -> {
                    _smartWasteReasonState.value = SmartWasteReasonUiState(
                        suggestion = result.suggestion
                    )
                    _uiMessage.value = "Smart Reason applied"
                }
                is SmartWasteReasonResult.Error -> {
                    _smartWasteReasonState.value = SmartWasteReasonUiState(
                        errorMessage = result.message
                    )
                    _uiMessage.value = result.message
                }
            }
        }
    }

    fun clearSmartWasteReasonSuggestion() {
        _smartWasteReasonState.value =
            _smartWasteReasonState.value.copy(suggestion = null)
    }

    fun logWaste(
        pantryItem: PantryItem,
        quantityWasted: Float,
        reasonTag: String
    ) {
        viewModelScope.launch {
            val co2 = Co2Calculator.calculateCo2(
                category = pantryItem.category,
                quantity = quantityWasted,
                unit     = pantryItem.unit
            )
            val water = Co2Calculator.calculateWater(
                category = pantryItem.category,
                quantity = quantityWasted,
                unit     = pantryItem.unit
            )
            val costWasted = if (pantryItem.quantity > 0) {
                (quantityWasted / pantryItem.quantity) * pantryItem.purchasePrice
            } else 0f

            val wasteLog = WasteLog(
                pantryItemId   = pantryItem.id,
                itemName       = pantryItem.name,
                quantityWasted = quantityWasted,
                unit           = pantryItem.unit,
                category       = pantryItem.category,
                reasonTag      = reasonTag,
                costWasted     = costWasted,
                co2Kg          = co2,
                waterLitres    = water
            )
            repository.insertWasteLog(wasteLog)
            _uiMessage.value =
                "Waste logged — equivalent to ${Co2Calculator.co2Equivalency(co2)}"
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}

// ── ScannedFood data class ────────────────────────────────────────────────────
data class ScannedFood(
    val name: String,
    val category: String,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val barcode: String
)

data class SmartPantryUiState(
    val isLoading: Boolean = false,
    val suggestion: SmartPantrySuggestion? = null,
    val errorMessage: String? = null
)

data class SmartFillUiState(
    val isLoading: Boolean = false,
    val suggestion: SmartFillSuggestion? = null,
    val errorMessage: String? = null
)

data class SmartWasteReasonUiState(
    val isLoading: Boolean = false,
    val suggestion: SmartWasteReasonSuggestion? = null,
    val errorMessage: String? = null
)

data class SmartRecipeUiState(
    val isLoading: Boolean = false,
    val recipe: SmartRecipeSuggestion? = null,
    val errorMessage: String? = null
)
