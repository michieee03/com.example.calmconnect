package com.example.calmconnect.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.calmconnect.db.entity.Quote

@Dao
interface QuoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<Quote>)

    @Update
    suspend fun update(quote: Quote)

    @Query("SELECT * FROM quotes")
    suspend fun getAll(): List<Quote>

    @Query("SELECT * FROM quotes WHERE isFavorite = 1")
    fun getFavorites(): LiveData<List<Quote>>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getById(id: Int): Quote?

    @Query("SELECT * FROM quotes WHERE text LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    suspend fun searchByTextOrAuthor(query: String): List<Quote>
}
