package com.example.metrohelper

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getLinesForStation(stationName: String): List<MetroLine> {
    return MetroNetwork.allLines.filter { line ->
        line.stations.any { it.equals(stationName.trim(), ignoreCase = true) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val allStations = remember { loadMetroStations(context) }

    var fromStation by remember { mutableStateOf("AIIMS") }
    var toStation by remember { mutableStateOf("Akshardham") }

    // Active field tracking for glitch-free inline suggestions
    var activeSearchField by remember { mutableStateOf<String?>(null) } // "FROM", "TO", or null
    var showDialogPickerFor by remember { mutableStateOf<String?>(null) } // "FROM", "TO", or null

    var plannedJourney by remember { mutableStateOf<PlannedJourney?>(null) }
    var hasSearched by remember { mutableStateOf(false) }

    // Filter matching stations based on what the user types
    val filteredFrom = remember(fromStation, allStations) {
        if (fromStation.isBlank()) allStations
        else allStations.filter { it.name.contains(fromStation.trim(), ignoreCase = true) }
    }

    val filteredTo = remember(toStation, allStations) {
        if (toStation.isBlank()) allStations
        else allStations.filter { it.name.contains(toStation.trim(), ignoreCase = true) }
    }

    val isFromExactMatch = remember(fromStation, allStations) {
        allStations.any { it.name.equals(fromStation.trim(), ignoreCase = true) }
    }

    val isToExactMatch = remember(toStation, allStations) {
        allStations.any { it.name.equals(toStation.trim(), ignoreCase = true) }
    }

    // Auto-calculate route on initial load
    LaunchedEffect(Unit) {
        val initialRoute = MetroRouteFinder.findRoute(fromStation, toStation)
        if (initialRoute != null) {
            plannedJourney = initialRoute
            hasSearched = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFD32F2F)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = "Metro Route Planner",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            text = "Interchanges, Travel Times & Official Fares",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Box Container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F6F8))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // 1. FROM STATION TEXT FIELD
                                OutlinedTextField(
                                    value = fromStation,
                                    onValueChange = {
                                        fromStation = it
                                        activeSearchField = "FROM"
                                    },
                                    label = { Text("From Station", fontSize = 12.sp) },
                                    placeholder = { Text("Search departure station…", fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.TripOrigin,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (fromStation.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        fromStation = ""
                                                        activeSearchField = "FROM"
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Clear",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = { showDialogPickerFor = "FROM" },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.List,
                                                    contentDescription = "Browse Stations",
                                                    tint = Color(0xFFD32F2F),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFFD32F2F)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged {
                                            if (it.isFocused) {
                                                activeSearchField = "FROM"
                                            }
                                        }
                                )

                                // INLINE AUTOCOMPLETE SUGGESTIONS FOR "FROM"
                                AnimatedVisibility(
                                    visible = activeSearchField == "FROM" && filteredFrom.isNotEmpty() && (!isFromExactMatch || filteredFrom.size > 1),
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp, bottom = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            filteredFrom.take(5).forEach { station ->
                                                val lines = remember(station.name) { getLinesForStation(station.name) }
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            fromStation = station.name
                                                            activeSearchField = null
                                                            focusManager.clearFocus()
                                                            plannedJourney = MetroRouteFinder.findRoute(station.name, toStation)
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Subway,
                                                        contentDescription = null,
                                                        tint = lines.firstOrNull()?.color ?: Color(0xFFD32F2F),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = station.name,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color(0xFF212121)
                                                        )
                                                        if (lines.isNotEmpty()) {
                                                            Text(
                                                                text = lines.joinToString(" • ") { it.name },
                                                                fontSize = 11.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    }
                                                }
                                                if (station != filteredFrom.take(5).last()) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(horizontal = 12.dp),
                                                        color = Color(0xFFF0F0F0)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2. TO STATION TEXT FIELD
                                OutlinedTextField(
                                    value = toStation,
                                    onValueChange = {
                                        toStation = it
                                        activeSearchField = "TO"
                                    },
                                    label = { Text("To Station", fontSize = 12.sp) },
                                    placeholder = { Text("Search arrival station…", fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (toStation.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        toStation = ""
                                                        activeSearchField = "TO"
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Clear",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = { showDialogPickerFor = "TO" },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.List,
                                                    contentDescription = "Browse Stations",
                                                    tint = Color(0xFFD32F2F),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFFD32F2F)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged {
                                            if (it.isFocused) {
                                                activeSearchField = "TO"
                                            }
                                        }
                                )

                                // INLINE AUTOCOMPLETE SUGGESTIONS FOR "TO"
                                AnimatedVisibility(
                                    visible = activeSearchField == "TO" && filteredTo.isNotEmpty() && (!isToExactMatch || filteredTo.size > 1),
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp, bottom = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            filteredTo.take(5).forEach { station ->
                                                val lines = remember(station.name) { getLinesForStation(station.name) }
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            toStation = station.name
                                                            activeSearchField = null
                                                            focusManager.clearFocus()
                                                            plannedJourney = MetroRouteFinder.findRoute(fromStation, station.name)
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Subway,
                                                        contentDescription = null,
                                                        tint = lines.firstOrNull()?.color ?: Color(0xFFD32F2F),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = station.name,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color(0xFF212121)
                                                        )
                                                        if (lines.isNotEmpty()) {
                                                            Text(
                                                                text = lines.joinToString(" • ") { it.name },
                                                                fontSize = 11.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    }
                                                }
                                                if (station != filteredTo.take(5).last()) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(horizontal = 12.dp),
                                                        color = Color(0xFFF0F0F0)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Swap Stations Button
                            FilledTonalIconButton(
                                onClick = {
                                    val temp = fromStation
                                    fromStation = toStation
                                    toStation = temp
                                    activeSearchField = null
                                    focusManager.clearFocus()
                                    if (hasSearched) {
                                        plannedJourney = MetroRouteFinder.findRoute(fromStation, toStation)
                                    }
                                },
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Swap Stations",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Find Route Action Button
                Button(
                    onClick = {
                        activeSearchField = null
                        focusManager.clearFocus()
                        hasSearched = true
                        plannedJourney = MetroRouteFinder.findRoute(fromStation, toStation)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Subway,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Find Optimal Route", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Body: Journey Details & Timeline
        if (hasSearched) {
            val journey = plannedJourney
            if (journey != null && journey.segments.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Metric Highlights Card
                    item {
                        JourneySummaryMetricsCard(
                            journey = journey,
                            onNavigateToExpenses = { navController.navigate("expensetracker") }
                        )
                    }

                    // 2. Journey Timeline Header
                    item {
                        Text(
                            text = "STEP-BY-STEP ROUTE & TRANSFERS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // 3. Visual Timeline Segments
                    items(journey.segments.size) { index ->
                        val segment = journey.segments[index]
                        val interchange = journey.interchanges.getOrNull(index)

                        TimelineSegmentCard(
                            segment = segment,
                            isFirstSegment = index == 0,
                            isLastSegment = index == journey.segments.size - 1,
                            interchange = interchange
                        )
                    }

                    // 4. Action Shortcuts Footer
                    item {
                        RouteActionButtonsFooter(
                            fromStation = journey.origin,
                            navController = navController
                        )
                    }
                }
            } else if (journey != null && journey.origin.equals(journey.destination, ignoreCase = true)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Origin and destination stations are the same!\nPlease select two different stations.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No route found between '$fromStation' and '$toStation'.\nPlease check station spellings.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Full Screen Station Picker Dialog fallback
        if (showDialogPickerFor != null) {
            val isFromPicker = showDialogPickerFor == "FROM"
            RouteStationPickerDialog(
                title = if (isFromPicker) "Select Origin Station" else "Select Destination Station",
                stations = allStations,
                currentStationName = if (isFromPicker) fromStation else toStation,
                onDismiss = { showDialogPickerFor = null },
                onStationSelected = { selectedStation ->
                    if (isFromPicker) {
                        fromStation = selectedStation.name
                    } else {
                        toStation = selectedStation.name
                    }
                    showDialogPickerFor = null
                    activeSearchField = null
                    focusManager.clearFocus()
                    plannedJourney = MetroRouteFinder.findRoute(fromStation, toStation)
                }
            )
        }
    }
}

@Composable
fun RouteStationPickerDialog(
    title: String,
    stations: List<MetroStation>,
    currentStationName: String,
    onDismiss: () -> Unit,
    onStationSelected: (MetroStation) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, stations) {
        if (searchQuery.isBlank()) stations
        else stations.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f),
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
                        text = title,
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
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search station name…") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFD32F2F))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
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
                        val isSelected = station.name.equals(currentStationName.trim(), ignoreCase = true)
                        val lines = remember(station.name) { getLinesForStation(station.name) }

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
                                    tint = if (isSelected) Color(0xFFD32F2F) else (lines.firstOrNull()?.color ?: Color.Gray),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = station.name,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFFD32F2F) else Color(0xFF212121)
                                    )
                                    if (lines.isNotEmpty()) {
                                        Text(
                                            text = lines.joinToString(" • ") { it.name },
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
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

@Composable
fun JourneySummaryMetricsCard(
    journey: PlannedJourney,
    onNavigateToExpenses: () -> Unit = {}
) {
    val context = LocalContext.current
    var showExpenseDialog by remember { mutableStateOf(false) }
    var addedFeedback by remember(journey) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Journey Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                // Interchanges badge
                val hasInterchange = journey.interchanges.isNotEmpty()
                Card(
                    shape = RoundedCornerShape(50.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasInterchange) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                    )
                ) {
                    Text(
                        text = if (hasInterchange) "🔄 ${journey.interchanges.size} Interchange(s)" else "✓ Direct Train",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasInterchange) Color(0xFFE65100) else Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Mini Stat Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    title = "Estimated Time",
                    value = "~${journey.estimatedTimeMins} min",
                    icon = "⏱",
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Normal Fare",
                    value = "₹${journey.normalFare}",
                    icon = "💰",
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Smart Card",
                    value = "₹${journey.smartCardFare}",
                    icon = "💳",
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Total Stops",
                    value = "${journey.totalStations}",
                    icon = "🚇",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ADD TO MONTHLY EXPENSES BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showExpenseDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (addedFeedback) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                ) {
                    Icon(
                        imageVector = if (addedFeedback) Icons.Default.CheckCircle else Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (addedFeedback) "✓ Added to Expenses" else "Add to Monthly Expense",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (addedFeedback) {
                    OutlinedButton(
                        onClick = onNavigateToExpenses,
                        modifier = Modifier.height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Text("View Tracker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showExpenseDialog) {
        AddToExpenseDialog(
            journey = journey,
            onDismiss = { showExpenseDialog = false },
            onConfirm = { isSmartCard, isRoundTrip ->
                val singleFare = if (isSmartCard) journey.smartCardFare else journey.normalFare
                val finalFare = if (isRoundTrip) singleFare * 2 else singleFare
                val normalFareTotal = if (isRoundTrip) journey.normalFare * 2 else journey.normalFare
                val trip = MetroExpenseTrip(
                    fromStation = journey.origin,
                    toStation = journey.destination,
                    fare = finalFare,
                    fareType = if (isSmartCard) "Smart Card" else "Token / QR",
                    normalFare = normalFareTotal,
                    isRoundTrip = isRoundTrip,
                    note = "Route Planner"
                )
                MetroExpenseManager.addTrip(context, trip)
                addedFeedback = true
                showExpenseDialog = false
                val currentMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
                Toast.makeText(context, "Added ₹$finalFare to $currentMonth expenses!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AddToExpenseDialog(
    journey: PlannedJourney,
    onDismiss: () -> Unit,
    onConfirm: (isSmartCard: Boolean, isRoundTrip: Boolean) -> Unit
) {
    var selectedFareType by remember { mutableStateOf("Smart Card") }
    var isRoundTrip by remember { mutableStateOf(false) }

    val singleFare = if (selectedFareType == "Smart Card") journey.smartCardFare else journey.normalFare
    val totalFare = if (isRoundTrip) singleFare * 2 else singleFare

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log to Monthly Expense", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Trip Summary line
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "${journey.origin} → ${journey.destination}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = "~${journey.estimatedTimeMins} min • ${journey.totalStations} stops • ${if (journey.interchanges.isEmpty()) "Direct" else "${journey.interchanges.size} Interchange(s)"}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = "SELECT PAYMENT TYPE:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                // Smart Card option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedFareType = "Smart Card" },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedFareType == "Smart Card") Color(0xFFE8F5E9) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (selectedFareType == "Smart Card") 2.dp else 1.dp,
                        color = if (selectedFareType == "Smart Card") Color(0xFF2E7D32) else Color(0xFFE0E0E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💳 Smart Card", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF2E7D32)
                                ) {
                                    Text(
                                        text = "10% OFF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("DMRC metro smart card discount", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text("₹${journey.smartCardFare}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                    }
                }

                // Normal Token option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedFareType = "Normal Token / QR" },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedFareType != "Smart Card") Color(0xFFFFF3E0) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (selectedFareType != "Smart Card") 2.dp else 1.dp,
                        color = if (selectedFareType != "Smart Card") Color(0xFFE65100) else Color(0xFFE0E0E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🎟️ Normal Token / QR Ticket", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Standard counter or DMRC app QR fare", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text("₹${journey.normalFare}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                    }
                }

                // Round trip checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isRoundTrip = !isRoundTrip }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isRoundTrip,
                        onCheckedChange = { isRoundTrip = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Round Trip (Return journey today)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Total: 2x fare = ₹$totalFare", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedFareType == "Smart Card", isRoundTrip)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add ₹$totalFare to Expense", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun MetricChip(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD32F2F)
            )
            Text(
                text = title,
                fontSize = 9.sp,
                color = Color.Gray,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TimelineSegmentCard(
    segment: RouteSegment,
    isFirstSegment: Boolean,
    isLastSegment: Boolean,
    interchange: RouteInterchange?
) {
    var expandedStops by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Line Header Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(segment.lineColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = segment.line.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = segment.lineColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${segment.stationCount} stations",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Boarding Station
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .border(3.dp, segment.lineColor, CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = segment.fromStation,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = if (isFirstSegment) "Board train here" else "Continue on this line",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            // 2. Intermediate Stations Dropdown
            if (segment.intermediateStations.isNotEmpty()) {
                Row(modifier = Modifier.padding(start = 9.dp)) {
                    // Vertical line connector
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(if (expandedStops) (segment.intermediateStations.size * 26 + 36).dp else 36.dp)
                            .background(segment.lineColor.copy(alpha = 0.5f))
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        // Expand/Collapse text button
                        Row(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clickable { expandedStops = !expandedStops },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (expandedStops) "▲ Hide intermediate stations" else "▼ Show ${segment.intermediateStations.size} intermediate stations",
                                fontSize = 12.sp,
                                color = segment.lineColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Expanded stations list
                        AnimatedVisibility(
                            visible = expandedStops,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                segment.intermediateStations.forEach { stationName ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(segment.lineColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stationName,
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Short vertical connector
                Box(
                    modifier = Modifier
                        .padding(start = 9.dp)
                        .width(3.dp)
                        .height(20.dp)
                        .background(segment.lineColor.copy(alpha = 0.5f))
                )
            }

            // 3. Leg Destination / Interchange Station
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(segment.lineColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = segment.toStation,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = if (isLastSegment) "🏁 Destination reached" else "Alight for transfer",
                        fontSize = 11.sp,
                        color = if (isLastSegment) Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 4. Interchange Card (if there's a line change here)
            if (interchange != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFB300))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔄", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Interchange at ${interchange.station}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "Change from ${interchange.fromLine} ➔ ${interchange.toLine}",
                                fontSize = 12.sp,
                                color = Color(0xFF424242)
                            )
                            Text(
                                text = "Follow platform signs • ~5 min walk",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteActionButtonsFooter(
    fromStation: String,
    navController: NavHostController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "TRAVEL UTILITIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Book Cab to Start Station
                Button(
                    onClick = {
                        navController.navigate("bookcab?station=${Uri.encode(fromStation)}")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalTaxi,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFB300)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cab to Station", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Book Metro Ticket
                Button(
                    onClick = {
                        navController.navigate("booktickets")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Book Ticket", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
