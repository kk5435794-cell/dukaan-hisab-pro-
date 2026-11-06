package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.engine.AccountingEngine
import com.example.core.model.PaymentMode
import com.example.core.util.Formatters
import com.example.database.entity.CashbookEntry
import com.example.database.entity.Expense
import com.example.ui.DukaanViewModel
import com.example.ui.components.DUKAAN_MORE_TABS
import com.example.ui.components.DukaanScreenshotTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterChipItem
import com.example.ui.components.PastelMetricCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.DukaanCreamBg
import com.example.ui.theme.DukaanCreamBorder
import com.example.ui.theme.DukaanFabGreen
import com.example.ui.theme.DukaanFabOrange
import com.example.ui.theme.DukaanForestGreen
import com.example.ui.theme.DukaanGreenDark
import com.example.ui.theme.DukaanLavenderBg
import com.example.ui.theme.DukaanLavenderBorder
import com.example.ui.theme.DukaanLavenderText
import com.example.ui.theme.DukaanMintBg
import com.example.ui.theme.DukaanMintBorder
import com.example.ui.theme.DukaanMintText
import com.example.ui.theme.DukaanOrangeText
import com.example.ui.theme.DukaanPinkBg
import com.example.ui.theme.DukaanPinkBorder
import com.example.ui.theme.DukaanRedText
import com.example.ui.theme.DukaanTextDark
import com.example.ui.theme.DukaanTextMuted

