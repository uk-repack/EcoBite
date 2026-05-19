package com.example.ecobite.ui.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.example.ecobite.data.remote.gemini.SmartPantrySuggestion
import com.example.ecobite.data.remote.gemini.SmartRecipeSuggestion
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
    val smartPantryState by viewModel.smartPantryState.collectAsState()
    val smartRecipeState by viewModel.smartRecipeState.collectAsState()
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
                item {
                    SmartPantryCard(
                        isLoading = smartPantryState.isLoading,
                        suggestion = smartPantryState.suggestion,
                        onGenerate = viewModel::generateSmartPantrySuggestions
                    )
                }
                item {
                    SmartRecipeCard(
                        isLoading = smartRecipeState.isLoading,
                        recipe = smartRecipeState.recipe,
                        onGenerate = viewModel::generateSmartRecipe
                    )
                }
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
fun SmartRecipeCard(
    isLoading: Boolean,
    recipe: SmartRecipeSuggestion?,
    onGenerate: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Recipe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Cook with what you already have",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onGenerate,
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Recipe")
                    }
                }
            }

            recipe?.let {
                SmartRecipeContent(
                    recipe = it,
                    isExpanded = isExpanded,
                    onToggleExpanded = { isExpanded = !isExpanded }
                )
            }
        }
    }
}

@Composable
fun SmartRecipeContent(
    recipe: SmartRecipeSuggestion,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val hasStructuredContent = recipe.usesUp.isNotEmpty() ||
            recipe.pantryIngredients.isNotEmpty() ||
            recipe.basicExtras.isNotEmpty() ||
            recipe.steps.isNotEmpty()

    if (!hasStructuredContent) {
        TextButton(
            onClick = onToggleExpanded,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Text(if (isExpanded) "Hide Recipe" else "View Recipe")
        }
        if (isExpanded) {
            Text(
                text = recipe.rawText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = recipe.recipeName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (recipe.usesUp.isNotEmpty()) {
            Text(
                text = "Uses up: ${recipe.usesUp.joinToString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (recipe.wasteSavingNote.isNotBlank()) {
            Text(
                text = recipe.wasteSavingNote,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(
            onClick = onToggleExpanded,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Text(if (isExpanded) "Hide Recipe" else "View Recipe")
        }

        if (!isExpanded) return@Column

        SmartRecipeSection("Steps", recipe.steps)
        SmartRecipeSection("Uses Up", recipe.usesUp)
        SmartRecipeSection("Pantry Ingredients", recipe.pantryIngredients)
        SmartRecipeSection("Basic Extras", recipe.basicExtras)
    }
}

@Composable
fun SmartRecipeSection(
    title: String,
    items: List<String>
) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        items.forEachIndexed { index, item ->
            val prefix = if (title == "Steps") "${index + 1}. " else "- "
            Text(
                text = "$prefix$item",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SmartPantryCard(
    isLoading: Boolean,
    suggestion: SmartPantrySuggestion?,
    onGenerate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Pantry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Get ideas from what expires soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Button(
                    onClick = onGenerate,
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Ask")
                    }
                }
            }

            suggestion?.let {
                SmartPantrySuggestionContent(suggestion = it)
            }
        }
    }
}

@Composable
fun SmartPantrySuggestionContent(
    suggestion: SmartPantrySuggestion
) {
    val hasStructuredContent = suggestion.useFirst.isNotEmpty() ||
            suggestion.recipeIdeas.isNotEmpty() ||
            suggestion.wasteSaverTip.isNotEmpty()

    if (!hasStructuredContent) {
        Text(
            text = suggestion.rawText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SmartPantrySection(
            title = "Use First",
            items = suggestion.useFirst
        )
        SmartPantrySection(
            title = "Recipe Ideas",
            items = suggestion.recipeIdeas
        )
        SmartPantrySection(
            title = "Waste Saver Tip",
            items = suggestion.wasteSaverTip
        )
    }
}

@Composable
fun SmartPantrySection(
    title: String,
    items: List<String>
) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        items.forEach { item ->
            Text(
                text = "- $item",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
