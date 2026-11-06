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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.core.util.Formatters
import com.example.repository.DukaanRepository
import com.example.ui.DukaanViewModel
import com.example.ui.components.DUKAAN_MORE_TABS
import com.example.ui.components.DukaanScreenshotTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.DukaanFabOrange
import com.example.ui.theme.DukaanGreenJama
import com.example.ui.theme.DukaanNavyPrimary
import com.example.ui.theme.DukaanOrangeWarning
import com.example.ui.theme.DukaanRedLight
import com.example.ui.theme.DukaanRedUdhaar

@Composable
fun ProductListScreen(
    viewModel: DukaanViewModel,
    onNavigateTab: (String) -> Unit
) {
    val productsWithStock by viewModel.productsWithStock.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var filterLowStockOnly by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToAdjust by remember { mutableStateOf<DukaanRepository.ProductWithStock?>(null) }

    val filteredProducts = remember(productsWithStock, searchQuery, filterLowStockOnly) {
        productsWithStock.filter {
            val matchesSearch = it.product.name.contains(searchQuery, ignoreCase = true) ||
                    it.product.skuOrBarcode.contains(searchQuery, ignoreCase = true) ||
                    it.product.category.contains(searchQuery, ignoreCase = true)
            val matchesLowStock = if (filterLowStockOnly) it.currentStock <= it.product.minStockThreshold else true
            matchesSearch && matchesLowStock
        }
    }

    val totalStockValuation = remember(productsWithStock) {
        productsWithStock.sumOf { it.stockValuePaise }
    }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Products & Stock",
                subtitle = if (isHindi) "स्टॉक मूल्यांकन: " + Formatters.formatPaiseToRupees(totalStockValuation, false) else "Stock Valuation: " + Formatters.formatPaiseToRupees(totalStockValuation, false),
                topTabs = DUKAAN_MORE_TABS,
                activeTab = "Products & Stock",
                onTabClick = onNavigateTab,
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProductDialog = true },
                containerColor = DukaanFabOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_product_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("product_list_screen")
        ) {
            // Stock Valuation Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
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
                            text = if (isHindi) "कुल दुकान स्टॉक मूल्यांकन (लागत मूल्य पर)" else "Total Stock Valuation (At Cost)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = DukaanNavyPrimary
                        )
                        Text(
                            text = Formatters.formatPaiseToRupees(totalStockValuation),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = DukaanNavyPrimary
                        )
                    }
                    Text(
                        text = "${productsWithStock.size} " + if (isHindi) "प्रॉडक्ट्स" else "Products",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar & Filter Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(if (isHindi) "सामान खोजें..." else "Search items...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = { filterLowStockOnly = !filterLowStockOnly },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (filterLowStockOnly) DukaanRedLight else Color.Transparent
                    )
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (filterLowStockOnly) DukaanRedUdhaar else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHindi) "कम स्टॉक" else "Low Stock",
                        color = if (filterLowStockOnly) DukaanRedUdhaar else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Inventory,
                    title = if (isHindi) "कोई सामान नहीं मिला" else "No items found",
                    description = if (isHindi) "नया सामान जोड़ने के लिए नीचे दिए बटन पर टैप करें" else "Tap below to add items to your shop stock.",
                    buttonText = if (isHindi) "+ नया सामान जोड़ें" else "+ Add Item",
                    onButtonClick = { showAddProductDialog = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts, key = { it.product.id }) { item ->
                        ProductStockItem(
                            item = item,
                            isHindi = isHindi,
                            onAdjustStock = { productToAdjust = item }
                        )
                    }
                }
            }
        }
    }

    if (showAddProductDialog) {
        AddProductDialog(
            isHindi = isHindi,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { name, sku, category, costPaise, sellingPaise, openingQty, minThreshold, unit ->
                viewModel.addProduct(name, sku, category, costPaise, sellingPaise, openingQty, minThreshold, unit)
                showAddProductDialog = false
            }
        )
    }

    productToAdjust?.let { pWithStock ->
        StockAdjustmentDialog(
            product = pWithStock,
            isHindi = isHindi,
            onDismiss = { productToAdjust = null },
            onConfirm = { adjustmentQty, reason ->
                viewModel.adjustStock(pWithStock.product.id, adjustmentQty, reason)
                productToAdjust = null
            }
        )
    }
}

