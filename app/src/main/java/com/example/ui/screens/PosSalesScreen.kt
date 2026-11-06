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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.engine.AccountingEngine
import com.example.core.model.InvoiceStatus
import com.example.core.model.PaymentMode
import com.example.core.util.Formatters
import com.example.database.entity.SalesInvoice
import com.example.repository.DukaanRepository
import com.example.ui.DukaanViewModel
import com.example.ui.components.DukaanScreenshotTopBar
import com.example.ui.components.FilterChipItem
import com.example.ui.components.PastelMetricCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.DukaanBlueBg
import com.example.ui.theme.DukaanBorderLight
import com.example.ui.theme.DukaanCreamBg
import com.example.ui.theme.DukaanCreamBorder
import com.example.ui.theme.DukaanFabGreen
import com.example.ui.theme.DukaanForestGreen
import com.example.ui.theme.DukaanGreenDark
import com.example.ui.theme.DukaanMintBg
import com.example.ui.theme.DukaanMintBorder
import com.example.ui.theme.DukaanMintText
import com.example.ui.theme.DukaanPinkBg
import com.example.ui.theme.DukaanPinkBorder
import com.example.ui.theme.DukaanRedText
import com.example.ui.theme.DukaanTextDark
import com.example.ui.theme.DukaanTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosSalesScreen(
    viewModel: DukaanViewModel,
    onNavigateToInvoice: (Long) -> Unit
) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val productsWithStock by viewModel.productsWithStock.collectAsStateWithLifecycle()
    val customersWithBalances by viewModel.customersWithBalances.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()

    var showCreateInvoiceDialog by remember { mutableStateOf(false) }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    val filterOptions = listOf("All", "Fully Paid", "Partially Paid", "Unpaid / Credit")

    val totalSalesPaise = remember(invoices) { invoices.sumOf { it.grandTotalPaise } }
    val totalBalanceDuePaise = remember(invoices) { invoices.sumOf { it.balanceDuePaise } }

    val filteredInvoices = remember(invoices, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> invoices.filter { it.status == InvoiceStatus.PAID }
            2 -> invoices.filter { it.status == InvoiceStatus.PARTIALLY_PAID }
            3 -> invoices.filter { it.status == InvoiceStatus.UNPAID }
            else -> invoices
        }
    }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Sales & Bills",
                subtitle = "${invoices.size} Invoices List",
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateInvoiceDialog = true },
                containerColor = DukaanFabGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("pos_checkout_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Bill",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("pos_sales_screen")
        ) {
            // Top 2 Pastel Summary Cards (Screenshot 6 & 7)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PastelMetricCard(
                    title = "Total Sales",
                    amountStr = Formatters.formatPaiseToRupees(totalSalesPaise, false),
                    bgColor = Color.White,
                    borderColor = DukaanBorderLight,
                    amountColor = DukaanGreenDark,
                    modifier = Modifier.weight(1f)
                )

                PastelMetricCard(
                    title = "Balance Due",
                    amountStr = Formatters.formatPaiseToRupees(totalBalanceDuePaise, false),
                    bgColor = DukaanPinkBg,
                    borderColor = DukaanPinkBorder,
                    amountColor = DukaanRedText,
                    modifier = Modifier.weight(1f)
                )
            }

            // Filter Chips Row (Screenshot 6 & 7)
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp),
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

            Spacer(modifier = Modifier.height(10.dp))

            // Invoice Cards List (Screenshots 6 & 7)
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredInvoices, key = { it.id }) { invoice ->
                    InvoiceScreenshotCard(
                        invoice = invoice,
                        onClick = { onNavigateToInvoice(invoice.id) }
                    )
                }
            }
        }
    }

    if (showCreateInvoiceDialog) {
        CreateInvoiceModal(
            viewModel = viewModel,
            products = productsWithStock,
            customers = customersWithBalances,
            onDismiss = { showCreateInvoiceDialog = false },
            onInvoiceCreated = { invoiceId ->
                showCreateInvoiceDialog = false
                onNavigateToInvoice(invoiceId)
            }
        )
    }
}

@Composable
fun InvoiceScreenshotCard(
    invoice: SalesInvoice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: INV Number + Status Badge on left, Date on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = invoice.invoiceNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DukaanForestGreen,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusPill(
                        text = if (invoice.status == InvoiceStatus.PAID) "Fully Paid"
                        else if (invoice.status == InvoiceStatus.PARTIALLY_PAID) "Partial" else "Unpaid",
                        bgColor = if (invoice.status == InvoiceStatus.PAID) DukaanMintBg else DukaanPinkBg,
                        textColor = if (invoice.status == InvoiceStatus.PAID) DukaanGreenDark else DukaanRedText
                    )
                }

                Text(
                    text = "29 Oct 2026",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DukaanTextMuted,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Row: Customer Name on left, Amount on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.customerName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DukaanTextDark,
                        fontSize = 15.sp
                    )
                )

                Text(
                    text = Formatters.formatPaiseToRupees(invoice.grandTotalPaise, false),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DukaanTextDark,
                        fontSize = 17.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Right Action Badges: PDF badge & Trash icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DukaanForestGreen)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PDF",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceModal(
    viewModel: DukaanViewModel,
    products: List<DukaanRepository.ProductWithStock>,
    customers: List<DukaanRepository.CustomerWithBalance>,
    onDismiss: () -> Unit,
    onInvoiceCreated: (Long) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<DukaanRepository.CustomerWithBalance?>(null) }
    var customerDropdown by remember { mutableStateOf(false) }
    var amountPaidStr by remember { mutableStateOf("") }
    var productSearch by remember { mutableStateOf("") }

    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val subtotal = cart.sumOf { it.lineTotalPaise }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Sale Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Customer selector
                ExposedDropdownMenuBox(
                    expanded = customerDropdown,
                    onExpandedChange = { customerDropdown = !customerDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.customer?.name ?: "Cash Walk-in (नकद ग्राहक)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Customer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = customerDropdown,
                        onDismissRequest = { customerDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cash Walk-in") },
                            onClick = {
                                selectedCustomer = null
                                customerDropdown = false
                            }
                        )
                        customers.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.customer.name} (${c.customer.phone})") },
                                onClick = {
                                    selectedCustomer = c
                                    customerDropdown = false
                                }
                            )
                        }
                    }
                }

                // Add Items Search & List
                Text("Tap items to add to bill:", style = MaterialTheme.typography.labelSmall)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(products.take(6)) { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF3F4F6))
                                .clickable { viewModel.addToCart(p.product) }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.product.name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(
                                text = Formatters.formatPaiseToRupees(p.product.sellingPricePaise, false),
                                fontWeight = FontWeight.Bold,
                                color = DukaanForestGreen
                            )
                        }
                    }
                }

                // Bill Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Amount:", fontWeight = FontWeight.Bold)
                    Text(
                        Formatters.formatPaiseToRupees(subtotal, false),
                        fontWeight = FontWeight.Bold,
                        color = DukaanForestGreen,
                        fontSize = 18.sp
                    )
                }

                OutlinedTextField(
                    value = amountPaidStr,
                    onValueChange = { amountPaidStr = it },
                    label = { Text("Amount Paid (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paid = Formatters.parseRupeesToPaise(amountPaidStr.ifBlank { (subtotal / 100).toString() })
                    viewModel.selectPosCustomer(
                        selectedCustomer?.customer?.id,
                        selectedCustomer?.customer?.name ?: "Cash Walk-in"
                    )
                    viewModel.setPosAmountPaid(paid)
                    viewModel.completeSale(onInvoiceCreated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DukaanForestGreen)
            ) {
                Text("Generate Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
