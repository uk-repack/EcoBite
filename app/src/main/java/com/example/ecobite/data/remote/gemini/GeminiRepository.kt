package com.example.ecobite.data.remote.gemini

import com.example.ecobite.BuildConfig
import com.example.ecobite.data.local.entities.PantryItem
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor(
    private val api: GeminiApi
) {
    suspend fun generateSmartFill(itemName: String): SmartFillResult {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            return SmartFillResult.Error(
                "Add GEMINI_API_KEY to local.properties to enable Smart Fill."
            )
        }

        val cleanedName = itemName.trim()
        if (cleanedName.isBlank()) {
            return SmartFillResult.Error("Enter an item name before using Smart Fill.")
        }

        return try {
            val response = api.generateContent(
                model = GEMINI_MODEL,
                apiKey = apiKey,
                request = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(buildSmartFillPrompt(cleanedName)))
                        )
                    )
                )
            )
            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()

            if (text.isNullOrBlank()) {
                SmartFillResult.Error("Gemini returned an empty Smart Fill result.")
            } else {
                SmartFillResult.Success(parseSmartFill(text))
            }
        } catch (exception: Exception) {
            SmartFillResult.Error(
                exception.message ?: "Could not generate Smart Fill details."
            )
        }
    }

    suspend fun generateSmartWasteReason(
        item: PantryItem,
        quantityWasted: Float?
    ): SmartWasteReasonResult {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            return SmartWasteReasonResult.Error(
                "Add GEMINI_API_KEY to local.properties to enable Smart Reason."
            )
        }

        return try {
            val response = api.generateContent(
                model = GEMINI_MODEL,
                apiKey = apiKey,
                request = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(buildSmartWasteReasonPrompt(item, quantityWasted))
                            )
                        )
                    )
                )
            )
            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()

            if (text.isNullOrBlank()) {
                SmartWasteReasonResult.Error("Gemini returned an empty Smart Reason result.")
            } else {
                SmartWasteReasonResult.Success(parseSmartWasteReason(text))
            }
        } catch (exception: Exception) {
            SmartWasteReasonResult.Error(
                exception.message ?: "Could not generate Smart Reason details."
            )
        }
    }

    suspend fun generateSmartRecipe(items: List<PantryItem>): SmartRecipeResult {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            return SmartRecipeResult.Error(
                "Add GEMINI_API_KEY to local.properties to enable Smart Recipe."
            )
        }

        if (items.isEmpty()) {
            return SmartRecipeResult.Error("Add pantry items before asking for a recipe.")
        }

        return try {
            val response = api.generateContent(
                model = GEMINI_MODEL,
                apiKey = apiKey,
                request = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(buildSmartRecipePrompt(items)))
                        )
                    )
                )
            )
            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()

            if (text.isNullOrBlank()) {
                SmartRecipeResult.Error("Gemini returned an empty Smart Recipe result.")
            } else {
                SmartRecipeResult.Success(parseSmartRecipe(text))
            }
        } catch (exception: Exception) {
            SmartRecipeResult.Error(
                exception.message ?: "Could not generate Smart Recipe."
            )
        }
    }

    suspend fun generatePantrySuggestions(items: List<PantryItem>): GeminiResult {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            return GeminiResult.Error(
                "Add GEMINI_API_KEY to local.properties to enable Smart Pantry."
            )
        }

        if (items.isEmpty()) {
            return GeminiResult.Error("Add pantry items before asking for suggestions.")
        }

        return try {
            val response = api.generateContent(
                model = GEMINI_MODEL,
                apiKey = apiKey,
                request = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(buildPrompt(items)))
                        )
                    )
                )
            )
            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()

            if (text.isNullOrBlank()) {
                GeminiResult.Error("Gemini returned an empty suggestion.")
            } else {
                GeminiResult.Success(parseSmartPantrySuggestion(text))
            }
        } catch (exception: Exception) {
            GeminiResult.Error(
                exception.message ?: "Could not generate Smart Pantry suggestions."
            )
        }
    }

    private fun buildPrompt(items: List<PantryItem>): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val pantryLines = items.joinToString(separator = "\n") { item ->
            "- ${item.name}: ${item.quantity} ${item.unit}, category ${item.category}, expires ${
                dateFormat.format(Date(item.expiryDate))
            }"
        }

        return """
            You are EcoBite's Smart Pantry assistant.
            Use the pantry list below to give practical food-waste reduction suggestions.
            Prioritize items expiring soon, suggest simple Indian-home-friendly meal ideas, and avoid inventing ingredients not in the list unless they are common basics like salt, water, oil, spices, onion, or garlic.
            Keep the full answer under 150 words.
            Use exactly this format:

            USE_FIRST:
            - item and reason
            - item and reason

            RECIPE_IDEAS:
            - meal idea
            - meal idea

            WASTE_SAVER_TIP:
            - one practical storage or prep action

            Pantry:
            $pantryLines
        """.trimIndent()
    }

    private fun buildSmartFillPrompt(itemName: String): String {
        return """
            You are EcoBite's pantry data assistant.
            Suggest pantry entry defaults for this food item: $itemName
            Choose one category only from:
            vegetable, fruit, dairy, meat, chicken, fish, grain, rice, legume, egg, tofu, beef, pork
            Estimate typical refrigerated or normal home-storage expiry days in India.
            Keep storage tip under 18 words.
            Use exactly this format:

            CATEGORY: category
            EXPIRY_DAYS: number
            STORAGE_TIP: short tip
        """.trimIndent()
    }

    private fun buildSmartWasteReasonPrompt(
        item: PantryItem,
        quantityWasted: Float?
    ): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val quantityText = quantityWasted?.let { "$it ${item.unit}" } ?: "not provided"

        return """
            You are EcoBite's food waste coach.
            Suggest the most likely waste reason for this pantry item and a prevention tip.
            Item: ${item.name}
            Category: ${item.category}
            Pantry quantity: ${item.quantity} ${item.unit}
            Quantity wasted: $quantityText
            Expiry date: ${dateFormat.format(Date(item.expiryDate))}
            Purchase price: ₹${item.purchasePrice}

            Choose one reason key only from:
            forgot, too_much, went_bad, disliked

            Keep prevention tip under 18 words and make it specific.
            Use exactly this format:

            REASON_KEY: key
            PREVENTION_TIP: short tip
        """.trimIndent()
    }

    private fun buildSmartRecipePrompt(items: List<PantryItem>): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val pantryLines = items.joinToString(separator = "\n") { item ->
            "- ${item.name}: ${item.quantity} ${item.unit}, category ${item.category}, expires ${
                dateFormat.format(Date(item.expiryDate))
            }"
        }

        return """
            You are EcoBite's low-waste recipe assistant.
            Create one simple Indian-home-friendly recipe using pantry items below.
            Prioritize items that expire soon. Do not require expensive or uncommon ingredients.
            Basic extras like salt, water, oil, spices, onion, garlic, chilli, lemon, or curd are allowed.
            Return only compact valid JSON. Do not use markdown.
            Include exactly 5 concrete cooking steps.
            Keep every string short and specific.
            Use this shape:
            {
              "recipeName": "name",
              "wasteSavingNote": "short note",
              "steps": ["step 1", "step 2", "step 3", "step 4", "step 5"],
              "usesUp": ["pantry item"],
              "pantryIngredients": ["ingredient with quantity"],
              "basicExtras": ["extra"]
            }

            Pantry:
            $pantryLines
        """.trimIndent()
    }

    private fun parseSmartFill(text: String): SmartFillSuggestion {
        val category = extractValue(text, "CATEGORY")
            .lowercase()
            .takeIf { it in SMART_FILL_CATEGORIES }
            ?: "vegetable"
        val expiryDays = extractValue(text, "EXPIRY_DAYS")
            .filter { it.isDigit() }
            .toIntOrNull()
            ?.coerceIn(1, 365)
            ?: 3
        val storageTip = extractValue(text, "STORAGE_TIP")
            .ifBlank { "Store properly and use before it loses freshness." }

        return SmartFillSuggestion(
            category = category,
            expiryDays = expiryDays,
            storageTip = storageTip
        )
    }

    private fun parseSmartWasteReason(text: String): SmartWasteReasonSuggestion {
        val reasonKey = extractValue(text, "REASON_KEY")
            .lowercase()
            .takeIf { it in SMART_WASTE_REASON_KEYS }
            ?: "went_bad"
        val preventionTip = extractValue(text, "PREVENTION_TIP")
            .ifBlank { "Plan a smaller portion next time and check expiry sooner." }

        return SmartWasteReasonSuggestion(
            reasonKey = reasonKey,
            preventionTip = preventionTip
        )
    }

    private fun parseSmartRecipe(text: String): SmartRecipeSuggestion {
        parseSmartRecipeJson(text)?.let { return it }

        val recipeName = extractSingleLineValue(
            text,
            listOf("RECIPE_NAME", "RECIPE NAME", "Recipe Name")
        )
            .ifBlank { "Smart Pantry Recipe" }
        val wasteSavingNote = extractSingleLineValue(
            text,
            listOf("WASTE_SAVING_NOTE", "WASTE SAVING NOTE", "Waste Saving Note")
        )

        return SmartRecipeSuggestion(
            recipeName = recipeName,
            wasteSavingNote = wasteSavingNote,
            usesUp = extractRecipeSection(text, "USES_UP"),
            pantryIngredients = extractRecipeSection(text, "PANTRY_INGREDIENTS"),
            basicExtras = extractRecipeSection(text, "BASIC_EXTRAS"),
            steps = extractRecipeSection(text, "STEPS"),
            rawText = text
        )
    }

    private fun parseSmartRecipeJson(text: String): SmartRecipeSuggestion? {
        return try {
            val jsonText = text
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val jsonObject = JsonParser().parse(jsonText).asJsonObject

            SmartRecipeSuggestion(
                recipeName = jsonObject.stringValue("recipeName")
                    .ifBlank { "Smart Pantry Recipe" },
                wasteSavingNote = jsonObject.stringValue("wasteSavingNote"),
                usesUp = jsonObject.stringList("usesUp"),
                pantryIngredients = jsonObject.stringList("pantryIngredients"),
                basicExtras = jsonObject.stringList("basicExtras"),
                steps = jsonObject.stringList("steps"),
                rawText = text
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun com.google.gson.JsonObject.stringValue(name: String): String {
        return get(name)?.takeIf { !it.isJsonNull }?.asString?.trim().orEmpty()
    }

    private fun com.google.gson.JsonObject.stringList(name: String): List<String> {
        val element = get(name)?.takeIf { !it.isJsonNull } ?: return emptyList()
        if (!element.isJsonArray) return emptyList()

        return element.asJsonArray
            .mapNotNull { item ->
                item.takeIf { it.isJsonPrimitive }
                    ?.asString
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
            }
    }

    private fun extractRecipeSection(text: String, heading: String): List<String> {
        val headingAliases = mapOf(
            "RECIPE_NAME" to listOf("RECIPE_NAME", "RECIPE NAME", "Recipe Name"),
            "WASTE_SAVING_NOTE" to listOf(
                "WASTE_SAVING_NOTE",
                "WASTE SAVING NOTE",
                "Waste Saving Note"
            ),
            "USES_UP" to listOf("USES_UP", "USES UP", "Uses Up"),
            "PANTRY_INGREDIENTS" to listOf(
                "PANTRY_INGREDIENTS",
                "PANTRY INGREDIENTS",
                "Pantry Ingredients"
            ),
            "BASIC_EXTRAS" to listOf(
                "BASIC_EXTRAS",
                "BASIC EXTRAS",
                "Basic Extras"
            ),
            "STEPS" to listOf("STEPS", "Steps", "METHOD", "Method")
        )

        val aliases = headingAliases[heading].orEmpty()
        val startMatch = aliases
            .mapNotNull { alias -> findHeading(text, alias) }
            .minByOrNull { it.first }
        val start = startMatch?.first ?: return emptyList()
        val headingLength = startMatch.second.length

        val contentStart = start + headingLength
        val nextHeadingStart = headingAliases
            .filterKeys { it != heading }
            .values
            .flatten()
            .mapNotNull { alias -> findHeading(text, alias, contentStart)?.first }
            .minOrNull() ?: text.length

        return text.substring(contentStart, nextHeadingStart)
            .lineSequence()
            .map { line ->
                line.trim()
                    .removePrefix("-")
                    .removePrefix("*")
                    .trim()
            }
            .filter { it.isNotBlank() }
            .toList()
    }

    private fun findHeading(
        text: String,
        heading: String,
        startIndex: Int = 0
    ): Pair<Int, String>? {
        val pattern = Regex(
            pattern = "(?im)^\\s*(?:#+\\s*)?(?:\\*\\*)?${Regex.escape(heading)}(?:\\*\\*)?\\s*:",
        )
        val match = pattern.find(text, startIndex) ?: return null
        return match.range.first to match.value
    }

    private fun extractSingleLineValue(text: String, labels: List<String>): String {
        return labels.asSequence()
            .mapNotNull { label -> findHeading(text, label) }
            .minByOrNull { it.first }
            ?.let { match ->
                text.substring(match.first + match.second.length)
                    .lineSequence()
                    .firstOrNull()
                    ?.trim()
            }
            .orEmpty()
    }

    private fun extractSingleLineValue(text: String, label: String): String {
        return extractSingleLineValue(text, listOf(label))
    }

    private fun extractLegacyRecipeSection(text: String, heading: String): List<String> {
        val headings = listOf(
            "RECIPE_NAME",
            "WASTE_SAVING_NOTE",
            "USES_UP",
            "PANTRY_INGREDIENTS",
            "BASIC_EXTRAS",
            "STEPS"
        )
        val start = text.indexOf("$heading:", ignoreCase = true)
        if (start == -1) return emptyList()

        val contentStart = start + heading.length + 1
        val nextHeadingStart = headings
            .filterNot { it.equals(heading, ignoreCase = true) }
            .map { text.indexOf("$it:", startIndex = contentStart, ignoreCase = true) }
            .filter { it != -1 }
            .minOrNull() ?: text.length

        return text.substring(contentStart, nextHeadingStart)
            .lineSequence()
            .map { line ->
                line.trim()
                    .removePrefix("-")
                    .removePrefix("*")
                    .trim()
            }
            .filter { it.isNotBlank() }
            .toList()
    }

    private fun extractValue(text: String, label: String): String {
        return text.lineSequence()
            .firstOrNull { it.trim().startsWith("$label:", ignoreCase = true) }
            ?.substringAfter(":")
            ?.trim()
            .orEmpty()
    }

    private fun parseSmartPantrySuggestion(text: String): SmartPantrySuggestion {
        return SmartPantrySuggestion(
            useFirst = extractSection(text, "USE_FIRST"),
            recipeIdeas = extractSection(text, "RECIPE_IDEAS"),
            wasteSaverTip = extractSection(text, "WASTE_SAVER_TIP"),
            rawText = text
        )
    }

    private fun extractSection(text: String, heading: String): List<String> {
        val headings = listOf("USE_FIRST", "RECIPE_IDEAS", "WASTE_SAVER_TIP")
        val start = text.indexOf("$heading:", ignoreCase = true)
        if (start == -1) return emptyList()

        val contentStart = start + heading.length + 1
        val nextHeadingStart = headings
            .filterNot { it.equals(heading, ignoreCase = true) }
            .map { text.indexOf("$it:", startIndex = contentStart, ignoreCase = true) }
            .filter { it != -1 }
            .minOrNull() ?: text.length

        return text.substring(contentStart, nextHeadingStart)
            .lineSequence()
            .map { line ->
                line.trim()
                    .removePrefix("-")
                    .removePrefix("*")
                    .trim()
            }
            .filter { it.isNotBlank() }
            .toList()
    }

    private companion object {
        const val GEMINI_MODEL = "gemini-2.5-flash"
        val SMART_FILL_CATEGORIES = setOf(
            "vegetable", "fruit", "dairy", "meat", "chicken", "fish",
            "grain", "rice", "legume", "egg", "tofu", "beef", "pork"
        )
        val SMART_WASTE_REASON_KEYS = setOf(
            "forgot", "too_much", "went_bad", "disliked"
        )
    }
}

sealed interface GeminiResult {
    data class Success(val suggestion: SmartPantrySuggestion) : GeminiResult
    data class Error(val message: String) : GeminiResult
}

data class SmartPantrySuggestion(
    val useFirst: List<String>,
    val recipeIdeas: List<String>,
    val wasteSaverTip: List<String>,
    val rawText: String
)

sealed interface SmartFillResult {
    data class Success(val suggestion: SmartFillSuggestion) : SmartFillResult
    data class Error(val message: String) : SmartFillResult
}

data class SmartFillSuggestion(
    val category: String,
    val expiryDays: Int,
    val storageTip: String
)

sealed interface SmartWasteReasonResult {
    data class Success(
        val suggestion: SmartWasteReasonSuggestion
    ) : SmartWasteReasonResult

    data class Error(val message: String) : SmartWasteReasonResult
}

data class SmartWasteReasonSuggestion(
    val reasonKey: String,
    val preventionTip: String
)

sealed interface SmartRecipeResult {
    data class Success(val recipe: SmartRecipeSuggestion) : SmartRecipeResult
    data class Error(val message: String) : SmartRecipeResult
}

data class SmartRecipeSuggestion(
    val recipeName: String,
    val wasteSavingNote: String,
    val usesUp: List<String>,
    val pantryIngredients: List<String>,
    val basicExtras: List<String>,
    val steps: List<String>,
    val rawText: String
)