@Composable
fun ProductStockItem(
    item: DukaanRepository.ProductWithStock,
    isHindi: Boolean,
    onAdjustStock: () -> Unit
) {
    val isLowStock = item.currentStock <= item.product.minStockThreshold

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${item.product.category} • " +
                                (if (isHindi) "बिक्री: " else "Sell: ") +
                                Formatters.formatPaiseToRupees(item.product.sellingPricePaise) +
                                (if (item.product.costPricePaise > 0) " (लागत: ${Formatters.formatPaiseToRupees(item.product.costPricePaise)})" else ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Formatters.formatQuantity(item.currentStock, item.product.unit),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isLowStock) DukaanRedUdhaar else DukaanNavyPrimary
                            )
                        )
                        if (isLowStock) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Low stock",
                                tint = DukaanRedUdhaar,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isHindi) "स्टॉक सुधारें" else "Adjust Stock",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .clickable { onAdjustStock() }
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long, Long, Double, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("किराना (Grocery)") }
    var costPriceStr by remember { mutableStateOf("") }
    var sellingPriceStr by remember { mutableStateOf("") }
    var openingStockStr by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Pcs") }

    val units = listOf("Pcs", "Packet", "Box", "Kg", "Gram", "Litre", "Dozen")
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isHindi) "दुकान का नया सामान जोड़ें" else "Add New Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isHindi) "सामान का नाम *" else "Product Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it },
                        label = { Text(if (isHindi) "बिक्री मूल्य (₹) *" else "Sell Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it },
                        label = { Text(if (isHindi) "खरीद लागत (₹)" else "Cost Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = openingStockStr,
                        onValueChange = { openingStockStr = it },
                        label = { Text(if (isHindi) "मौजूदा स्टॉक मात्रा" else "Initial Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded,
                        onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isHindi) "इकाई" else "Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && sellingPriceStr.isNotBlank()) {
                        val sellPaise = Formatters.parseRupeesToPaise(sellingPriceStr)
                        val costPaise = Formatters.parseRupeesToPaise(costPriceStr)
                        val qty = openingStockStr.toDoubleOrNull() ?: 0.0
                        onConfirm(name.trim(), sku.trim(), category, costPaise, sellPaise, qty, 5.0, unit)
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
fun StockAdjustmentDialog(
    product: DukaanRepository.ProductWithStock,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var adjustmentQtyStr by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) } // true = Add, false = Remove/Damaged
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isHindi) "स्टॉक सुधार: ${product.product.name}" else "Adjust Stock: ${product.product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${if (isHindi) "वर्तमान स्टॉक:" else "Current stock:"} ${Formatters.formatQuantity(product.currentStock, product.product.unit)}",
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isAddition = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isAddition) DukaanGreenJama.copy(alpha = 0.15f) else Color.Transparent
                        )
                    ) {
                        Text(if (isHindi) "+ स्टॉक बढ़ाएं" else "+ Add Stock")
                    }

                    OutlinedButton(
                        onClick = { isAddition = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isAddition) DukaanRedLight else Color.Transparent
                        )
                    ) {
                        Text(if (isHindi) "- कम करें / नुकसान" else "- Remove / Damaged")
                    }
                }

                OutlinedTextField(
                    value = adjustmentQtyStr,
                    onValueChange = { adjustmentQtyStr = it },
                    label = { Text(if (isHindi) "मात्रा (${product.product.unit}) *" else "Quantity (${product.product.unit}) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(if (isHindi) "सुधार का कारण (उदा. नया माल / टूट-फूट) *" else "Reason *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawQty = adjustmentQtyStr.toDoubleOrNull() ?: 0.0
                    if (rawQty > 0.0 && reason.isNotBlank()) {
                        val finalQty = if (isAddition) rawQty else -rawQty
                        onConfirm(finalQty, reason.trim())
                    }
                }
            ) {
                Text(if (isHindi) "सुधार दर्ज करें" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel")
            }
        }
    )
}
