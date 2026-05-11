package com.example.ecobite.data.local

import androidx.room.*
import com.example.ecobite.data.local.entities.PantryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PantryItem)

    @Update
    suspend fun updateItem(item: PantryItem)

    @Delete
    suspend fun deleteItem(item: PantryItem)

    @Query("SELECT * FROM pantry_items ORDER BY expiryDate ASC")
    fun getAllItems(): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    fun getItemById(id: Int): Flow<PantryItem?>

    @Query("SELECT * FROM pantry_items WHERE expiryDate <= :thresholdDate ORDER BY expiryDate ASC")
    fun getItemsExpiringSoon(thresholdDate: Long): Flow<List<PantryItem>>

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)
}