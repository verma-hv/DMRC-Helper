package com.example.metrohelper.parking

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_facilities")
data class ParkingFacility(
    @PrimaryKey
    val id: Int,
    val line: String,
    val station: String,
    val contractor: String,
    val contact: String
)
