package com.example.ecobite.data.local

import androidx.room.*
import com.example.ecobite.data.local.entities.WasteLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWasteLog(log: WasteLog)

    @Delete
    suspend fun deleteWasteLog(log: WasteLog)

    @Query("SELECT * FROM waste_logs ORDER BY dateWasted DESC")
    fun getAllWasteLogs(): Flow<List<WasteLog>>

    @Query("SELECT * FROM waste_logs WHERE dateWasted >= :startDate ORDER BY dateWasted DESC")
    fun getWasteLogsFrom(startDate: Long): Flow<List<WasteLog>>

    @Query("SELECT SUM(costWasted) FROM waste_logs WHERE dateWasted >= :startDate")
    fun getTotalCostWastedFrom(startDate: Long): Flow<Float?>

    @Query("SELECT SUM(co2Kg) FROM waste_logs WHERE dateWasted >= :startDate")
    fun getTotalCo2From(startDate: Long): Flow<Float?>

    @Query("SELECT SUM(waterLitres) FROM waste_logs WHERE dateWasted >= :startDate")
    fun getTotalWaterFrom(startDate: Long): Flow<Float?>
}
