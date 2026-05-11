package com.example.ecobite.data.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodFactsRepository @Inject constructor(
    private val api: OpenFoodFactsApi
) {
    suspend fun getProductInfo(barcode: String): FoodResult {
        return try {
            val response = api.getProductByBarcode(barcode)
            if (response.status == 1 && response.product != null) {
                val product = response.product
                val name = product.product_name
                    ?: product.product_name_en
                    ?: "Unknown Product"

                val category = mapCategory(
                    product.categories_tags ?: emptyList()
                )

                FoodResult.Success(
                    name     = name,
                    category = category,
                    protein  = product.nutriments?.proteins_100g ?: 0f,
                    carbs    = product.nutriments?.carbohydrates_100g ?: 0f,
                    fat      = product.nutriments?.fat_100g ?: 0f
                )
            } else {
                FoodResult.NotFound
            }
        } catch (e: Exception) {
            FoodResult.Error(e.message ?: "Network error")
        }
    }

    private fun mapCategory(tags: List<String>): String {
        val tagString = tags.joinToString(" ").lowercase()
        return when {
            "beef" in tagString || "lamb" in tagString   -> "beef"
            "chicken" in tagString || "poultry" in tagString -> "chicken"
            "pork" in tagString                          -> "pork"
            "fish" in tagString || "seafood" in tagString -> "fish"
            "meat" in tagString                          -> "meat"
            "dairy" in tagString || "milk" in tagString ||
                    "cheese" in tagString || "yogurt" in tagString -> "dairy"
            "egg" in tagString                           -> "egg"
            "rice" in tagString                          -> "rice"
            "bread" in tagString || "grain" in tagString ||
                    "wheat" in tagString || "cereal" in tagString -> "grain"
            "tofu" in tagString || "soy" in tagString    -> "tofu"
            "legume" in tagString || "lentil" in tagString ||
                    "bean" in tagString || "dal" in tagString    -> "legume"
            "fruit" in tagString                         -> "fruit"
            "vegetable" in tagString || "veggie" in tagString -> "vegetable"
            else                                         -> "vegetable"
        }
    }
}

// ── Result sealed class ───────────────────────────────────────────────────────
sealed class FoodResult {
    data class Success(
        val name: String,
        val category: String,
        val protein: Float,
        val carbs: Float,
        val fat: Float
    ) : FoodResult()

    object NotFound : FoodResult()
    data class Error(val message: String) : FoodResult()
}