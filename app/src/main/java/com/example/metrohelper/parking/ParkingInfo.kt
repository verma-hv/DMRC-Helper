package com.example.metrohelper.parking

data class ParkingInfo(
    val exists: Boolean,
    val stationName: String,
    val line: String,
    val contractorName: String,
    val contactNumber: String
)
