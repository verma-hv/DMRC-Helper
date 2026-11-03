package com.example.metrohelper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.metrohelper.ui.theme.MetroHelperTheme
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.tileprovider.tilesource.XYTileSource
data class DashboardItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class MetroStation(
    val name: String,
    val lat: Double,
    val lon: Double
)

data class ParkingStation(
    val id: Int,
    val line: String,
    val station: String,
    val contractor: String,
    val contact: String,
    val additionalContacts: String,
    val note: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OSMDroid configuration
        val osmdroidConfig = Configuration.getInstance()

        osmdroidConfig.load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        osmdroidConfig.userAgentValue =
            "DMRCHelper/1.0 (contact: thegtofficial22@gmail.com)"

// Keep downloaded map tiles cached locally
        osmdroidConfig.tileFileSystemCacheMaxBytes =
            100L * 1024L * 1024L

        osmdroidConfig.tileFileSystemCacheTrimBytes =
            80L * 1024L * 1024L

        // Request location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }

        enableEdgeToEdge()

        setContent {
            MetroHelperTheme {
                MetroApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetroApp() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem("metromap", "Metro Map", Icons.Default.Map),
        BottomNavItem("neareststation", "Nearby", Icons.Default.LocationOn), // ✅ NEW
        BottomNavItem("booktickets", "Tickets", Icons.Default.ConfirmationNumber),
        BottomNavItem("availableparkings", "Parkings", Icons.Default.LocalParking),
        BottomNavItem("aboutapp", "About App", Icons.Default.Info)
    )


    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            TopAppBar(
                title = { Text("DMRC Helper") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding()
                )
                .fillMaxSize()
        ) {
            NavigationGraph(navController)
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)


@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "dashboard") {

        composable("routeplanner") { RoutePlannerScreen() }
        composable("dashboard") { DashboardHomeScreen(navController) }
        composable("metromap") { MetroMapScreen() }

        composable("neareststation") { NearestStationScreen() } // ✅ NEW

        composable("booktickets") { BookTicketsScreen() }
        composable("availableparkings") { AvailableParkingsScreen() }
        composable("aboutapp") { AboutAppScreen() }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerScreen() {

    val context = LocalContext.current
    val stations = remember { loadMetroStations(context) }

    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }

    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }

    var result by remember { mutableStateOf("") }

    val filteredFrom = stations.filter {
        it.name.contains(from, ignoreCase = true)
    }

    val filteredTo = stations.filter {
        it.name.contains(to, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Metro Route Planner",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // FROM
        ExposedDropdownMenuBox(
            expanded = expandedFrom,
            onExpandedChange = { expandedFrom = !expandedFrom }
        ) {

            OutlinedTextField(
                value = from,
                onValueChange = {
                    from = it
                    expandedFrom = true
                },
                label = { Text("From Station") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandedFrom,
                onDismissRequest = { expandedFrom = false }
            ) {

                filteredFrom.take(8).forEach { station ->

                    DropdownMenuItem(
                        text = { Text(station.name) },
                        onClick = {
                            from = station.name
                            expandedFrom = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TO
        ExposedDropdownMenuBox(
            expanded = expandedTo,
            onExpandedChange = { expandedTo = !expandedTo }
        ) {

            OutlinedTextField(
                value = to,
                onValueChange = {
                    to = it
                    expandedTo = true
                },
                label = { Text("To Station") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandedTo,
                onDismissRequest = { expandedTo = false }
            ) {

                filteredTo.take(8).forEach { station ->

                    DropdownMenuItem(
                        text = { Text(station.name) },
                        onClick = {
                            to = station.name
                            expandedTo = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                val start = stations.find { it.name == from }
                val end = stations.find { it.name == to }

                if (start != null && end != null) {

                    val distance = calculateDistance(
                        start.lat,
                        start.lon,
                        end.lat,
                        end.lon
                    )

                    val time = (distance * 3).toInt()
                    val fare = (distance * 2).toInt()

                    result =
                        "Distance: %.1f km\nEstimated Time: $time mins\nFare: ₹$fare"
                            .format(distance)

                } else {
                    result = "Please select valid stations"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Find Route")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = result,
            fontSize = 18.sp
        )
    }
}

@Composable
fun DashboardHomeScreen(navController: NavHostController) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("metro_stats", Context.MODE_PRIVATE)

    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val day = remember { java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) }

    // Fake stats for now
    val trips = prefs.getInt("trips", 8)
    val savings = trips * 25

    val bestTime = when {
        hour in 8..10 -> "Rush Hour"
        hour in 18..20 -> "Heavy Rush "
        hour in 11..16 -> "Perfect Time To Leave"
        else -> "Good Time To Travel"
    }

    val crowd = when {
        hour in 8..10 -> "Very High"
        hour in 18..20 -> "High"
        hour in 11..16 -> "Low"
        else -> "Medium"
    }

    val suggestion = when {
        day == java.util.Calendar.SUNDAY ->
            "Weekend travel is smooth today"

        hour < 8 ->
            "Leave now to avoid rush"

        hour in 8..10 ->
            "Use less crowded coach"

        else ->
            "Metro is best option now"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                text = "Smart Metro Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
        }

        item {
            Text(
                text = "Live travel intelligence",
                color = Color.Gray
            )
        }

        item { SmartCard("⏰ Best Time To Leave", bestTime) }

        item { SmartCard("📈 Crowd Level", crowd) }

        item { SmartCard("💡 Smart Advice", suggestion) }

        item { SmartCard("💰 Weekly Savings", "₹$savings saved") }

        item { SmartCard("🚇 Trips This Week", "$trips trips completed") }

        item {
            Text(
                text = "Utilities",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ){


                item {
                    DashboardNavCard("Metro Map", Icons.Default.Map) {
                        navController.navigate("metromap")
                    }
                }

                item {
                    DashboardNavCard("Nearby", Icons.Default.LocationOn) {
                        navController.navigate("neareststation")
                    }
                }

                item {
                    DashboardNavCard("Tickets", Icons.Default.ConfirmationNumber) {
                        navController.navigate("booktickets")
                    }
                }
                item { DashboardNavCard("Route Planner", Icons.Default.Subway) {
                    navController.navigate("routeplanner")
                    }
                }
                item {
                    DashboardNavCard("About App", Icons.Default.Info) {
                        navController.navigate("aboutapp")
                    }
                }

                item {
                    DashboardNavCard("Parking", Icons.Default.LocalParking) {
                        navController.navigate("availableparkings")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun SmartCard(title: String, value: String) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
fun DashboardNavCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
@Composable
fun MetroMapScreen() {

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {

                detectTransformGestures { _, pan, zoom, _ ->

                    // Limit zoom
                    val newScale =
                        (scale * zoom).coerceIn(1f, 4f)

                    scale = newScale

                    // Apply pan with limits
                    val maxOffset = 500f * (newScale - 1)

                    offsetX =
                        (offsetX + pan.x)
                            .coerceIn(-maxOffset, maxOffset)

                    offsetY =
                        (offsetY + pan.y)
                            .coerceIn(-maxOffset, maxOffset)
                }
            },
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.mapview),
            contentDescription = "Delhi Metro Map",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = ContentScale.Fit
        )
    }
}

/*----------------- Nearby Station Finder ---------*/

fun loadMetroStations(context: Context): List<MetroStation> {

    val stations = mutableListOf<MetroStation>()

    val jsonString = context.assets
        .open("dmrc_stations.json")
        .bufferedReader()
        .use { it.readText() }

    val jsonArray = JSONArray(jsonString)

    for (i in 0 until jsonArray.length()) {

        val obj = jsonArray.getJSONObject(i)

        stations.add(
            MetroStation(
                obj.getString("Station"),
                obj.getDouble("Latitude"),
                obj.getDouble("Longitude")
            )
        )
    }

    return stations
}

fun findNearestStation(
    userLat: Double,
    userLon: Double,
    stations: List<MetroStation>
): MetroStation? {

    return stations.minByOrNull {

        val result = FloatArray(1)

        android.location.Location.distanceBetween(
            userLat,
            userLon,
            it.lat,
            it.lon,
            result
        )

        result[0]
    }
}

fun calculateDistance(
    userLat: Double,
    userLon: Double,
    stationLat: Double,
    stationLon: Double
): Float {

    val result = FloatArray(1)

    android.location.Location.distanceBetween(
        userLat,
        userLon,
        stationLat,
        stationLon,
        result
    )

    return result[0] / 1000   // km
}

suspend fun getRoute(
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double
): List<GeoPoint> {

    return withContext(Dispatchers.IO) {

        val url =
            "https://router.project-osrm.org/route/v1/driving/$startLon,$startLat;$endLon,$endLat?overview=full&geometries=geojson"

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()

        val json = JSONObject(response.body!!.string())

        val coords = json
            .getJSONArray("routes")
            .getJSONObject(0)
            .getJSONObject("geometry")
            .getJSONArray("coordinates")

        val points = mutableListOf<GeoPoint>()

        for (i in 0 until coords.length()) {

            val coord = coords.getJSONArray(i)

            points.add(
                GeoPoint(
                    coord.getDouble(1),
                    coord.getDouble(0)
                )
            )
        }

        points
    }
}

fun checkLocationEnabled(activity: Activity, onEnabled: () -> Unit) {

    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 1000
    ).build()

    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)

    val client = LocationServices.getSettingsClient(activity)

    val task = client.checkLocationSettings(builder.build())

    task.addOnSuccessListener {
        onEnabled()
    }

    task.addOnFailureListener { exception ->

        if (exception is ResolvableApiException) {
            try {
                exception.startResolutionForResult(activity, 200)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
@SuppressLint("MissingPermission")
@Composable
fun NearestStationScreen() {

    val context = LocalContext.current
    val activity = context as Activity

    var userLocation by remember {
        mutableStateOf<GeoPoint?>(null)
    }

    var nearestStation by remember { mutableStateOf<MetroStation?>(null) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    val stations = remember {
        loadMetroStations(context)
    }

    // -----------------------------------------
    // GET USER LOCATION
    // -----------------------------------------

    LaunchedEffect(Unit) {

        checkLocationEnabled(activity) {

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->

                location?.let {

                    userLocation =
                        GeoPoint(
                            it.latitude,
                            it.longitude
                        )

                    nearestStation =
                        findNearestStation(
                            it.latitude,
                            it.longitude,
                            stations
                        )
                }
            }
        }
    }

    // -----------------------------------------
    // GET ROUTE
    // -----------------------------------------

    LaunchedEffect(userLocation, nearestStation) {

        if (
            userLocation != null &&
            nearestStation != null
        ) {

            routePoints = getRoute(
                userLocation!!.latitude,
                userLocation!!.longitude,
                nearestStation!!.lat,
                nearestStation!!.lon
            )
        }
    }
    // -----------------------------------------
// CENTER MAP AUTOMATICALLY ON USER LOCATION
// -----------------------------------------

    LaunchedEffect(userLocation, mapViewRef) {

        val location = userLocation
        val map = mapViewRef

        if (location != null && map != null) {

            map.post {

                map.controller.setZoom(15.5)

                map.controller.animateTo(location)

                map.invalidate()
            }
        }
    }

    // -----------------------------------------
    // DISTANCE
    // -----------------------------------------

    val distance = if (
        userLocation != null &&
        nearestStation != null
    ) {

        calculateDistance(
            userLocation!!.latitude,
            userLocation!!.longitude,
            nearestStation!!.lat,
            nearestStation!!.lon
        )

    } else {
        0f
    }

    val estimatedTime =
        (distance * 12).toInt()

// -----------------------------------------
// MAP UI
// -----------------------------------------



    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ->

        // =====================================
        // MAP
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
                        arrayOf(
                            "https://tile.openstreetmap.org/"
                        )
                    )

                    setTileSource(mapnikTileSource)

                    setMultiTouchControls(true)

                    controller.setZoom(15.5)
                }
            },

            update = { mapView ->

                mapView.overlays.clear()

                // =================================
                // USER LOCATION
                // =================================

                userLocation?.let { userPoint ->

                    val userMarker = Marker(mapView)

                    userMarker.position = userPoint

                    userMarker.title = "You are here"

                    mapView.overlays.add(userMarker)
                }

                // =================================
                // NEAREST STATION
                // =================================

                nearestStation?.let { station ->

                    val stationPoint = GeoPoint(
                        station.lat,
                        station.lon
                    )

                    val stationMarker = Marker(mapView)

                    stationMarker.position = stationPoint

                    stationMarker.title = station.name

                    mapView.overlays.add(stationMarker)
                }

                // =================================
                // ROUTE
                // =================================

                if (routePoints.isNotEmpty()) {

                    // Outer border
                    val routeBorder = Polyline()

                    routeBorder.setPoints(routePoints)

                    routeBorder.outlinePaint.color =
                        android.graphics.Color.WHITE

                    routeBorder.outlinePaint.strokeWidth = 14f

                    mapView.overlays.add(routeBorder)

                    // Main blue route
                    val route = Polyline()

                    route.setPoints(routePoints)

                    route.outlinePaint.color =
                        android.graphics.Color.rgb(
                            33,
                            150,
                            243
                        )

                    route.outlinePaint.strokeWidth = 8f

                    mapView.overlays.add(route)
                }

                mapView.invalidate()
            }
        )

        // =====================================
        // OSM ATTRIBUTION
        // =====================================
        Text(
            text = "© OpenStreetMap contributors",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 12.dp,
                    bottom = 155.dp
                ),
            fontSize = 10.sp,
            color = Color.DarkGray
        )

        // =====================================
        // TOP HEADER
        // =====================================

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp
                )
                .height(92.dp),

            shape = RoundedCornerShape(22.dp),

            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),

            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(
                    alpha = 0.94f
                )
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 18.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "🚇",
                    fontSize = 32.sp
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column {

                    Text(
                        text = "Nearby Metro",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )

                    Text(
                        text = "Finding the best station for you",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }


        // =====================================
        // ZOOM CONTROLS
        // =====================================

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(
                    end = 14.dp
                )
        ) {

            FloatingMapButton(
                modifier = Modifier.size(48.dp),
                icon = Icons.Default.Add
            ) {

                mapViewRef?.controller?.zoomIn()
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            FloatingMapButton(
                modifier = Modifier.size(48.dp),
                icon = Icons.Default.Remove
            ) {

                mapViewRef?.controller?.zoomOut()
            }
        }


        // =====================================
        // LOCATE ME
        // =====================================

        FloatingMapButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 14.dp,
                    bottom = 205.dp
                )
                .size(48.dp),

            icon = Icons.Default.MyLocation

        ) {

            userLocation?.let { point ->

                mapViewRef?.controller?.animateTo(
                    point
                )

                mapViewRef?.controller?.setZoom(
                    16.5
                )
            }
        }


        // =====================================
        // BOTTOM STATION CARD
        // =====================================

        nearestStation?.let { station ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 12.dp
                    )
                    .align(Alignment.BottomCenter),

                shape = RoundedCornerShape(24.dp),

                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                ),

                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(
                        alpha = 0.97f
                    )
                )
            ) {

                Column(
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    )
                ) {

                    // Station title
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.Subway,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(34.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        Column {

                            Text(
                                text = "Nearest Metro Station",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = station.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    // Distance + ETA
                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        InfoChip(
                            "📍 %.1f km".format(distance)
                        )

                        InfoChip(
                            "⏱ ~${estimatedTime} min"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingMapButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit
) {

    Card(
        modifier = modifier
            .size(52.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation =
            CardDefaults.cardElevation(
                8.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint =
                    Color(0xFFD32F2F),
                modifier =
                    Modifier.size(25.dp)
            )
        }
    }
}

@Composable
fun InfoChip(text: String) {

    Card(
        shape =
            RoundedCornerShape(50.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFFF5F5F5)
            )
    ) {

        Text(
            text = text,
            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 7.dp
                ),
            fontSize = 13.sp,
            fontWeight =
                FontWeight.Medium
        )
    }
}

@Composable
fun BookTicketsScreen() {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Book Metro Tickets",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // DMRC Website
        TicketOptionCard(
            icon = Icons.Default.ConfirmationNumber,
            title = "DMRC Official Booking",
            subtitle = "Book via DMRC website",
            backgroundColor = Color(0xFFD32F2F)
        ) {
            openLinkSafely(
                context,
                "https://qrticket.dmrc.org/qrapp/"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // WhatsApp Booking
        TicketOptionCard(
            icon = Icons.Default.Info,
            title = "Book via WhatsApp",
            subtitle = "DMRC WhatsApp service",
            backgroundColor = Color(0xFF25D366)
        ) {

            val message = "Hi DMRC, I want to book a metro ticket."

            val encodedMessage = Uri.encode(message)

            val whatsappUrl =
                "https://wa.me/919650855800?text=$encodedMessage"

            openLinkSafely(context, whatsappUrl)
        }

    }
}
fun openLinkSafely(context: Context, url: String) {

    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)

    } catch (e: Exception) {

        Toast.makeText(
            context,
            "Please install a browser or WhatsApp to continue",
            Toast.LENGTH_LONG
        ).show()
    }
}

// -------- Google Maps Nearest Loc fun() --------
/*fun openNearestMetroInMaps(context: Context) {

    try {
        // Google Maps search intent
        val uri = Uri.parse("geo:0,0?q=nearest DMRC metro station")

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        context.startActivity(intent)

    } catch (e: Exception) {

        // Fallback to browser if Maps not installed
        openLinkSafely(
            context,
            "https://www.google.com/maps/search/nearest+DMRC+metro+station"
        )
    }
} */


@Composable
fun TicketOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}



fun loadParkingStations(context: Context): List<ParkingStation> {
    return try {
        val jsonString = context.assets
            .open("delhi_metro_parking_clean.json")
            .bufferedReader()
            .use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val stations = mutableListOf<ParkingStation>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            stations.add(
                ParkingStation(
                    id = obj.getInt("id"),
                    line = obj.getString("line"),
                    station = obj.getString("station"),
                    contractor = obj.getString("contractor"),
                    contact = obj.getString("contact"),
                    additionalContacts = obj.optString("additional_contacts", ""),
                    note = obj.optString("note", "")
                )
            )
        }
        stations
    } catch (e: Exception) {
        emptyList()
    }
}

fun lineColor(line: String): Color = when {
    line.contains("Red", ignoreCase = true) -> Color(0xFFD32F2F)
    line.contains("Yellow", ignoreCase = true) -> Color(0xFFF9A825)
    line.contains("Blue", ignoreCase = true) -> Color(0xFF1565C0)
    line.contains("Green", ignoreCase = true) -> Color(0xFF2E7D32)
    line.contains("Violet", ignoreCase = true) -> Color(0xFF6A1B9A)
    line.contains("Pink", ignoreCase = true) -> Color(0xFFAD1457)
    line.contains("Magenta", ignoreCase = true) -> Color(0xFF880E4F)
    line.contains("Grey", ignoreCase = true) -> Color(0xFF546E7A)
    line.contains("Aqua", ignoreCase = true) -> Color(0xFF00838F)
    line.contains("Orange", ignoreCase = true) -> Color(0xFFE65100)
    else -> Color(0xFFD32F2F)
}

@Composable
fun AvailableParkingsScreen() {
    val context = LocalContext.current
    var allStations by remember { mutableStateOf<List<ParkingStation>>(emptyList()) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        allStations = withContext(Dispatchers.IO) { loadParkingStations(context) }
    }

    val filtered = remember(query, allStations) {
        if (query.isBlank()) allStations
        else allStations.filter {
            it.station.contains(query, ignoreCase = true) ||
                it.line.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            placeholder = { Text("Search by station or line…") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color(0xFFD32F2F)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD32F2F),
                cursorColor = Color(0xFFD32F2F)
            )
        )

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.LocalParking,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F).copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Not Available",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No parking contractor found for\n\"$query\"",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { station ->
                    ParkingStationCard(station = station)
                }
            }
        }
    }
}

