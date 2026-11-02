package com.example.metrohelper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

enum class CabProvider(val displayName: String, val tagline: String, val primaryColor: Color, val textColor: Color) {
    UBER("Uber", "Auto, Go & Premier", Color(0xFF000000), Color.White),
    OLA("Ola", "Auto, Mini & Prime", Color(0xFF0D47A1), Color.White),
    RAPIDO("Rapido", "Bike & Auto Taxi", Color(0xFFFFB300), Color(0xFF1E1E1E)),
    MAPS("Google Maps", "Live Turn-by-Turn", Color(0xFF34A853), Color.White)
}

data class CabFareEstimate(
    val category: String,
    val iconEmoji: String,
    val estimatedFare: String,
    val etaMins: Int,
    val description: String
)

fun calculateEstimatedFares(distanceKm: Float): List<CabFareEstimate> {
    val dist = distanceKm.coerceAtLeast(0.5f)
    val autoBase = 30
    val autoFare = (autoBase + dist * 11).toInt().coerceAtLeast(35)

    val bikeBase = 20
    val bikeFare = (bikeBase + dist * 7).toInt().coerceAtLeast(25)

    val cabBase = 50
    val cabFare = (cabBase + dist * 14).toInt().coerceAtLeast(60)

    val sedanBase = 70
    val sedanFare = (sedanBase + dist * 18).toInt().coerceAtLeast(80)

    val drivingEta = (dist * 3.2).toInt().coerceAtLeast(3)

    return listOf(
        CabFareEstimate("Auto", "🛺", "₹$autoFare–${autoFare + 15}", drivingEta, "Quickest in traffic"),
        CabFareEstimate("Bike", "🏍️", "₹$bikeFare–${bikeFare + 10}", (drivingEta * 0.85).toInt().coerceAtLeast(2), "Best for solo riders"),
        CabFareEstimate("Cab Mini", "🚕", "₹$cabFare–${cabFare + 20}", drivingEta, "AC compact cab"),
        CabFareEstimate("Sedan", "🚘", "₹$sedanFare–${sedanFare + 30}", drivingEta, "Comfort & spacious")
    )
}

