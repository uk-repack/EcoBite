package com.example.ecobite.data.repository

import com.example.ecobite.data.local.PantryDao
import com.example.ecobite.data.local.WasteDao
import com.example.ecobite.data.local.entities.PantryItem
import com.example.ecobite.data.local.entities.WasteLog
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryRepository @Inject constructor(
    private val pantryDao: PantryDao,
    private val wasteDao: WasteDao
) {

    // ── Pantry operations ─────────────────────────────────────────────────

    val allPantryItems: Flow<List<PantryItem>> = pantryDao.getAllItems()

    fun getItemById(id: Int): Flow<PantryItem?> =
        pantryDao.getItemById(id)

    fun getItemsExpiringSoon(thresholdDate: Long): Flow<List<PantryItem>> =
        pantryDao.getItemsExpiringSoon(thresholdDate)

    suspend fun insertItem(item: PantryItem) =
        pantryDao.insertItem(item)

    suspend fun updateItem(item: PantryItem) =
        pantryDao.updateItem(item)

    suspend fun deleteItem(item: PantryItem) =
        pantryDao.deleteItem(item)

    suspend fun deleteItemById(id: Int) =
        pantryDao.deleteItemById(id)

    // ── Waste log operations ──────────────────────────────────────────────

    val allWasteLogs: Flow<List<WasteLog>> = wasteDao.getAllWasteLogs()

    fun getWasteLogsFrom(startDate: Long): Flow<List<WasteLog>> =
        wasteDao.getWasteLogsFrom(startDate)

    fun getTotalCostWastedFrom(startDate: Long): Flow<Float?> =
        wasteDao.getTotalCostWastedFrom(startDate)

    fun getTotalCo2From(startDate: Long): Flow<Float?> =
        wasteDao.getTotalCo2From(startDate)

    fun getTotalWaterFrom(startDate: Long): Flow<Float?> =
        wasteDao.getTotalWaterFrom(startDate)

    suspend fun insertWasteLog(log: WasteLog) =
        wasteDao.insertWasteLog(log)

    suspend fun deleteWasteLog(log: WasteLog) =
        wasteDao.deleteWasteLog(log)
}
