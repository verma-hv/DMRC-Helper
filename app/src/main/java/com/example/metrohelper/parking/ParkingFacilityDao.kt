package com.example.metrohelper.parking

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ParkingFacilityDao {

    @Query("SELECT * FROM parking_facilities WHERE LOWER(station) LIKE '%' || LOWER(:stationName) || '%'")
    suspend fun getParkingByStationName(stationName: String): List<ParkingFacility>

    @Query("SELECT * FROM parking_facilities WHERE id = :stationId")
    suspend fun getParkingByStationId(stationId: Int): ParkingFacility?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(facilities: List<ParkingFacility>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(facility: ParkingFacility)

    @Query("SELECT COUNT(*) > 0 FROM parking_facilities WHERE LOWER(station) LIKE '%' || LOWER(:stationName) || '%'")
    suspend fun isParkingAvailableAtStation(stationName: String): Boolean

    @Query("SELECT COUNT(*) FROM parking_facilities")
    suspend fun count(): Int
}
