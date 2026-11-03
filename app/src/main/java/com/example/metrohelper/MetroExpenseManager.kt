package com.example.metrohelper

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Data model representing a logged metro trip expense.
 */
data class MetroExpenseTrip(
    val id: String = UUID.randomUUID().toString(),
    val fromStation: String,
    val toStation: String,
    val fare: Int,
    val fareType: String, // "Smart Card" or "Token / QR"
    val normalFare: Int,  // Full token fare for calculating savings
    val isRoundTrip: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
) {
    val savings: Int
        get() = if (fareType.contains("Smart", ignoreCase = true)) {
            val singleSavings = (normalFare - (normalFare * 9 / 10)).coerceAtLeast(normalFare - (fare / if (isRoundTrip) 2 else 1))
            val diff = (normalFare - (fare / if (isRoundTrip) 2 else 1))
            if (diff > 0) diff * (if (isRoundTrip) 2 else 1) else 0
        } else {
            0
        }
}

/**
 * Summary metrics for a specific month.
 */
data class MonthlyExpenseSummary(
    val year: Int,
    val month: Int, // 1-12
    val monthName: String,
    val totalSpent: Int,
    val tripCount: Int,
    val totalSavings: Int,
    val budget: Int,
    val trips: List<MetroExpenseTrip>
)

object MetroExpenseManager {

    private const val PREFS_NAME = "metro_expense_prefs"
    private const val KEY_TRIPS = "logged_trips_json"
    private const val KEY_BUDGET = "monthly_budget"
    private const val DEFAULT_BUDGET = 2000

    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dateGroupFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDateGroup(timestamp: Long): String {
        val calTrip = Calendar.getInstance().apply { timeInMillis = timestamp }
        val calToday = Calendar.getInstance()
        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            isSameDay(calTrip, calToday) -> "Today, ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))}"
            isSameDay(calTrip, calYesterday) -> "Yesterday, ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))}"
            else -> dateGroupFormat.format(Date(timestamp))
        }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Get all trips stored locally.
     */
    @Synchronized
    fun getAllTrips(context: Context): List<MetroExpenseTrip> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_TRIPS, "[]") ?: "[]"
        val list = mutableListOf<MetroExpenseTrip>()

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    MetroExpenseTrip(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        fromStation = obj.getString("fromStation"),
                        toStation = obj.getString("toStation"),
                        fare = obj.getInt("fare"),
                        fareType = obj.optString("fareType", "Smart Card"),
                        normalFare = obj.optInt("normalFare", obj.getInt("fare")),
                        isRoundTrip = obj.optBoolean("isRoundTrip", false),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        note = obj.optString("note", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return sorted by most recent first
        return list.sortedByDescending { it.timestamp }
    }

    /**
     * Save a new trip to expenses.
     */
    @Synchronized
    fun addTrip(context: Context, trip: MetroExpenseTrip) {
        val currentTrips = getAllTrips(context).toMutableList()
        currentTrips.add(0, trip)
        saveTrips(context, currentTrips)
    }

    /**
     * Delete a trip by ID.
     */
    @Synchronized
    fun deleteTrip(context: Context, tripId: String) {
        val currentTrips = getAllTrips(context).filter { it.id != tripId }
        saveTrips(context, currentTrips)
    }

    /**
     * Clear all trips.
     */
    @Synchronized
    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_TRIPS).apply()
    }

    private fun saveTrips(context: Context, trips: List<MetroExpenseTrip>) {
        val jsonArray = JSONArray()
        for (trip in trips) {
            val obj = JSONObject().apply {
                put("id", trip.id)
                put("fromStation", trip.fromStation)
                put("toStation", trip.toStation)
                put("fare", trip.fare)
                put("fareType", trip.fareType)
                put("normalFare", trip.normalFare)
                put("isRoundTrip", trip.isRoundTrip)
                put("timestamp", trip.timestamp)
                put("note", trip.note)
            }
            jsonArray.put(obj)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TRIPS, jsonArray.toString()).apply()
    }

    /**
     * Get monthly budget.
     */
    fun getMonthlyBudget(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_BUDGET, DEFAULT_BUDGET)
    }

    /**
     * Update monthly budget.
     */
    fun setMonthlyBudget(context: Context, budget: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_BUDGET, budget).apply()
    }

    /**
     * Get summary for a specific year and month (1-based, e.g. 11 for November).
     */
    fun getMonthlySummary(context: Context, year: Int, month: Int): MonthlyExpenseSummary {
        val allTrips = getAllTrips(context)
        val cal = Calendar.getInstance()

        val monthlyTrips = allTrips.filter { trip ->
            cal.timeInMillis = trip.timestamp
            cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month
        }

        val totalSpent = monthlyTrips.sumOf { it.fare }
        val tripCount = monthlyTrips.sumOf { if (it.isRoundTrip) 2 else 1 }
        val totalSavings = monthlyTrips.sumOf { it.savings }
        val budget = getMonthlyBudget(context)

        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        val monthName = monthYearFormat.format(cal.time)

        return MonthlyExpenseSummary(
            year = year,
            month = month,
            monthName = monthName,
            totalSpent = totalSpent,
            tripCount = tripCount,
            totalSavings = totalSavings,
            budget = budget,
            trips = monthlyTrips
        )
    }

    /**
     * Get weekly statistics for Dashboard cards.
     * Returns Pair(tripsThisWeek, savingsThisWeek).
     */
    fun getWeeklyStats(context: Context): Pair<Int, Int> {
        val allTrips = getAllTrips(context)
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)

        val recentTrips = allTrips.filter { it.timestamp >= sevenDaysAgo }
        if (recentTrips.isEmpty()) {
            return Pair(0, 0)
        }

        val count = recentTrips.sumOf { if (it.isRoundTrip) 2 else 1 }
        val savings = recentTrips.sumOf { it.savings }
        return Pair(count, savings)
    }

    /**
     * Get top frequent routes for 1-tap quick logging.
     */
    fun getFrequentRoutes(context: Context): List<Pair<String, String>> {
        val trips = getAllTrips(context)
        if (trips.isEmpty()) return emptyList()

        return trips
            .groupBy { it.fromStation to it.toStation }
            .entries
            .sortedByDescending { it.value.size }
            .take(3)
            .map { it.key }
    }
}

