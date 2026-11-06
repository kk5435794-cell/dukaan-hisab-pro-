package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.PaymentMode
import com.example.core.model.SupplierTransactionType
import com.example.core.util.Formatters
import com.example.database.entity.SupplierTransaction
import com.example.repository.DukaanRepository
import com.example.ui.DukaanViewModel
import com.example.ui.components.DUKAAN_MORE_TABS
import com.example.ui.components.DukaanScreenshotTopBar
import com.example.ui.components.DukaanTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PastelMetricCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.DukaanCreamBg
import com.example.ui.theme.DukaanCreamBorder
import com.example.ui.theme.DukaanFabOrange
import com.example.ui.theme.DukaanGreenJama
import com.example.ui.theme.DukaanMintBg
import com.example.ui.theme.DukaanMintBorder
import com.example.ui.theme.DukaanMintText
import com.example.ui.theme.DukaanNavyPrimary
import com.example.ui.theme.DukaanOrangeText
import com.example.ui.theme.DukaanPinkBg
import com.example.ui.theme.DukaanPinkBorder
import com.example.ui.theme.DukaanRedLight
import com.example.ui.theme.DukaanRedText
import com.example.ui.theme.DukaanRedUdhaar
import com.example.ui.theme.DukaanTextDark
import com.example.ui.theme.DukaanTextMuted