fun launchCabApp(
    context: Context,
    provider: CabProvider,
    pickupLat: Double,
    pickupLon: Double,
    dropLat: Double,
    dropLon: Double,
    stationName: String
) {
    val dropNickname = Uri.encode("$stationName Metro Station")
    val pickupNickname = Uri.encode("My Current Location")

    val (appUri, fallbackUrl) = when (provider) {
        CabProvider.UBER -> Pair(
            "uber://?action=setPickup&pickup[latitude]=$pickupLat&pickup[longitude]=$pickupLon&pickup[nickname]=$pickupNickname&dropoff[latitude]=$dropLat&dropoff[longitude]=$dropLon&dropoff[nickname]=$dropNickname",
            "https://m.uber.com/ul/?action=setPickup&pickup[latitude]=$pickupLat&pickup[longitude]=$pickupLon&dropoff[latitude]=$dropLat&dropoff[longitude]=$dropLon&dropoff[nickname]=$dropNickname"
        )
        CabProvider.OLA -> Pair(
            "olacabs://app/launch?lat=$pickupLat&lng=$pickupLon&drop_lat=$dropLat&drop_lng=$dropLon&drop_name=$dropNickname",
            "https://book.olacabs.com/?pickup_lat=$pickupLat&pickup_lng=$pickupLon&drop_lat=$dropLat&drop_lng=$dropLon"
        )
        CabProvider.RAPIDO -> Pair(
            "rapido://ride?pickup_lat=$pickupLat&pickup_lng=$pickupLon&drop_lat=$dropLat&drop_lng=$dropLon",
            "https://www.rapido.bike/"
        )
        CabProvider.MAPS -> Pair(
            "google.navigation:q=$dropLat,$dropLon&mode=d",
            "https://www.google.com/maps/dir/?api=1&origin=$pickupLat,$pickupLon&destination=$dropLat,$dropLon&travelmode=driving"
        )
    }

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appUri)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // App not installed, open fallback URL in browser
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Could not open ${provider.displayName}", Toast.LENGTH_SHORT).show()
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BookCabScreen(
    navController: NavHostController,
    initialStationName: String? = null
) {
    val context = LocalContext.current
    val activity = context as Activity

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedStation by remember { mutableStateOf<MetroStation?>(null) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var isLoadingLocation by remember { mutableStateOf(true) }
    var isLoadingRoute by remember { mutableStateOf(false) }
    var showStationSelector by remember { mutableStateOf(false) }

    val allStations = remember { loadMetroStations(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 1. Fetch User Location
    LaunchedEffect(Unit) {
        checkLocationEnabled(activity) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                isLoadingLocation = false
                location?.let {
                    val userPt = GeoPoint(it.latitude, it.longitude)
                    userLocation = userPt

                    // Target station: either matching initialStationName or nearest station
                    val targetStation = if (!initialStationName.isNullOrBlank()) {
                        allStations.find { s -> s.name.equals(initialStationName, ignoreCase = true) }
                            ?: findNearestStation(it.latitude, it.longitude, allStations)
                    } else {
                        findNearestStation(it.latitude, it.longitude, allStations)
                    }
                    selectedStation = targetStation
                }
            }.addOnFailureListener {
                isLoadingLocation = false
            }
        }
    }

    // 2. Fetch OSRM Driving Route
    LaunchedEffect(userLocation, selectedStation) {
        val user = userLocation
        val station = selectedStation
        if (user != null && station != null) {
            isLoadingRoute = true
            try {
                routePoints = getRoute(
                    user.latitude,
                    user.longitude,
                    station.lat,
                    station.lon
                )
            } catch (e: Exception) {
                routePoints = emptyList()
            } finally {
                isLoadingRoute = false
            }
        }
    }

    // 3. Center and Zoom Map to accommodate both user location and station
    LaunchedEffect(userLocation, selectedStation, mapViewRef) {
        val user = userLocation
        val station = selectedStation
        val map = mapViewRef
        if (user != null && station != null && map != null) {
            map.post {
                try {
                    val minLat = minOf(user.latitude, station.lat)
                    val maxLat = maxOf(user.latitude, station.lat)
                    val minLon = minOf(user.longitude, station.lon)
                    val maxLon = maxOf(user.longitude, station.lon)

                    val latMargin = ((maxLat - minLat) * 0.35).coerceAtLeast(0.005)
                    val lonMargin = ((maxLon - minLon) * 0.35).coerceAtLeast(0.005)

                    val box = BoundingBox(
                        maxLat + latMargin,
                        maxLon + lonMargin,
                        minLat - latMargin,
                        minLon - lonMargin
                    )
                    map.zoomToBoundingBox(box, true)
                } catch (e: Exception) {
                    map.controller.setZoom(15.0)
                    map.controller.animateTo(user)
                }
                map.invalidate()
            }
        }
    }

    val distanceKm = if (userLocation != null && selectedStation != null) {
        calculateDistance(
            userLocation!!.latitude,
            userLocation!!.longitude,
            selectedStation!!.lat,
            selectedStation!!.lon
        )
    } else {
        0f
    }

    val estimatedDrivingTime = (distanceKm * 3.2).toInt().coerceAtLeast(3)
    val fares = remember(distanceKm) { calculateEstimatedFares(distanceKm) }

    Box(modifier = Modifier.fillMaxSize()) {
        // =====================================
        // OSM MAP
        // =====================================
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewRef = this
                    val mapnikTileSource = XYTileSource(
                        "OpenStreetMap",
                        0,
                        19,
                        256,
                        ".png",
                        arrayOf("https://tile.openstreetmap.org/")
                    )
                    setTileSource(mapnikTileSource)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                // Pickup Marker (User Location)
                userLocation?.let { userPt ->
                    val userMarker = Marker(mapView)
                    userMarker.position = userPt
                    userMarker.title = "Pickup: My Location"
                    userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(userMarker)
                }

                // Dropoff Marker (Nearest/Selected Metro Station)
                selectedStation?.let { st ->
                    val stationPt = GeoPoint(st.lat, st.lon)
                    val stationMarker = Marker(mapView)
                    stationMarker.position = stationPt
                    stationMarker.title = "Dropoff: ${st.name} Metro"
                    stationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(stationMarker)
                }

                // Driving Route Polyline
                if (routePoints.isNotEmpty()) {
                    val routeBorder = Polyline().apply {
                        setPoints(routePoints)
                        outlinePaint.color = android.graphics.Color.WHITE
                        outlinePaint.strokeWidth = 14f
                    }
                    mapView.overlays.add(routeBorder)

                    val routeLine = Polyline().apply {
                        setPoints(routePoints)
                        outlinePaint.color = android.graphics.Color.rgb(211, 47, 47) // DMRC Crimson Red
                        outlinePaint.strokeWidth = 8f
                    }
                    mapView.overlays.add(routeLine)
                }

                mapView.invalidate()
            }
        )

        // OSM Attribution
        Text(
            text = "© OpenStreetMap contributors",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 300.dp),
            fontSize = 10.sp,
            color = Color.DarkGray
        )

        // =====================================
        // FLOATING MAP ZOOM & LOCATE BUTTONS
        // =====================================
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp)
        ) {
            FloatingMapButton(icon = Icons.Default.Add) {
                mapViewRef?.controller?.zoomIn()
            }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingMapButton(icon = Icons.Default.Remove) {
                mapViewRef?.controller?.zoomOut()
            }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingMapButton(icon = Icons.Default.MyLocation) {
                userLocation?.let { pt ->
                    mapViewRef?.controller?.animateTo(pt)
                    mapViewRef?.controller?.setZoom(16.5)
                }
            }
        }

        // =====================================
        // TOP FLOATING HEADER CARD
        // =====================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.96f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🚕 Book Cab to Metro",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                    Text(
                        text = if (selectedStation != null) "Dropoff: ${selectedStation!!.name} Metro" else "Detecting nearest station…",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        maxLines = 1
                    )
                }

                // Change station button
                OutlinedButton(
                    onClick = { showStationSelector = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Change", fontSize = 12.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
            }
        }

        // =====================================
        // BOTTOM SHEET / CAB BOOKING CARD
        // =====================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Drag handle bar indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE0E0E0))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Station Name & Distance Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Subway,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedStation?.name ?: "Nearest Metro Station",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            maxLines = 1
                        )
                        Text(
                            text = "Origin → Nearest Metro Station",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    // Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoChip("📍 %.1f km".format(distanceKm))
                        InfoChip("⏱ ~$estimatedDrivingTime min")
                    }
                }

                // If user is within walking distance (< 500m)
                if (distanceKm > 0 && distanceKm < 0.5f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🚶", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Station is very close! Walkable in ~${(distanceKm * 12).toInt().coerceAtLeast(2)} mins",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fare Comparison Carousel
                Text(
                    text = "ESTIMATED CAB & AUTO FARES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(fares) { fare ->
                        Card(
                            modifier = Modifier.width(108.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = fare.iconEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = fare.category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF212121)
                                )
                                Text(
                                    text = fare.estimatedFare,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD32F2F)
                                )
                                Text(
                                    text = "~${fare.etaMins}m",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // One-Tap Booking Options Header
                Text(
                    text = "CHOOSE APP TO BOOK RIDE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Provider buttons grid (Uber, Ola, Rapido)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Uber Button
                    Button(
                        onClick = {
                            val user = userLocation
                            val st = selectedStation
                            if (user != null && st != null) {
                                launchCabApp(context, CabProvider.UBER, user.latitude, user.longitude, st.lat, st.lon, st.name)
                            } else {
                                Toast.makeText(context, "Location not ready yet", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000)),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Uber", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }

                    // Ola Button
                    Button(
                        onClick = {
                            val user = userLocation
                            val st = selectedStation
                            if (user != null && st != null) {
                                launchCabApp(context, CabProvider.OLA, user.latitude, user.longitude, st.lat, st.lon, st.name)
                            } else {
                                Toast.makeText(context, "Location not ready yet", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Ola", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }

                    // Rapido Button
                    Button(
                        onClick = {
                            val user = userLocation
                            val st = selectedStation
                            if (user != null && st != null) {
                                launchCabApp(context, CabProvider.RAPIDO, user.latitude, user.longitude, st.lat, st.lon, st.name)
                            } else {
                                Toast.makeText(context, "Location not ready yet", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Rapido", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E1E1E))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Google Maps fallback directions button
                OutlinedButton(
                    onClick = {
                        val user = userLocation
                        val st = selectedStation
                        if (user != null && st != null) {
                            launchCabApp(context, CabProvider.MAPS, user.latitude, user.longitude, st.lat, st.lon, st.name)
                        } else {
                            Toast.makeText(context, "Location not ready yet", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Open Route in Google Maps",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }

        // =====================================
        // STATION SELECTOR DIALOG
        // =====================================
        if (showStationSelector) {
            StationPickerDialog(
                stations = allStations,
                currentStation = selectedStation,
                onDismiss = { showStationSelector = false },
                onStationSelected = { st ->
                    selectedStation = st
                    showStationSelector = false
                }
            )
        }
    }
}

@Composable
fun StationPickerDialog(
    stations: List<MetroStation>,
    currentStation: MetroStation?,
    onDismiss: () -> Unit,
    onStationSelected: (MetroStation) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, stations) {
        if (query.isBlank()) stations
        else stations.filter { it.name.contains(query, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Metro Station",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search station…") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFD32F2F))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered) { station ->
                        val isSelected = station.name == currentStation?.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStationSelected(station) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFFFEBEE) else Color(0xFFF9F9F9)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subway,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFFD32F2F) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = station.name,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFFD32F2F) else Color(0xFF212121),
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

