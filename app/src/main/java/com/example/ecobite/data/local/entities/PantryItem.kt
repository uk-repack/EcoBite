package com.example.ecobite.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: Float,
    val unit: String,              // "kg", "g", "units", "litres"
    val category: String,          // "vegetable", "fruit", "dairy", "meat", "grain"
    val expiryDate: Long,          // stored as timestamp (milliseconds)
    val purchasePrice: Float,      // in rupees
    val barcode: String? = null,   // nullable — not all items have barcodes
    val protein: Float = 0f,       // grams per 100g — from Open Food Facts later
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val addedDate: Long = System.currentTimeMillis()
)