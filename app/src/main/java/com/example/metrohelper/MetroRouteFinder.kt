package com.example.metrohelper

import androidx.compose.ui.graphics.Color
import java.util.PriorityQueue

data class MetroHop(
    val fromStation: String,
    val toStation: String,
    val line: String,
    val lineColor: Color
)

data class RouteSegment(
    val line: String,
    val lineColor: Color,
    val fromStation: String,
    val toStation: String,
    val intermediateStations: List<String>,
    val stationCount: Int
)

data class RouteInterchange(
    val station: String,
    val fromLine: String,
    val fromLineColor: Color,
    val toLine: String,
    val toLineColor: Color
)

data class PlannedJourney(
    val origin: String,
    val destination: String,
    val segments: List<RouteSegment>,
    val interchanges: List<RouteInterchange>,
    val totalStations: Int,
    val totalDistanceKm: Float,
    val estimatedTimeMins: Int,
    val normalFare: Int,
    val smartCardFare: Int
)

object MetroRouteFinder {

    private data class Edge(
        val to: String,
        val line: String,
        val lineColor: Color
    )

    private val adjacencyList: Map<String, List<Edge>> by lazy {
        val map = mutableMapOf<String, MutableList<Edge>>()

        for (line in MetroNetwork.allLines) {
            val stations = line.stations
            for (i in 0 until stations.size - 1) {
                val s1 = stations[i]
                val s2 = stations[i + 1]

                map.getOrPut(s1) { mutableListOf() }.add(Edge(s2, line.name, line.color))
                map.getOrPut(s2) { mutableListOf() }.add(Edge(s1, line.name, line.color))
            }
        }
        map
    }

    private data class SearchState(
        val currentStation: String,
        val currentLine: String,
        val cost: Float,
        val hops: List<MetroHop>
    ) : Comparable<SearchState> {
        override fun compareTo(other: SearchState): Int = this.cost.compareTo(other.cost)
    }

    fun findRoute(origin: String, destination: String): PlannedJourney? {
        if (origin.isBlank() || destination.isBlank()) return null
        if (origin.equals(destination, ignoreCase = true)) {
            return PlannedJourney(
                origin = origin,
                destination = destination,
                segments = emptyList(),
                interchanges = emptyList(),
                totalStations = 0,
                totalDistanceKm = 0f,
                estimatedTimeMins = 0,
                normalFare = 0,
                smartCardFare = 0
            )
        }

        // Standardize station names
        val start = findExactStationName(origin) ?: return null
        val target = findExactStationName(destination) ?: return null

        val pq = PriorityQueue<SearchState>()
        // Track lowest cost to reach (station, line)
        val visited = mutableMapOf<Pair<String, String>, Float>()

        // Seed with all lines touching the start station
        val initialEdges = adjacencyList[start] ?: return null
        for (edge in initialEdges) {
            val hop = MetroHop(start, edge.to, edge.line, edge.lineColor)
            val state = SearchState(
                currentStation = edge.to,
                currentLine = edge.line,
                cost = 2.1f, // Travel time per station
                hops = listOf(hop)
            )
            pq.add(state)
            visited[Pair(edge.to, edge.line)] = 2.1f
        }

        var bestHops: List<MetroHop>? = null

        while (pq.isNotEmpty()) {
            val current = pq.poll() ?: break

            if (current.currentStation.equals(target, ignoreCase = true)) {
                bestHops = current.hops
                break
            }

            val currentBest = visited[Pair(current.currentStation, current.currentLine)]
            if (currentBest != null && current.cost > currentBest) {
                continue
            }

            val neighbors = adjacencyList[current.currentStation] ?: emptyList()
            for (edge in neighbors) {
                // Avoid immediate u-turn back to previous station
                if (current.hops.isNotEmpty() && edge.to.equals(current.hops.last().fromStation, ignoreCase = true)) {
                    continue
                }

                // If switching lines, add a 5.5 minute interchange transfer penalty
                val isLineSwitch = !edge.line.equals(current.currentLine, ignoreCase = true)
                val edgeCost = if (isLineSwitch) 5.5f + 2.1f else 2.1f
                val newCost = current.cost + edgeCost

                val stateKey = Pair(edge.to, edge.line)
                val existingCost = visited[stateKey]

                if (existingCost == null || newCost < existingCost) {
                    visited[stateKey] = newCost
                    val hop = MetroHop(current.currentStation, edge.to, edge.line, edge.lineColor)
                    pq.add(
                        SearchState(
                            currentStation = edge.to,
                            currentLine = edge.line,
                            cost = newCost,
                            hops = current.hops + hop
                        )
                    )
                }
            }
        }

        val hops = bestHops ?: return null
        return buildJourney(start, target, hops)
    }