@Composable
fun ExpensesScreen(viewModel: DukaanViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    val categories = listOf(
        "All",
        "Shop Rent",
        "Tea / Refreshments",
        "Electricity",
        "Freight / Transport",
        "Staff Salary",
        "Repairs / Maintenance",
        "Stock Purchase",
        "Packaging & Bags",
        "Marketing & Ads",
        "Miscellaneous"
    )

    val totalExpensesSum = remember(expenses) { expenses.sumOf { it.amountPaise } }

    val filteredExpenses = remember(expenses, selectedFilterIndex) {
        if (selectedFilterIndex == 0) expenses
        else {
            val selectedCat = categories[selectedFilterIndex]
            expenses.filter { it.category.contains(selectedCat, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Expenses",
                subtitle = "Monthly Expenses: " + Formatters.formatPaiseToRupees(totalExpensesSum, false),
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DukaanFabOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top 2 Pastel Summary Cards (Screenshots 8, 9, 10)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PastelMetricCard(
                    title = "आज का खर्च (Today)",
                    amountStr = "₹0",
                    bgColor = DukaanCreamBg,
                    borderColor = DukaanCreamBorder,
                    amountColor = DukaanOrangeText,
                    modifier = Modifier.weight(1f)
                )

                PastelMetricCard(
                    title = "Monthly Expenses",
                    amountStr = Formatters.formatPaiseToRupees(totalExpensesSum, false),
                    bgColor = DukaanLavenderBg,
                    borderColor = DukaanLavenderBorder,
                    amountColor = DukaanLavenderText,
                    modifier = Modifier.weight(1f)
                )
            }

            // Horizontal Filter Chips Row
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEachIndexed { index, cat ->
                    FilterChipItem(
                        label = cat,
                        isSelected = selectedFilterIndex == index,
                        onClick = { selectedFilterIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredExpenses.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Delete,
                    title = if (isHindi) "कोई खर्चा दर्ज नहीं है" else "No expenses yet",
                    description = if (isHindi) "+ बटन दबाकर नया खर्चा जोड़ें" else "Tap + to add an expense."
                )
            } else {
                // Expenses List
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseScreenshotCard(
                            expense = expense,
                            onDelete = { viewModel.deleteExpense(expense.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            isHindi = isHindi,
            onDismiss = { showAddDialog = false },
            onConfirm = { category, amountPaise, mode, payee, note ->
                viewModel.addExpense(category, amountPaise, mode, payee, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExpenseScreenshotCard(
    expense: Expense,
    onDelete: () -> Unit
) {
    val initial = expense.category.take(1).uppercase()
    val (avatarBg, avatarText) = when (initial) {
        "M" -> Color(0xFFE8F0FE) to Color(0xFF1967D2)
        "P" -> Color(0xFFEDE4FB) to Color(0xFF7E22CE)
        "S" -> Color(0xFFFFF9C4) to Color(0xFFF57F17)
        "R" -> Color(0xFFFDE8E8) to Color(0xFFD32F2F)
        else -> Color(0xFFE8F8EF) to Color(0xFF137A47)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontWeight = FontWeight.Bold,
                        color = avatarText,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DukaanTextDark,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Formatters.formatDate(expense.dateTimestamp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DukaanTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Formatters.formatPaiseToRupees(expense.amountPaise, false),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DukaanRedText,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = DukaanTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, PaymentMode, String, String) -> Unit
) {
    var category by remember { mutableStateOf("Miscellaneous") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Description / Note") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountPaise = Formatters.parseRupeesToPaise(amountStr)
                    if (amountPaise > 0L) {
                        onConfirm(category.trim(), amountPaise, PaymentMode.CASH, "", note.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DukaanFabOrange)
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CashbookScreen(
    viewModel: DukaanViewModel,
    onNavigateTab: (String) -> Unit
) {
    val entries by viewModel.cashbookEntries.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()
    var showAddCashDialog by remember { mutableStateOf(false) }

    val currentCashBalance = remember(entries) {
        val txs = entries.map { AccountingEngine.CashTx(it.isInflow, it.amountPaise) }
        AccountingEngine.calculateCashBalance(0L, txs)
    }

    val totalInflow = remember(entries) {
        entries.filter { it.isInflow }.sumOf { it.amountPaise }
    }
    val totalOutflow = remember(entries) {
        entries.filter { !it.isInflow }.sumOf { it.amountPaise }
    }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Cashbook",
                subtitle = "Cash in Hand: " + Formatters.formatPaiseToRupees(currentCashBalance, false),
                topTabs = DUKAAN_MORE_TABS,
                activeTab = "Cashbook",
                onTabClick = onNavigateTab,
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCashDialog = true },
                containerColor = DukaanFabGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Cash Entry",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Large Top Cash in Hand Card (Screenshot 12)
            Box(modifier = Modifier.padding(16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DukaanPinkBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DukaanPinkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Cash in Hand",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DukaanTextDark,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Formatters.formatPaiseToRupees(currentCashBalance, false),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DukaanRedText,
                                fontSize = 28.sp
                            )
                        )
                    }
                }
            }

            // Two side-by-side Inflow & Outflow Cards (Screenshot 12)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PastelMetricCard(
                    title = "Cash Receipts (Inflow)",
                    amountStr = Formatters.formatPaiseToRupees(totalInflow, false),
                    bgColor = DukaanMintBg,
                    borderColor = DukaanMintBorder,
                    amountColor = DukaanMintText,
                    modifier = Modifier.weight(1f)
                )

                PastelMetricCard(
                    title = "Cash Payments (Outflow)",
                    amountStr = Formatters.formatPaiseToRupees(totalOutflow, false),
                    bgColor = DukaanPinkBg,
                    borderColor = DukaanPinkBorder,
                    amountColor = DukaanRedText,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (entries.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.AttachMoney,
                    title = if (isHindi) "कोई रोकड़ प्रविष्टि नहीं है" else "No cash entries yet",
                    description = if (isHindi) "+ बटन दबाकर नकद आवक या जावक प्रविष्टि दर्ज करें" else "Tap + to add cash inflow or outflow."
                )
            } else {
                // Dynamic Cashbook entries list matching screenshot
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        CashbookScreenshotRow(
                            isOut = !entry.isInflow,
                            title = entry.description.ifBlank { entry.category },
                            dateStr = Formatters.formatDate(entry.dateTimestamp),
                            amountStr = (if (entry.isInflow) "+ " else "- ") + Formatters.formatPaiseToRupees(entry.amountPaise, false),
                            onDelete = { viewModel.deleteCashEntry(entry.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddCashDialog) {
        var isInflow by remember { mutableStateOf(true) }
        var category by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCashDialog = false },
            title = { Text("Add Cashbook Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isInflow = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isInflow) DukaanMintBg else Color.Transparent
                            )
                        ) {
                            Text("+ Inflow (IN)", color = DukaanGreenDark)
                        }
                        OutlinedButton(
                            onClick = { isInflow = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (!isInflow) DukaanPinkBg else Color.Transparent
                            )
                        ) {
                            Text("- Outflow (OUT)", color = DukaanRedText)
                        }
                    }

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category / Description *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountPaise = Formatters.parseRupeesToPaise(amountStr)
                        if (amountPaise > 0L && category.isNotBlank()) {
                            viewModel.addCashEntry(isInflow, category.trim(), amountPaise, "")
                            showAddCashDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DukaanForestGreen)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CashbookScreenshotRow(
    isOut: Boolean,
    title: String,
    dateStr: String,
    amountStr: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isOut) DukaanPinkBg else DukaanMintBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isOut) "OUT" else "IN",
                        color = if (isOut) DukaanRedText else DukaanMintText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DukaanTextDark,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DukaanTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = amountStr,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isOut) DukaanRedText else DukaanGreenDark,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = DukaanTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