@Composable
fun SupplierListScreen(
    viewModel: DukaanViewModel,
    onNavigateTab: (String) -> Unit,
    onSupplierClick: (Long) -> Unit
) {
    val suppliersWithBalances by viewModel.suppliersWithBalances.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddSupplierDialog by remember { mutableStateOf(false) }

    val filteredSuppliers = remember(suppliersWithBalances, searchQuery) {
        suppliersWithBalances.filter {
            it.supplier.name.contains(searchQuery, ignoreCase = true) ||
                    it.supplier.phone.contains(searchQuery)
        }
    }

    val totalPayableSum = remember(suppliersWithBalances) {
        suppliersWithBalances.sumOf { if (it.netPayablePaise > 0) it.netPayablePaise else 0L }
    }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Suppliers",
                subtitle = if (isHindi) "कुल सप्लायर देनदारी: " + Formatters.formatPaiseToRupees(totalPayableSum, false) else "Total Payables: " + Formatters.formatPaiseToRupees(totalPayableSum, false),
                topTabs = DUKAAN_MORE_TABS,
                activeTab = "Suppliers",
                onTabClick = onNavigateTab,
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSupplierDialog = true },
                containerColor = DukaanFabOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_supplier_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Add Supplier",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("supplier_list_screen")
        ) {
            // Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DukaanRedLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isHindi) "कुल सप्लायर देनदारी (देना बाकी)" else "Total Supplier Payables",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = DukaanRedUdhaar
                        )
                        Text(
                            text = Formatters.formatPaiseToRupees(totalPayableSum),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = DukaanRedUdhaar
                        )
                    }
                    Text(
                        text = "${suppliersWithBalances.size} " + if (isHindi) "सप्लायर" else "Suppliers",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = {
                    Text(if (isHindi) "सप्लायर का नाम या नंबर खोजें..." else "Search suppliers...")
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (filteredSuppliers.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.LocalShipping,
                    title = if (isHindi) "कोई सप्लायर नहीं मिला" else "No suppliers found",
                    description = if (isHindi) "दुकान के लिए सामान आपूर्ति करने वाले डीलरों को जोड़ें" else "Add your wholesale suppliers and distributors.",
                    buttonText = if (isHindi) "+ नया सप्लायर जोड़ें" else "+ Add Supplier",
                    onButtonClick = { showAddSupplierDialog = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSuppliers, key = { it.supplier.id }) { item ->
                        SupplierListItem(
                            item = item,
                            isHindi = isHindi,
                            onClick = { onSupplierClick(item.supplier.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddSupplierDialog) {
        AddSupplierDialog(
            isHindi = isHindi,
            onDismiss = { showAddSupplierDialog = false },
            onConfirm = { name, phone, address, notes, openingBalance ->
                viewModel.addSupplier(name, phone, address, notes, openingBalance)
                showAddSupplierDialog = false
            }
        )
    }
}

@Composable
fun SupplierListItem(
    item: DukaanRepository.SupplierWithBalance,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8EAF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = DukaanNavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.supplier.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    if (item.supplier.phone.isNotBlank()) {
                        Text(
                            text = item.supplier.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val balance = item.netPayablePaise
                if (balance > 0) {
                    Text(
                        text = Formatters.formatPaiseToRupees(balance),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DukaanRedUdhaar
                        )
                    )
                    Text(
                        text = if (isHindi) "देना बाकी (Payable)" else "Payable",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = DukaanRedUdhaar
                    )
                } else {
                    Text(
                        text = "₹0.00",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = if (isHindi) "खाता चुकता" else "Settled",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SupplierDetailScreen(
    supplierId: Long,
    viewModel: DukaanViewModel,
    onBack: () -> Unit
) {
    val suppliersWithBalances by viewModel.suppliersWithBalances.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()

    val supplierItem = suppliersWithBalances.find { it.supplier.id == supplierId }
    val transactionsFlow = remember(supplierId) { viewModel.repository.getSupplierTransactions(supplierId) }
    val transactions by transactionsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddTxDialog by remember { mutableStateOf<SupplierTransactionType?>(null) }

    if (supplierItem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (isHindi) "सप्लायर विवरण लोड हो रहा है..." else "Loading supplier...")
        }
        return
    }

    Scaffold(
        topBar = {
            DukaanTopBar(
                title = supplierItem.supplier.name,
                subtitle = supplierItem.supplier.phone.ifBlank { if (isHindi) "सप्लायर लेजर" else "Supplier Ledger" },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showAddTxDialog = SupplierTransactionType.PAYMENT_MADE },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DukaanGreenJama),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isHindi) "₹ भुगतान किया" else "Payment Made",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Button(
                        onClick = { showAddTxDialog = SupplierTransactionType.PURCHASE_CREDIT },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DukaanRedUdhaar),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isHindi) "+ माल खरीदा (उधार)" else "Purchase In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DukaanRedLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isHindi) "सप्लायर को कुल देना बाकी है" else "Total Payable to Supplier",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = DukaanRedUdhaar
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatPaiseToRupees(supplierItem.netPayablePaise),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            ),
                            color = DukaanRedUdhaar
                        )
                    }
                }
            }

            item {
                Text(
                    text = if (isHindi) "लेन-देन इतिहास (Purchase & Payments)" else "Transaction History",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Restore,
                        title = if (isHindi) "कोई प्रविष्टि नहीं है" else "No entries yet",
                        description = if (isHindi) "सप्लायर से माल खरीद या भुगतान दर्ज करें" else "Record purchase on credit or payments made."
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    val isPayment = tx.type == SupplierTransactionType.PAYMENT_MADE
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isHindi) tx.type.hindiLabel else tx.type.englishLabel,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = Formatters.formatDate(tx.dateTimestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (tx.note.isNotBlank()) {
                                    Text(
                                        text = tx.note,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = (if (isPayment) "- " else "+ ") + Formatters.formatPaiseToRupees(tx.amountPaise),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPayment) DukaanGreenJama else DukaanRedUdhaar
                                    )
                                )
                                Text(
                                    text = tx.paymentMode.hindiLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    showAddTxDialog?.let { type ->
        var amountStr by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        val isPayment = type == SupplierTransactionType.PAYMENT_MADE

        AlertDialog(
            onDismissRequest = { showAddTxDialog = null },
            title = {
                Text(if (isPayment) "सप्लायर को भुगतान (Payment Made)" else "उधार माल खरीद (Purchase Credit)")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text(if (isHindi) "राशि (₹) *" else "Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(if (isHindi) "बिल नंबर / नोट" else "Bill # / Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountPaise = Formatters.parseRupeesToPaise(amountStr)
                        if (amountPaise > 0L) {
                            viewModel.addSupplierTransaction(supplierId, type, amountPaise, PaymentMode.CASH, note)
                            showAddTxDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPayment) DukaanGreenJama else DukaanRedUdhaar
                    )
                ) {
                    Text(if (isHindi) "सुरक्षित करें" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTxDialog = null }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun AddSupplierDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var openingBalanceStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isHindi) "नया सप्लायर / डीलर जोड़ें" else "Add Supplier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isHindi) "सप्लायर / फर्म का नाम *" else "Supplier / Firm Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isHindi) "मोबाइल नंबर" else "Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isHindi) "शहर / मंडी का पता" else "Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = openingBalanceStr,
                    onValueChange = { openingBalanceStr = it },
                    label = { Text(if (isHindi) "शुरुआती देनदारी (₹)" else "Opening Balance (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val openingPaise = Formatters.parseRupeesToPaise(openingBalanceStr)
                        onConfirm(name.trim(), phone.trim(), address.trim(), "", openingPaise)
                    }
                }
            ) {
                Text(if (isHindi) "सुरक्षित करें" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel")
            }
        }
    )
}

@Composable
fun PurchasesScreen(
    viewModel: DukaanViewModel,
    onNavigateTab: (String) -> Unit
) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val suppliersWithBalances by viewModel.suppliersWithBalances.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<SupplierTransaction?>(null) }

    val totalPurchasesSum = remember(purchases) { purchases.sumOf { it.amountPaise } }
    val totalPayableSum = remember(suppliersWithBalances) {
        suppliersWithBalances.sumOf { if (it.netPayablePaise > 0) it.netPayablePaise else 0L }
    }

    val filteredPurchases = remember(purchases, searchQuery) {
        if (searchQuery.isBlank()) purchases
        else purchases.filter { it.note.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Purchases",
                subtitle = "${purchases.size} " + if (isHindi) "खरीद बिल सूची" else "Purchases List",
                topTabs = DUKAAN_MORE_TABS,
                activeTab = "Purchases",
                onTabClick = onNavigateTab,
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DukaanFabOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_purchase_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Purchase",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("purchases_screen")
        ) {
            // Top 2 Metric Cards: Purchase Total & Balance Payable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PastelMetricCard(
                    title = if (isHindi) "कुल माल खरीदी" else "Purchase Total",
                    amountStr = Formatters.formatPaiseToRupees(totalPurchasesSum, false),
                    bgColor = DukaanCreamBg,
                    borderColor = DukaanCreamBorder,
                    amountColor = DukaanOrangeText,
                    modifier = Modifier.weight(1f)
                )

                PastelMetricCard(
                    title = if (isHindi) "देना बाकी (Payable)" else "Balance Payable",
                    amountStr = Formatters.formatPaiseToRupees(totalPayableSum, false),
                    bgColor = DukaanPinkBg,
                    borderColor = DukaanPinkBorder,
                    amountColor = DukaanRedText,
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = {
                    Text(if (isHindi) "खरीद बिल या सप्लायर खोजें..." else "Search purchase bill / supplier...")
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = DukaanTextMuted)
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredPurchases.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.LocalShipping,
                    title = if (isHindi) "कोई खरीद बिल दर्ज नहीं है" else "No purchases yet",
                    description = if (isHindi) "+ बटन दबाकर नई खरीद बिल प्रविष्टि दर्ज करें" else "Tap + to add a new purchase invoice."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPurchases, key = { it.id }) { purchase ->
                        val isPaid = purchase.note.contains("पूर्ण भुगतान", ignoreCase = true) || purchase.note.contains("नकद भुगतान", ignoreCase = true)
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (purchase.note.contains("#PUR-")) {
                                                purchase.note.substringAfter("#").substringBefore(" ").substringBefore(":")
                                            } else {
                                                "PUR-${purchase.id + 91100}"
                                            },
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DukaanTextDark,
                                                fontSize = 15.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StatusPill(
                                            label = if (isPaid) "पूर्ण भुगतान" else "उधार (Credit)",
                                            isGreen = isPaid
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = Formatters.formatDate(purchase.dateTimestamp),
                                        style = MaterialTheme.typography.bodySmall.copy(color = DukaanTextMuted)
                                    )
                                    if (purchase.note.isNotBlank()) {
                                        Text(
                                            text = purchase.note,
                                            style = MaterialTheme.typography.bodySmall.copy(color = DukaanTextDark)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "- " + Formatters.formatPaiseToRupees(purchase.amountPaise, false),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DukaanRedText,
                                            fontSize = 16.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(onClick = { itemToDelete = purchase }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = DukaanTextMuted,
                                            modifier = Modifier.size(20.dp)
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

    if (showAddDialog) {
        AddPurchaseDialog(
            isHindi = isHindi,
            onDismiss = { showAddDialog = false },
            onConfirm = { suppName, suppPhone, invNo, totalPaise, paidPaise, mode, note ->
                viewModel.addPurchase(suppName, suppPhone, invNo, totalPaise, paidPaise, mode, note)
                showAddDialog = false
            }
        )
    }

    itemToDelete?.let { purchase ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(if (isHindi) "खरीद हटाएं?" else "Delete Purchase?") },
            text = { Text(if (isHindi) "क्या आप इस खरीद प्रविष्टि को हटाना चाहते हैं?" else "Delete this purchase record?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePurchase(purchase.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(if (isHindi) "हटाएं" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun AddPurchaseDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long, Long, PaymentMode, String) -> Unit
) {
    var supplierName by remember { mutableStateOf("Raju") }
    var supplierPhone by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("PUR-${(10000..99999).random()}") }
    var totalAmountStr by remember { mutableStateOf("") }
    var paidAmountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isHindi) "+ नई खरीद बिल दर्ज करें" else "+ Add Purchase Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text(if (isHindi) "सप्लायर का नाम *" else "Supplier Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("Bill / Invoice No.") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = totalAmountStr,
                    onValueChange = {
                        totalAmountStr = it
                        paidAmountStr = it // default full paid
                    },
                    label = { Text(if (isHindi) "कुल खरीद राशि (₹) *" else "Total Purchase Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = paidAmountStr,
                    onValueChange = { paidAmountStr = it },
                    label = { Text(if (isHindi) "भुगतान की गई राशि (₹)" else "Amount Paid (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isHindi) "सामान / नोट विवरण" else "Description / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val totalPaise = Formatters.parseRupeesToPaise(totalAmountStr)
                    val paidPaise = Formatters.parseRupeesToPaise(paidAmountStr)
                    if (totalPaise > 0L) {
                        onConfirm(
                            supplierName.trim(),
                            supplierPhone.trim(),
                            invoiceNumber.trim(),
                            totalPaise,
                            paidPaise,
                            PaymentMode.CASH,
                            note.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DukaanFabOrange)
            ) {
                Text(if (isHindi) "खरीद सहेजें" else "Save Purchase")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel")
            }
        }
    )
}
