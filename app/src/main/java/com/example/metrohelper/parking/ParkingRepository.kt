package com.example.metrohelper.parking

import android.content.Context

class ParkingRepository(context: Context) {

    private val dao: ParkingFacilityDao =
        AppDatabase.getInstance(context).parkingFacilityDao()

    suspend fun getParkingInfoByName(stationName: String): List<ParkingInfo> {
        val facilities = dao.getParkingByStationName(stationName)
        return facilities.map { facility ->
            ParkingInfo(
                exists = true,
                stationName = facility.station,
                line = facility.line,
                contractorName = facility.contractor,
                contactNumber = facility.contact
            )
        }
    }

    suspend fun getParkingInfoById(stationId: Int): ParkingInfo? {
        val facility = dao.getParkingByStationId(stationId) ?: return null
        return ParkingInfo(
            exists = true,
            stationName = facility.station,
            line = facility.line,
            contractorName = facility.contractor,
            contactNumber = facility.contact
        )
    }

    suspend fun isParkingAvailable(stationName: String): Boolean {
        return dao.isParkingAvailableAtStation(stationName)
    }
}
