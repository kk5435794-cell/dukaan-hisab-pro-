package com.example.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.BackupScreen
import com.example.ui.screens.CashbookScreen
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomerListScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.InvoiceDetailScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PosSalesScreen
import com.example.ui.screens.ProductListScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SupplierDetailScreen
import com.example.ui.screens.SupplierListScreen
import com.example.ui.theme.DukaanForestGreen
import com.example.ui.theme.DukaanNavBg
import com.example.ui.theme.DukaanTextDark
import com.example.ui.theme.DukaanTextMuted

enum class AppScreen {
    ONBOARDING,
    DASHBOARD,
    CUSTOMERS,
    CUSTOMER_DETAIL,
    SALES_AND_BILLS,
    INVOICE_DETAIL,
    EXPENSES,
    MORE,
    STOCK,
    SUPPLIERS,
    SUPPLIER_DETAIL,
    PURCHASES,
    CASHBOOK,
    REPORTS,
    BACKUP,
    SETTINGS
}

@Composable
fun MainAppNavigation(viewModel: DukaanViewModel) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var selectedSupplierId by remember { mutableStateOf<Long?>(null) }
    var selectedInvoiceId by remember { mutableStateOf<Long?>(null) }

    val showOnboarding = profile != null && !profile!!.isSetupCompleted && currentScreen == AppScreen.ONBOARDING

    if (showOnboarding || currentScreen == AppScreen.ONBOARDING) {
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = { currentScreen = AppScreen.DASHBOARD }
        )
        return
    }

    androidx.activity.compose.BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        when (currentScreen) {
            AppScreen.CUSTOMER_DETAIL -> currentScreen = AppScreen.CUSTOMERS
            AppScreen.SUPPLIER_DETAIL -> currentScreen = AppScreen.SUPPLIERS
            AppScreen.INVOICE_DETAIL -> currentScreen = AppScreen.SALES_AND_BILLS
            AppScreen.STOCK, AppScreen.SUPPLIERS, AppScreen.PURCHASES, AppScreen.CASHBOOK, AppScreen.REPORTS, AppScreen.BACKUP, AppScreen.SETTINGS, AppScreen.MORE ->
                currentScreen = AppScreen.DASHBOARD
            else -> currentScreen = AppScreen.DASHBOARD
        }
    }

    val onTopTabNavigate: (String) -> Unit = { tab ->
        when (tab) {
            "Suppliers" -> currentScreen = AppScreen.SUPPLIERS
            "Products & Stock" -> currentScreen = AppScreen.STOCK
            "Purchases" -> currentScreen = AppScreen.PURCHASES
            "Cashbook" -> currentScreen = AppScreen.CASHBOOK
            "Reports" -> currentScreen = AppScreen.REPORTS
            "Backup" -> currentScreen = AppScreen.BACKUP
            "Settings" -> currentScreen = AppScreen.SETTINGS
        }
    }

    Scaffold(
        bottomBar = {
            val showBottomNav = currentScreen in listOf(
                AppScreen.DASHBOARD,
                AppScreen.CUSTOMERS,
                AppScreen.SALES_AND_BILLS,
                AppScreen.EXPENSES,
                AppScreen.MORE,
                AppScreen.STOCK,
                AppScreen.SUPPLIERS,
                AppScreen.PURCHASES,
                AppScreen.CASHBOOK,
                AppScreen.REPORTS,
                AppScreen.BACKUP,
                AppScreen.SETTINGS
            )
            if (showBottomNav) {
                // Exact 5 Bottom Nav Items from screenshots:
                // [Dashboard] [Customers] [Sales & Bills] [Expenses] [More]
                NavigationBar(
                    containerColor = DukaanNavBg,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DASHBOARD,
                        onClick = { currentScreen = AppScreen.DASHBOARD },
                        icon = { Icon(Icons.Default.GridView, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DukaanForestGreen,
                            selectedTextColor = DukaanForestGreen,
                            indicatorColor = Color(0xFFCDE4D6),
                            unselectedIconColor = DukaanTextMuted,
                            unselectedTextColor = DukaanTextMuted
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.CUSTOMERS || currentScreen == AppScreen.CUSTOMER_DETAIL,
                        onClick = { currentScreen = AppScreen.CUSTOMERS },
                        icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
                        label = { Text("Customers", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DukaanForestGreen,
                            selectedTextColor = DukaanForestGreen,
                            indicatorColor = Color(0xFFCDE4D6),
                            unselectedIconColor = DukaanTextMuted,
                            unselectedTextColor = DukaanTextMuted
                        ),
                        modifier = Modifier.testTag("nav_customers")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SALES_AND_BILLS || currentScreen == AppScreen.INVOICE_DETAIL,
                        onClick = { currentScreen = AppScreen.SALES_AND_BILLS },
                        icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Sales & Bills") },
                        label = { Text("Sales & Bills", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DukaanForestGreen,
                            selectedTextColor = DukaanForestGreen,
                            indicatorColor = Color(0xFFCDE4D6),
                            unselectedIconColor = DukaanTextMuted,
                            unselectedTextColor = DukaanTextMuted
                        ),
                        modifier = Modifier.testTag("nav_pos")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.EXPENSES,
                        onClick = { currentScreen = AppScreen.EXPENSES },
                        icon = { Icon(Icons.Default.Payments, contentDescription = "Expenses") },
                        label = { Text("Expenses", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DukaanForestGreen,
                            selectedTextColor = DukaanForestGreen,
                            indicatorColor = Color(0xFFCDE4D6),
                            unselectedIconColor = DukaanTextMuted,
                            unselectedTextColor = DukaanTextMuted
                        ),
                        modifier = Modifier.testTag("nav_expenses")
                    )

                    NavigationBarItem(
                        selected = currentScreen in listOf(
                            AppScreen.MORE,
                            AppScreen.STOCK,
                            AppScreen.SUPPLIERS,
                            AppScreen.PURCHASES,
                            AppScreen.CASHBOOK,
                            AppScreen.REPORTS,
                            AppScreen.BACKUP,
                            AppScreen.SETTINGS
                        ),
                        onClick = {
                            if (currentScreen !in listOf(
                                    AppScreen.STOCK,
                                    AppScreen.SUPPLIERS,
                                    AppScreen.PURCHASES,
                                    AppScreen.CASHBOOK,
                                    AppScreen.REPORTS,
                                    AppScreen.BACKUP,
                                    AppScreen.SETTINGS
                                )) {
                                currentScreen = AppScreen.SUPPLIERS
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "More") },
                        label = { Text("More", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE65100),
                            selectedTextColor = Color(0xFFE65100),
                            indicatorColor = Color(0xFFFFE0B2), // Soft Orange Pill per screenshot
                            unselectedIconColor = DukaanTextMuted,
                            unselectedTextColor = DukaanTextMuted
                        ),
                        modifier = Modifier.testTag("nav_more")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToPos = { currentScreen = AppScreen.SALES_AND_BILLS },
                    onNavigateToCustomers = { currentScreen = AppScreen.CUSTOMERS },
                    onNavigateToSuppliers = { currentScreen = AppScreen.SUPPLIERS },
                    onNavigateToStock = { currentScreen = AppScreen.STOCK },
                    onNavigateToExpenses = { currentScreen = AppScreen.EXPENSES },
                    onNavigateToCashbook = { currentScreen = AppScreen.CASHBOOK }
                )

                AppScreen.CUSTOMERS -> CustomerListScreen(
                    viewModel = viewModel,
                    onCustomerClick = { customerId ->
                        selectedCustomerId = customerId
                        currentScreen = AppScreen.CUSTOMER_DETAIL
                    }
                )

                AppScreen.CUSTOMER_DETAIL -> selectedCustomerId?.let { id ->
                    CustomerDetailScreen(
                        customerId = id,
                        viewModel = viewModel,
                        onBack = { currentScreen = AppScreen.CUSTOMERS }
                    )
                }

                AppScreen.SALES_AND_BILLS -> PosSalesScreen(
                    viewModel = viewModel,
                    onNavigateToInvoice = { invoiceId ->
                        selectedInvoiceId = invoiceId
                        currentScreen = AppScreen.INVOICE_DETAIL
                    }
                )

                AppScreen.INVOICE_DETAIL -> selectedInvoiceId?.let { id ->
                    InvoiceDetailScreen(
                        invoiceId = id,
                        viewModel = viewModel,
                        onBack = { currentScreen = AppScreen.SALES_AND_BILLS }
                    )
                }

                AppScreen.EXPENSES -> ExpensesScreen(viewModel = viewModel)

                AppScreen.MORE -> SupplierListScreen(
                    viewModel = viewModel,
                    onNavigateTab = onTopTabNavigate,
                    onSupplierClick = { supplierId ->
                        selectedSupplierId = supplierId
                        currentScreen = AppScreen.SUPPLIER_DETAIL
                    }
                )

                AppScreen.STOCK -> ProductListScreen(
                    viewModel = viewModel,
                    onNavigateTab = onTopTabNavigate
                )

                AppScreen.SUPPLIERS -> SupplierListScreen(
                    viewModel = viewModel,
                    onNavigateTab = onTopTabNavigate,
                    onSupplierClick = { supplierId ->
                        selectedSupplierId = supplierId
                        currentScreen = AppScreen.SUPPLIER_DETAIL
                    }
                )

                AppScreen.SUPPLIER_DETAIL -> selectedSupplierId?.let { id ->
                    SupplierDetailScreen(
                        supplierId = id,
                        viewModel = viewModel,
                        onBack = { currentScreen = AppScreen.SUPPLIERS }
                    )
                }

                AppScreen.PURCHASES -> PurchasesScreen(
                    viewModel = viewModel,
                    onNavigateTab = onTopTabNavigate
                )

                AppScreen.CASHBOOK -> CashbookScreen(
                    viewModel = viewModel,
                    onNavigateTab = onTopTabNavigate
                )

                AppScreen.REPORTS -> ReportsScreen(
                    viewModel = viewModel,
                    onNavigateTab = onTopTabNavigate
                )

                AppScreen.BACKUP -> BackupScreen(
                    viewModel = viewModel,
                    onNavigateTab = onTopTabNavigate
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateTab = onTopTabNavigate
                )

                else -> {}
            }
        }
    }
}

@Composable
fun MoreMenuScreen(
    isHindi: Boolean,
    onNavigate: (AppScreen) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("more_menu_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "More Modules & Settings",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DukaanTextDark
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.Inventory,
                title = "Products & Stock",
                subtitle = "Manage inventory, items & price",
                onClick = { onNavigate(AppScreen.STOCK) },
                iconTint = Color(0xFF00796B)
            )
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.LocalShipping,
                title = "Suppliers & Purchases",
                subtitle = "Track purchases & supplier balances",
                onClick = { onNavigate(AppScreen.SUPPLIERS) },
                iconTint = DukaanForestGreen
            )
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.AttachMoney,
                title = "Cashbook (रोकड़ बही)",
                subtitle = "Daily cash in hand, inflow & outflow",
                onClick = { onNavigate(AppScreen.CASHBOOK) },
                iconTint = Color(0xFF2E7D32)
            )
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.Assessment,
                title = "Reports & Analytics",
                subtitle = "P&L summary, sales & margin analysis",
                onClick = { onNavigate(AppScreen.REPORTS) },
                iconTint = Color(0xFF1976D2)
            )
        }

        item {
            MoreMenuItem(
                icon = Icons.Default.Settings,
                title = "Store Profile & Settings",
                subtitle = "Language, shop details and demo mode",
                onClick = { onNavigate(AppScreen.SETTINGS) },
                iconTint = Color(0xFFE65100)
            )
        }
    }
}

@Composable
fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DukaanTextDark
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = DukaanTextMuted)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = DukaanTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
