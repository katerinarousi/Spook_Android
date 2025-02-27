package com.example.room_setup_composables

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Insert
    suspend fun insert(booking: Booking)

    @Delete
    suspend fun delete(booking: Booking)

    @Query("SELECT * FROM booking_table")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("DELETE FROM booking_table")
    fun deleteAllBookings()

    @Query("SELECT * FROM booking_table WHERE userId = :userId")
    fun getBookingsForUser(userId: Int): Flow<List<Booking>>
}