@Composable
fun ParkingStationCard(station: ParkingStation) {
    val context = LocalContext.current
    val accentColor = lineColor(station.line)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Colored header bar with line name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accentColor, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Subway,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = station.line,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Card body
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = station.station,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = station.contractor,
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )
                if (station.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = station.note,
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Contact row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = station.contact,
                        fontSize = 14.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${station.contact}")
                            }
                            context.startActivity(intent)
                        }
                    )
                    if (station.additionalContacts.isNotBlank()) {
                        Text(
                            text = "  |  ${station.additionalContacts}",
                            fontSize = 14.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${station.additionalContacts}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutAppScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "DMRC Helper",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Smart Delhi Metro Assistant",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "This app helps users navigate Delhi Metro using route planner, nearest station finder, metro map, ticket booking and parking assistance.",
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Developed By",
            fontWeight = FontWeight.Bold
        )

        Text("Harshit Verma")
        Text("Devansh Jindal")
        Text("Debanshu Mazumdar")
        Text("Samarth Gupta")
        Text("Abhay Pratab Singh")
        Text("Amit Shahare")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Guided By",
            fontWeight = FontWeight.Bold
        )

        Text("Mr. Munish Kumar")

        Spacer(modifier = Modifier.height(16.dp))

        Text("Version 1.0")

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "This is an academic project and not officially affiliated with DMRC.",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CenteredText(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/* Optional: Dashboard-like grid reused later */
@Composable
fun DashboardGrid() {
    val items = listOf(
        DashboardItem("Plan Journey", Icons.Filled.Subway),
        DashboardItem("Nearest Station", Icons.Filled.LocationOn),
        DashboardItem("Live Status", Icons.Filled.Train),
        DashboardItem("Fare Calculator", Icons.Filled.AccountBalanceWallet)
    )

    LazyVerticalGrid(columns = GridCells.Adaptive(160.dp)) {
        items(items) { item -> DashboardCard(item) }
    }
}

@Composable
fun DashboardCard(item: DashboardItem) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(130.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Preview(showBackground = true)
@Composable
fun MetroAppPreview() {
    MetroHelperTheme {
        MetroApp()
    }
}
