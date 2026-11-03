package com.example.metrohelper

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetroExpenseScreen(navController: NavController) {
    val context = LocalContext.current

    // Calendar state for month switching
    val calendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) } // 1-12

    // State tracking to force recomposition on modifications
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Dialog states
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var tripToDelete by remember { mutableStateOf<MetroExpenseTrip?>(null) }

    // Fetch summary
    val summary = remember(selectedYear, selectedMonth, refreshTrigger) {
        MetroExpenseManager.getMonthlySummary(context, selectedYear, selectedMonth)
    }

    val frequentRoutes = remember(refreshTrigger) {
        MetroExpenseManager.getFrequentRoutes(context)
    }

    val isCurrentMonth = remember(selectedYear, selectedMonth) {
        val now = Calendar.getInstance()
        now.get(Calendar.YEAR) == selectedYear && (now.get(Calendar.MONTH) + 1) == selectedMonth
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Metro Expense Tracker",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Monthly Spending & Smart Card Savings",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showManualAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Expense",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD32F2F)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. MONTH SELECTOR BAR
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedMonth == 1) {
                                    selectedMonth = 12
                                    selectedYear -= 1
                                } else {
                                    selectedMonth -= 1
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Month",
                                tint = Color(0xFFD32F2F)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = summary.monthName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            if (!isCurrentMonth) {
                                Text(
                                    text = "Tap to jump to Current Month",
                                    fontSize = 10.sp,
                                    color = Color(0xFFD32F2F),
                                    modifier = Modifier.clickable {
                                        val now = Calendar.getInstance()
                                        selectedYear = now.get(Calendar.YEAR)
                                        selectedMonth = now.get(Calendar.MONTH) + 1
                                    }
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (selectedMonth == 12) {
                                    selectedMonth = 1
                                    selectedYear += 1
                                } else {
                                    selectedMonth += 1
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Month",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            // 2. HERO SPEND & BUDGET CARD
            item {
                HeroExpenseCard(
                    summary = summary,
                    onEditBudget = { showBudgetDialog = true }
                )
            }

            // 3. QUICK 1-TAP COMMUTE LOG (if frequent routes exist)
            if (frequentRoutes.isNotEmpty()) {
                item {
                    FrequentRoutesCard(
                        routes = frequentRoutes,
                        onLogRoute = { from, to ->
                            val route = MetroRouteFinder.findRoute(from, to)
                            val fare = route?.smartCardFare ?: 30
                            val normal = route?.normalFare ?: 30
                            val newTrip = MetroExpenseTrip(
                                fromStation = from,
                                toStation = to,
                                fare = fare,
                                fareType = "Smart Card",
                                normalFare = normal,
                                isRoundTrip = false,
                                note = "Quick Commute"
                            )
                            MetroExpenseManager.addTrip(context, newTrip)
                            refreshTrigger++
                            Toast.makeText(context, "Logged ₹$fare ($from → $to)", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // 4. HEADER: TRIP HISTORY
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trip History (${summary.trips.size})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    if (summary.trips.isNotEmpty()) {
                        Text(
                            text = "${summary.tripCount} journeys taken",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // 5. TRIPS LIST OR EMPTY STATE
            if (summary.trips.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEBEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "No trips logged for ${summary.monthName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Plan a route in the Route Planner and tap 'Add to Expense' to track your metro budget automatically.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { navController.navigate("routeplanner") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subway,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Plan a Route Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Group trips by date
                val grouped = summary.trips.groupBy { MetroExpenseManager.formatDateGroup(it.timestamp) }

                grouped.forEach { (dateHeader, tripsForDate) ->
                    item {
                        Text(
                            text = dateHeader,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp, start = 4.dp)
                        )
                    }

                    items(tripsForDate, key = { it.id }) { trip ->
                        ExpenseTripItemCard(
                            trip = trip,
                            onDelete = { tripToDelete = trip }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // DIALOG: EDIT BUDGET
    if (showBudgetDialog) {
        var newBudgetText by remember { mutableStateOf(summary.budget.toString()) }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Set Monthly Budget") },
            text = {
                Column {
                    Text("Enter your target metro travel budget for each month:", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newBudgetText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) newBudgetText = it },
                        label = { Text("Budget (₹)") },
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newBudgetText.toIntOrNull() ?: 2000
                        MetroExpenseManager.setMonthlyBudget(context, amount)
                        refreshTrigger++
                        showBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Save Budget")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // DIALOG: CONFIRM DELETE
    tripToDelete?.let { trip ->
        AlertDialog(
            onDismissRequest = { tripToDelete = null },
            title = { Text("Remove Expense?") },
            text = {
                Text(
                    "Are you sure you want to delete the trip from ${trip.fromStation} to ${trip.toStation} (₹${trip.fare})?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        MetroExpenseManager.deleteTrip(context, trip.id)
                        tripToDelete = null
                        refreshTrigger++
                        Toast.makeText(context, "Trip removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { tripToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // DIALOG: MANUAL EXPENSE ENTRY
    if (showManualAddDialog) {
        ManualAddExpenseDialog(
            onDismiss = { showManualAddDialog = false },
            onAdd = { from, to, fare, isSmartCard, isRoundTrip ->
                val singleFare = fare.toIntOrNull() ?: 30
                val finalFare = if (isRoundTrip) singleFare * 2 else singleFare
                val normal = if (isSmartCard) (singleFare * 10 / 9) else singleFare
                val newTrip = MetroExpenseTrip(
                    fromStation = from.ifBlank { "Metro Station" },
                    toStation = to.ifBlank { "Destination" },
                    fare = finalFare,
                    fareType = if (isSmartCard) "Smart Card" else "Token / QR",
                    normalFare = if (isRoundTrip) normal * 2 else normal,
                    isRoundTrip = isRoundTrip,
                    note = "Manual Entry"
                )
                MetroExpenseManager.addTrip(context, newTrip)
                refreshTrigger++
                showManualAddDialog = false
                Toast.makeText(context, "Added ₹$finalFare to expenses", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Hero Spend & Budget Card
 */
@Composable
fun HeroExpenseCard(
    summary: MonthlyExpenseSummary,
    onEditBudget: () -> Unit
) {
    val progress = if (summary.budget > 0) {
        (summary.totalSpent.toFloat() / summary.budget.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val isOverBudget = summary.totalSpent > summary.budget && summary.budget > 0
    val progressColor = when {
        isOverBudget -> Color(0xFFD32F2F)
        progress > 0.8f -> Color(0xFFFF9800)
        else -> Color(0xFF2E7D32)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Budget header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL SPENT THIS MONTH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEditBudget() }
                        .background(Color(0xFFF5F5F5))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Budget: ₹${summary.budget}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Big Spend Amount
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹${summary.totalSpent}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isOverBudget) Color(0xFFD32F2F) else Color(0xFF212121)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (summary.budget > 0) {
                    Text(
                        text = if (isOverBudget) "(Exceeded by ₹${summary.totalSpent - summary.budget})"
                        else "(₹${summary.budget - summary.totalSpent} left)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isOverBudget) Color(0xFFD32F2F) else Color(0xFF757575),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = Color(0xFFEEEEEE),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Sub-Metric chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExpenseMiniChip(
                    title = "Trips",
                    value = "${summary.tripCount}",
                    subtitle = "journeys",
                    icon = "🚇",
                    modifier = Modifier.weight(1f)
                )

                ExpenseMiniChip(
                    title = "Saved",
                    value = "₹${summary.totalSavings}",
                    subtitle = "vs Token",
                    icon = "💰",
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFF2E7D32)
                )

                val avgPerTrip = if (summary.tripCount > 0) summary.totalSpent / summary.tripCount else 0
                ExpenseMiniChip(
                    title = "Avg / Trip",
                    value = "₹$avgPerTrip",
                    subtitle = "per ride",
                    icon = "📊",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ExpenseMiniChip(
    title: String,
    value: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF212121)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = "$title ($subtitle)",
                fontSize = 9.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}

/**
 * 1-Tap Quick Commute Card
 */
@Composable
fun FrequentRoutesCard(
    routes: List<Pair<String, String>>,
    onLogRoute: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(0xFFF57F17),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "1-Tap Quick Log Commute",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF57F17)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                routes.take(2).forEach { (from, to) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onLogRoute(from, to) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subway,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "$from → $to",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    color = Color(0xFF212121)
                                )
                                Text(
                                    text = "+ Tap to Log",
                                    fontSize = 9.sp,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Trip Card in Ledger
 */
@Composable
fun ExpenseTripItemCard(
    trip: MetroExpenseTrip,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Metro Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Subway,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Route & Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${trip.fromStation} → ${trip.toStation}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Fare type badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (trip.fareType.contains("Smart", ignoreCase = true)) Color(0xFFE8EAF6) else Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = trip.fareType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (trip.fareType.contains("Smart", ignoreCase = true)) Color(0xFF283593) else Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (trip.isRoundTrip) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE0F2F1)
                        ) {
                            Text(
                                text = "2x Round Trip",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00695C),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "• ${MetroExpenseManager.formatTime(trip.timestamp)}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Fare & Delete
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${trip.fare}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD32F2F)
                )

                if (trip.savings > 0) {
                    Text(
                        text = "Saved ₹${trip.savings}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Manual Add Expense Dialog
 */
@Composable
fun ManualAddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (from: String, to: String, fare: String, isSmartCard: Boolean, isRoundTrip: Boolean) -> Unit
) {
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var fare by remember { mutableStateOf("40") }
    var isSmartCard by remember { mutableStateOf(true) }
    var isRoundTrip by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Metro Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = from,
                    onValueChange = { from = it },
                    label = { Text("From Station") },
                    placeholder = { Text("e.g. Rajiv Chowk") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text("To Station") },
                    placeholder = { Text("e.g. Noida Sec 18") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fare,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) fare = it },
                    label = { Text("One-Way Fare (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isSmartCard,
                        onCheckedChange = { isSmartCard = it }
                    )
                    Text("Paid with Smart Card (Discounted)", fontSize = 12.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isRoundTrip,
                        onCheckedChange = { isRoundTrip = it }
                    )
                    Text("Round Trip (Return journey today, 2x)", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(from, to, fare, isSmartCard, isRoundTrip) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Add Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

