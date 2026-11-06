package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.CustomerTransactionType
import com.example.core.model.PaymentMode
import com.example.core.util.Formatters
import com.example.database.entity.CustomerTransaction
import com.example.repository.DukaanRepository
import com.example.ui.DukaanViewModel
import com.example.ui.components.DukaanScreenshotTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterChipItem
import com.example.ui.components.StatusPill
import com.example.ui.theme.DukaanBorderLight
import com.example.ui.theme.DukaanFabGreen
import com.example.ui.theme.DukaanForestGreen
import com.example.ui.theme.DukaanGreenDark
import com.example.ui.theme.DukaanMintBg
import com.example.ui.theme.DukaanMintBorder
import com.example.ui.theme.DukaanMintText
import com.example.ui.theme.DukaanPinkBg
import com.example.ui.theme.DukaanPinkBorder
import com.example.ui.theme.DukaanRedLight
import com.example.ui.theme.DukaanRedText
import com.example.ui.theme.DukaanTextDark
import com.example.ui.theme.DukaanTextMuted

@Composable
fun CustomerListScreen(
    viewModel: DukaanViewModel,
    onCustomerClick: (Long) -> Unit
) {
    val customersWithBalances by viewModel.customersWithBalances.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf(
        "All",
        "You'll Get (Credit)",
        "You'll Give (Advance)",
        "Settled"
    )

    val filteredCustomers = remember(customersWithBalances, searchQuery, selectedFilterIndex) {
        customersWithBalances.filter { item ->
            val matchesSearch = item.customer.name.contains(searchQuery, ignoreCase = true) ||
                    item.customer.phone.contains(searchQuery)
            val matchesFilter = when (selectedFilterIndex) {
                1 -> item.netBalancePaise > 0   // You'll get (Credit)
                2 -> item.netBalancePaise < 0   // You'll give (Advance)
                3 -> item.netBalancePaise == 0L // Settled
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    val totalReceivable = remember(customersWithBalances) {
        customersWithBalances.sumOf { if (it.netBalancePaise > 0) it.netBalancePaise else 0L }
    }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Customers",
                subtitle = "${customersWithBalances.size} Customers • Receivable (You'll Get) " + Formatters.formatPaiseToRupees(totalReceivable, false),
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomerDialog = true },
                containerColor = DukaanFabGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_customer_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Customer",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("customer_list_screen")
        ) {
            // Search Input with rounded border (Screenshot 4 & 5)
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_search_input"),
                    placeholder = {
                        Text(
                            text = "Search customer by name or phone...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = DukaanTextMuted)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = DukaanTextMuted
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DukaanForestGreen,
                        unfocusedBorderColor = DukaanBorderLight,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // Filter Chips Row (Screenshot 4 & 5)
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEachIndexed { index, option ->
                    FilterChipItem(
                        label = option,
                        isSelected = selectedFilterIndex == index,
                        onClick = { selectedFilterIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer List
            if (filteredCustomers.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.PersonAdd,
                    title = "No Customers Found",
                    description = "Tap the + button below to add your first customer khata.",
                    buttonText = "+ Add Customer",
                    onButtonClick = { showAddCustomerDialog = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.customer.id }) { item ->
                        ScreenshotCustomerCard(
                            item = item,
                            onClick = { onCustomerClick(item.customer.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            isHindi = isHindi,
            onDismiss = { showAddCustomerDialog = false },
            onConfirm = { name, phone, address, notes, openingBalance ->
                viewModel.addCustomer(name, phone, address, notes, openingBalance)
                showAddCustomerDialog = false
            }
        )
    }
}

@Composable
fun ScreenshotCustomerCard(
    item: DukaanRepository.CustomerWithBalance,
    onClick: () -> Unit
) {
    val name = item.customer.name
    val initial = name.take(1).uppercase()
    val balance = item.netBalancePaise

    // Color avatars per screenshot
    val (avatarBg, avatarText) = when (initial) {
        "R" -> Color(0xFFD1F2D9) to Color(0xFF1B8755)
        "A" -> Color(0xFFEDE4FB) to Color(0xFF7E22CE)
        "S" -> Color(0xFFDDF4F8) to Color(0xFF00838F)
        else -> Color(0xFFE8F2EC) to DukaanForestGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("customer_item_${item.customer.id}"),
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
                        .size(44.dp)
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
                        text = name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DukaanTextDark,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "29 Oct 2026",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DukaanTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val amountStr = when {
                    balance > 0 -> Formatters.formatPaiseToRupees(balance, false)
                    balance < 0 -> Formatters.formatPaiseToRupees(-balance, false)
                    else -> "₹0"
                }

                val amountColor = when {
                    balance > 0 -> DukaanRedText
                    balance < 0 -> DukaanMintText
                    else -> DukaanTextDark
                }

                Text(
                    text = amountStr,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = amountColor,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                when {
                    balance > 0 -> StatusPill(
                        text = "You'll Get (Credit)",
                        bgColor = DukaanPinkBg,
                        textColor = DukaanRedText
                    )
                    balance < 0 -> StatusPill(
                        text = "You'll Give (Advance)",
                        bgColor = DukaanMintBg,
                        textColor = DukaanMintText
                    )
                    else -> StatusPill(
                        text = "Settled",
                        bgColor = Color(0xFFF3F4F6),
                        textColor = DukaanTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerDetailScreen(
    customerId: Long,
    viewModel: DukaanViewModel,
    onBack: () -> Unit
) {
    val customersWithBalances by viewModel.customersWithBalances.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()

    val customerItem = customersWithBalances.find { it.customer.id == customerId }
    val transactionsFlow = remember(customerId) { viewModel.repository.getCustomerTransactions(customerId) }
    val transactions by transactionsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddTxDialog by remember { mutableStateOf<CustomerTransactionType?>(null) }
    var txToReverse by remember { mutableStateOf<CustomerTransaction?>(null) }

    if (customerItem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading customer...")
        }
        return
    }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = customerItem.customer.name,
                subtitle = customerItem.customer.phone.ifBlank { "Customer Ledger" },
                onLanguageClick = { viewModel.toggleLanguage() },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showAddTxDialog = CustomerTransactionType.PAYMENT_RECEIVED },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("got_payment_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DukaanGreenDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "₹ You Got (Payment)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { showAddTxDialog = CustomerTransactionType.SALE_CREDIT },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("gave_credit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DukaanRedText),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "₹ You Gave (Credit)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("customer_ledger_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val balance = customerItem.netBalancePaise
                val isOwed = balance > 0
                val bgColor = if (isOwed) DukaanPinkBg else DukaanMintBg
                val borderColor = if (isOwed) DukaanPinkBorder else DukaanMintBorder
                val textColor = if (isOwed) DukaanRedText else DukaanMintText

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when {
                                balance > 0 -> "Total Receivable (You'll Get)"
                                balance < 0 -> "Total Advance (You'll Give)"
                                else -> "Account Settled"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Formatters.formatPaiseToRupees(if (balance < 0) -balance else balance, false),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            ),
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total Credit (Given)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DukaanTextMuted
                                )
                                Text(
                                    text = Formatters.formatPaiseToRupees(customerItem.totalDebitPaise, false),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DukaanRedText
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total Payments (Got)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DukaanTextMuted
                                )
                                Text(
                                    text = Formatters.formatPaiseToRupees(customerItem.totalCreditPaise, false),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DukaanGreenDark
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Restore,
                        title = "No transactions yet",
                        description = "Tap below to record credit or payment."
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionRow(
                        tx = tx,
                        isHindi = isHindi,
                        onReverseClick = { txToReverse = tx }
                    )
                }
            }
        }
    }

    showAddTxDialog?.let { type ->
        AddLedgerEntryDialog(
            type = type,
            isHindi = isHindi,
            onDismiss = { showAddTxDialog = null },
            onConfirm = { amountPaise, mode, note ->
                viewModel.addCustomerTransaction(
                    customerId = customerId,
                    type = type,
                    amountPaise = amountPaise,
                    paymentMode = mode,
                    note = note
                )
                showAddTxDialog = null
            }
        )
    }

    txToReverse?.let { tx ->
        var reversalReason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { txToReverse = null },
            title = { Text("Reverse Entry") },
            text = {
                Column {
                    Text("Enter audit reason for reversing this entry:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reversalReason,
                        onValueChange = { reversalReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reverseCustomerTransaction(tx, reversalReason.ifBlank { "Reversed entry" })
                        txToReverse = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DukaanRedText)
                ) {
                    Text("Reverse")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToReverse = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionRow(
    tx: CustomerTransaction,
    isHindi: Boolean,
    onReverseClick: () -> Unit
) {
    val isCredit = tx.type == CustomerTransactionType.PAYMENT_RECEIVED || tx.type == CustomerTransactionType.RETURN
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCredit) "Payment Got" else "Credit Given",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DukaanTextDark
                        )
                    )
                    Text(
                        text = Formatters.formatDate(tx.dateTimestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = DukaanTextMuted
                    )
                    if (tx.note.isNotBlank()) {
                        Text(
                            text = tx.note,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val amountColor = if (isCredit) DukaanGreenDark else DukaanRedText
                    Text(
                        text = (if (isCredit) "+ " else "- ") + Formatters.formatPaiseToRupees(tx.amountPaise, false),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                    )
                    Text(
                        text = tx.paymentMode.englishLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = DukaanTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
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
        title = { Text("Add Customer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Market") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = openingBalanceStr,
                    onValueChange = { openingBalanceStr = it },
                    label = { Text("Opening Balance (₹)") },
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
                },
                colors = ButtonDefaults.buttonColors(containerColor = DukaanForestGreen)
            ) {
                Text("Save")
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
fun AddLedgerEntryDialog(
    type: CustomerTransactionType,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long, PaymentMode, String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(PaymentMode.CASH) }

    val isGotPayment = type == CustomerTransactionType.PAYMENT_RECEIVED
    val title = if (isGotPayment) "Record Payment (You Got)" else "Record Credit (You Gave)"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    label = { Text("Note / Bill #") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountPaise = Formatters.parseRupeesToPaise(amountStr)
                    if (amountPaise > 0L) {
                        onConfirm(amountPaise, selectedMode, note.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGotPayment) DukaanGreenDark else DukaanRedText
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
