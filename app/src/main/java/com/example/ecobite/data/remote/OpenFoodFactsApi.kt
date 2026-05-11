package com.example.ecobite.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {

    @GET("product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OpenFoodFactsResponse
}

// ── Response data classes ─────────────────────────────────────────────────────

data class OpenFoodFactsResponse(
    val status: Int,              // 1 = found, 0 = not found
    val product: Product? = null
)

data class Product(
    val product_name: String? = null,
    val product_name_en: String? = null,
    val categories_tags: List<String>? = null,
    val nutriments: Nutriments? = null,
    val image_url: String? = null
)

data class Nutriments(
    val proteins_100g: Float? = null,
    val carbohydrates_100g: Float? = null,
    val fat_100g: Float? = null,
    val energy_kcal_100g: Float? = null
)