    private fun findExactStationName(input: String): String? {
        val trimmed = input.trim()
        // 1. Check exact match in graph
        adjacencyList.keys.firstOrNull { it.equals(trimmed, ignoreCase = true) }?.let { return it }

        // 2. Check contains match
        return adjacencyList.keys.firstOrNull { it.contains(trimmed, ignoreCase = true) }
    }

    private fun buildJourney(origin: String, destination: String, hops: List<MetroHop>): PlannedJourney {
        val segments = mutableListOf<RouteSegment>()
        val interchanges = mutableListOf<RouteInterchange>()

        if (hops.isEmpty()) {
            return PlannedJourney(origin, destination, emptyList(), emptyList(), 0, 0f, 0, 0, 0)
        }

        var currentLine = hops.first().line
        var currentLineColor = hops.first().lineColor
        var segmentStart = hops.first().fromStation
        val currentIntermediate = mutableListOf<String>()

        for (i in hops.indices) {
            val hop = hops[i]
            val isLast = i == hops.size - 1
            val nextHop = if (!isLast) hops[i + 1] else null

            if (nextHop == null || !nextHop.line.equals(currentLine, ignoreCase = true)) {
                // End of this line segment
                segments.add(
                    RouteSegment(
                        line = currentLine,
                        lineColor = currentLineColor,
                        fromStation = segmentStart,
                        toStation = hop.toStation,
                        intermediateStations = currentIntermediate.toList(),
                        stationCount = currentIntermediate.size + 1
                    )
                )

                if (nextHop != null) {
                    // Record interchange
                    interchanges.add(
                        RouteInterchange(
                            station = hop.toStation,
                            fromLine = currentLine,
                            fromLineColor = currentLineColor,
                            toLine = nextHop.line,
                            toLineColor = nextHop.lineColor
                        )
                    )
                    // Reset for next segment
                    currentLine = nextHop.line
                    currentLineColor = nextHop.lineColor
                    segmentStart = nextHop.fromStation
                    currentIntermediate.clear()
                }
            } else {
                currentIntermediate.add(hop.toStation)
            }
        }

        val totalStations = hops.size
        val totalDistanceKm = (totalStations * 1.35f).coerceAtLeast(1.0f)
        val estimatedTimeMins = (totalStations * 2.2f + interchanges.size * 5.0f).toInt().coerceAtLeast(3)

        // DMRC Official Distance Fare Slabs
        val normalFare = when {
            totalDistanceKm <= 2.0f -> 10
            totalDistanceKm <= 5.0f -> 20
            totalDistanceKm <= 12.0f -> 30
            totalDistanceKm <= 21.0f -> 40
            totalDistanceKm <= 32.0f -> 50
            else -> 60
        }
        val smartCardFare = (normalFare * 0.9f).toInt()

        return PlannedJourney(
            origin = origin,
            destination = destination,
            segments = segments,
            interchanges = interchanges,
            totalStations = totalStations,
            totalDistanceKm = totalDistanceKm,
            estimatedTimeMins = estimatedTimeMins,
            normalFare = normalFare,
            smartCardFare = smartCardFare
        )
    }
}

