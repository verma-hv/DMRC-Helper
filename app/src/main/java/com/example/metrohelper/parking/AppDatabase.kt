package com.example.metrohelper.parking

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

@Database(entities = [ParkingFacility::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun parkingFacilityDao(): ParkingFacilityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            val appContext = context.applicationContext
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                "dmrc_parking_db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(appContext).let { database ->
                                seedDatabase(appContext, database.parkingFacilityDao())
                            }
                        }
                    }
                })
                .build()
        }

        private suspend fun seedDatabase(context: Context, dao: ParkingFacilityDao) {
            try {
                val jsonString = context.assets
                    .open("delhi_metro_parking_clean.json")
                    .bufferedReader()
                    .use { it.readText() }

                val jsonArray = JSONArray(jsonString)
                val facilities = mutableListOf<ParkingFacility>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    facilities.add(
                        ParkingFacility(
                            id = obj.getInt("id"),
                            line = obj.getString("line"),
                            station = obj.getString("station"),
                            contractor = obj.getString("contractor"),
                            contact = obj.getString("contact")
                        )
                    )
                }

                dao.insertAll(facilities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
