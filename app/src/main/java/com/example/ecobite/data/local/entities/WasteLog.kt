package com.example.ecobite.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waste_logs")
data class WasteLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pantryItemId: Int,         // which item was wasted
    val itemName: String,          // store name directly — item may be deleted later
    val quantityWasted: Float,
    val unit: String,
    val category: String,
    val reasonTag: String,         // "forgot", "too_much", "went_bad", "disliked"
    val costWasted: Float,         // rupees
    val co2Kg: Float,              // kg of CO2 equivalent
    val waterLitres: Float,        // litres of water wasted
    val dateWasted: Long = System.currentTimeMillis()
)

