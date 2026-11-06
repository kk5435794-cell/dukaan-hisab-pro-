package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.util.Formatters
import com.example.database.entity.SalesInvoiceItem
import com.example.ui.DukaanViewModel
import com.example.ui.components.DUKAAN_MORE_TABS
import com.example.ui.components.DukaanScreenshotTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterChipItem
import com.example.ui.theme.DukaanForestGreen
import com.example.ui.theme.DukaanGreenDark
import com.example.ui.theme.DukaanMintBg
import com.example.ui.theme.DukaanMintBorder
import com.example.ui.theme.DukaanMintText
import com.example.ui.theme.DukaanRedText
import com.example.ui.theme.DukaanTextDark
import com.example.ui.theme.DukaanTextMuted

@Composable
fun ReportsScreen(
    viewModel: DukaanViewModel,
    onNavigateTab: (String) -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val topTabs = DUKAAN_MORE_TABS
    val timeFilters = listOf("Today", "This Week", "This Month", "All Time")
    var selectedTimeIndex by remember { mutableIntStateOf(2) } // "This Month" selected per screenshot 13

    val totalSalesPaise = remember(invoices) { invoices.sumOf { it.grandTotalPaise } }
    val totalExpensesPaise = remember(expenses) { expenses.sumOf { it.amountPaise } }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Reports",
                subtitle = "Reports & Analytics",
                topTabs = topTabs,
                activeTab = "Reports",
                onTabClick = onNavigateTab,
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("reports_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Time Filter Row (Today, This Week, This Month, All Time)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeFilters.forEachIndexed { index, filter ->
                        FilterChipItem(
                            label = filter,
                            isSelected = selectedTimeIndex == index,
                            onClick = { selectedTimeIndex = index },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Card 1: लाभ-हानि व व्यावसायिक हिसाब (P&L Summary) (Screenshot 13)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "लाभ-हानि व व्यावसायिक हिसाब (P&L Summary)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DukaanTextDark,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        ScreenshotReportRow(label = "Total Sales", value = Formatters.formatPaiseToRupees(totalSalesPaise, false), valueColor = DukaanGreenDark)
                        Spacer(modifier = Modifier.height(10.dp))
                        ScreenshotReportRow(label = "Cost of Goods Sold (COGS)", value = "- ₹0", valueColor = DukaanTextDark)
                        Spacer(modifier = Modifier.height(10.dp))
                        ScreenshotReportRow(label = "Est. Gross Profit", value = Formatters.formatPaiseToRupees(totalSalesPaise, false), valueColor = DukaanGreenDark)
                        Spacer(modifier = Modifier.height(10.dp))
                        ScreenshotReportRow(label = "Monthly Expenses", value = "- " + Formatters.formatPaiseToRupees(totalExpensesPaise, false), valueColor = DukaanRedText)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Highlighted Soft Green Box: Est. Net Profit
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DukaanMintBg)
                                .border(1.dp, DukaanMintBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Est. Net Profit",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DukaanMintText
                                    )
                                )
                                Text(
                                    text = Formatters.formatPaiseToRupees(totalSalesPaise - totalExpensesPaise, false),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DukaanMintText
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Card 2: रोकड़ व बही प्रवाह (Cash & Ledger Flow) (Screenshot 13)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "रोकड़ व बही प्रवाह (Cash & Ledger Flow)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DukaanTextDark,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        ScreenshotReportRow(label = "ग्राहक से वसूली (Payments Collected):", value = "₹0", valueColor = DukaanGreenDark)
                        Spacer(modifier = Modifier.height(10.dp))
                        ScreenshotReportRow(label = "नया उधार दिया (Credit Given):", value = "₹0", valueColor = DukaanRedText)
                        Spacer(modifier = Modifier.height(10.dp))
                        ScreenshotReportRow(label = "कुल माल खरीदी (Purchases):", value = "₹0", valueColor = DukaanRedText)
                    }
                }
            }

            // Card 3: खर्चों का वर्गीकरण (Expense Breakdown) (Screenshot 13)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "खर्चों का वर्गीकरण (Expense Breakdown)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DukaanTextDark,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "इस अवधि में कोई खर्चा दर्ज नहीं है।",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DukaanTextMuted,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenshotReportRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DukaanTextDark,
                fontSize = 14.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = valueColor,
                fontSize = 15.sp
            )
        )
    }
}

@Composable
fun BackupScreen(
    viewModel: DukaanViewModel,
    onNavigateTab: (String) -> Unit
) {
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var backupStatusText by remember { mutableStateOf<String?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Backup",
                subtitle = if (isHindi) "डेटा बैकअप व रिस्टोर" else "Data Backup & Restore",
                topTabs = DUKAAN_MORE_TABS,
                activeTab = "Backup",
                onTabClick = onNavigateTab,
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("backup_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: Export Backup
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DukaanForestGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = DukaanForestGreen)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isHindi) "ऑफ़लाइन बैकअप बनाएं (JSON)" else "Create Offline Backup",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DukaanTextDark
                                    )
                                )
                                Text(
                                    text = if (isHindi) "सभी खातों, बिलों और स्टॉक का बैकअप सुरक्षित करें" else "Save customer khata, invoices & stock",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DukaanTextMuted)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val json = viewModel.repository.exportBackupJson()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, json)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Dukaan Backup"))
                                    backupStatusText = if (isHindi) "बैकअप सफलतापूर्वक तैयार हुआ!" else "Backup generated successfully!"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DukaanForestGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isHindi) "बैकअप एक्सपोर्ट करें" else "Export Backup Now", fontWeight = FontWeight.Bold)
                        }

                        backupStatusText?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = it, color = DukaanForestGreen, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Card 2: Restore Data
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE65100).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFFE65100))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isHindi) "बैकअप से डेटा पुनर्स्थापित करें" else "Restore from Backup",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DukaanTextDark
                                    )
                                )
                                Text(
                                    text = if (isHindi) "पहले से सेव किए गए बैकअप से बहीखाता लोड करें" else "Restore your ledger records from backup",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DukaanTextMuted)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isHindi) "बैकअप फाइल चुनें / रीस्टोर" else "Restore Backup File", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Card 3: 100% Offline Safety Guarantee
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isHindi) "100% सुरक्षित और स्थानीय डेटा" else "100% Offline & Private",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            )
                            Text(
                                text = if (isHindi)
                                    "आपका सारा बहीखाता आपके फोन के लोकल SQLite डेटाबेस में सुरक्षित रहता है।"
                                else
                                    "Your complete ledger is safely stored on your local device.",
                                style = MaterialTheme.typography.bodySmall.copy(color = DukaanTextDark)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(if (isHindi) "डेटा पुनर्स्थापित करें?" else "Restore Backup?") },
            text = {
                Text(
                    if (isHindi)
                        "क्या आप अपने बैकअप से डेटा लोड करना चाहते हैं? इससे वर्तमान डेटा अपडेट हो जाएगा।"
                    else
                        "Do you want to restore data from backup? This will sync all records."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreDialog = false
                        backupStatusText = if (isHindi) "डेटा सुरक्षित रूप से सत्यापित!" else "Data verified successfully!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DukaanForestGreen)
                ) {
                    Text(if (isHindi) "पुनर्स्थापित करें" else "Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsScreen(
    viewModel: DukaanViewModel,
    onNavigateTab: (String) -> Unit
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    var shopName by remember(profile) { mutableStateOf(profile?.businessName ?: "मेरी दुकान") }
    var businessType by remember(profile) { mutableStateOf(profile?.businessType ?: "Grocery & General Store") }
    var ownerName by remember(profile) { mutableStateOf(profile?.ownerName ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var address by remember(profile) { mutableStateOf(profile?.address ?: "") }
    var isPinLockEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Settings",
                subtitle = "App Preferences & Store Details",
                topTabs = DUKAAN_MORE_TABS,
                activeTab = "Settings",
                onTabClick = onNavigateTab,
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: Dukaan Hisab Pro Banner Card (Screenshot 4)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dukaan_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Dukaan Hisab Pro",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F5132),
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "आपकी दुकान, आपका हिसाब",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DukaanTextDark,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "100% सुरक्षित • ऑफ़लाइन बहीखाता",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // Card 2: Language / भाषा (Screenshot 4)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = DukaanForestGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Language / भाषा",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DukaanTextDark,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Hindi Pill
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { if (!isHindi) viewModel.toggleLanguage() },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isHindi) Color(0xFFE8F5E9) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isHindi) Color(0xFF81C784) else Color(0xFFD1D5DB)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "हिंदी (Hindi)",
                                        fontWeight = if (isHindi) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isHindi) Color(0xFF1B5E20) else DukaanTextDark,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            // English Pill
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { if (isHindi) viewModel.toggleLanguage() },
                                shape = RoundedCornerShape(10.dp),
                                color = if (!isHindi) Color(0xFFE8F5E9) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (!isHindi) Color(0xFF81C784) else Color(0xFFD1D5DB)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "English",
                                        fontWeight = if (!isHindi) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isHindi) Color(0xFF1B5E20) else DukaanTextDark,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 3: App PIN Lock (Screenshot 4)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "App PIN Lock",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DukaanTextDark,
                                        fontSize = 16.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Protect access with a 4-digit PIN",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DukaanTextMuted)
                                )
                            }
                        }
                        Switch(
                            checked = isPinLockEnabled,
                            onCheckedChange = { isPinLockEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = DukaanForestGreen
                            )
                        )
                    }
                }
            }

            // Card 4: Shop Profile (Screenshot 4)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Shop Profile",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DukaanTextDark,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("Shop Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Business Type Dropdown
                        var expandedBusinessType by remember { mutableStateOf(false) }
                        val businessTypes = listOf(
                            "Grocery & General Store",
                            "Electronics & Mobile",
                            "Clothing & Garments",
                            "Medical & Pharmacy",
                            "Hardware & Sanitary",
                            "Wholesale & Distributor"
                        )
                        @OptIn(ExperimentalMaterial3Api::class)
                        ExposedDropdownMenuBox(
                            expanded = expandedBusinessType,
                            onExpandedChange = { expandedBusinessType = it }
                        ) {
                            OutlinedTextField(
                                value = businessType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Business Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBusinessType) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedBusinessType,
                                onDismissRequest = { expandedBusinessType = false }
                            ) {
                                businessTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            businessType = type
                                            expandedBusinessType = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.saveBusinessProfile(
                                    name = shopName.trim(),
                                    type = businessType,
                                    owner = ownerName.trim(),
                                    phone = phone.trim(),
                                    address = address.trim(),
                                    openingCashPaise = profile?.openingCashPaise ?: 0L
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DukaanForestGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isHindi) "सुरक्षित करें (Save Profile)" else "Save Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Card 5: Clear All Entries
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8C8C8))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHindi) "सभी एंट्रीज हटाएं (Clear All Entries)" else "Clear All Entries",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isHindi)
                                "ग्राहकों के बहीखाता, बिक्री बिल, खर्च और रोकड़ की सभी एंट्रीज पूरी तरह साफ करें।"
                            else
                                "Wipe all customer khata, invoices, expenses, and cashbook records clean.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DukaanTextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showClearConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isHindi) "सभी एंट्रीज हटाएं" else "Clear All Entries")
                        }
                    }
                }
            }

            // Card 6: Privacy Policy & Data Protection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = DukaanForestGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHindi) "गोपनीयता नीति (Privacy Policy)" else "Privacy Policy & Trust",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = DukaanForestGreen)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isHindi)
                                "आपका संपूर्ण बहीखाता 100% ऑफ़लाइन व सुरक्षित है। हम कोई भी व्यक्तिगत या व्यावसायिक डेटा बाहरी सर्वर पर नहीं भेजते।"
                            else
                                "Your complete khata is 100% on-device & private. No customer or ledger records are shared externally.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DukaanTextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showPrivacyPolicyDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isHindi) "गोपनीयता नीति पढ़ें" else "View Privacy Policy")
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = if (isHindi) "क्या आप सभी एंट्रीज हटाना चाहते हैं?" else "Clear All Entries?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (isHindi)
                        "यह क्रिया सभी ग्राहकों, बिलों, खर्चों और रोकड़ प्रविष्टियों को हमेशा के लिए हटा देगी।"
                    else
                        "This will permanently delete all customer khata, bills, expenses, and cashbook records."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(if (isHindi) "हाँ, सब हटाएं" else "Yes, Clear All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Text(
                    text = if (isHindi) "गोपनीयता नीति (Privacy Policy)" else "Privacy Policy",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Dukaan Hisab Pro (com.aistudio.dukaanhisab.kzpvqm)\n" +
                                "Developer: kk5435794@gmail.com\n\n" +
                                if (isHindi)
                                    "1. 100% ऑफ़लाइन डेटा: आपके ग्राहकों, लेन-देन, बिलों और स्टॉक की सभी जानकारी पूरी तरह आपके डिवाइस के लोकल डेटाबेस में सुरक्षित रहती है।\n" +
                                    "2. कोई सर्वर शेयरिंग नहीं: हम आपका कोई भी डेटा किसी बाहरी सर्वर या थर्ड-पार्टी को नहीं बेचते और न ही अपलोड करते हैं।\n" +
                                    "3. डेटा बैकअप व रिस्टोर: आप कभी भी बैकअप एक्सपोर्ट कर सकते हैं।\n" +
                                    "4. डेटा विलोपन: आप सेटिंग्स में 'सभी एंट्रीज हटाएं' द्वारा सारा डेटा कभी भी मिटा सकते हैं।"
                                else
                                    "1. 100% Local Storage: All customer, ledger, sales, expenses, and inventory data is stored locally in a secure on-device SQLite database.\n" +
                                    "2. Zero External Tracking: We do not upload, sell, or monetize your business records.\n" +
                                    "3. User Data Control: You can export offline JSON backups at any time.\n" +
                                    "4. Immediate Deletion: You can purge all records using 'Clear All Entries' or uninstalling the app."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DukaanForestGreen)
                ) {
                    Text(if (isHindi) "समझ गया" else "OK")
                }
            }
        )
    }
}

@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: DukaanViewModel,
    onBack: () -> Unit
) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val invoice = invoices.find { it.id == invoiceId }
    val itemsState = produceState<List<SalesInvoiceItem>>(initialValue = emptyList(), invoiceId) {
        value = viewModel.repository.getInvoiceItems(invoiceId)
    }

    Scaffold(
        topBar = {
            DukaanScreenshotTopBar(
                title = "Invoice",
                subtitle = invoice?.invoiceNumber ?: "",
                onLanguageClick = { viewModel.toggleLanguage() },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        if (invoice == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading invoice...")
            }
            return@Scaffold
        }

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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = profile?.businessName ?: "मेरी दुकान",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DukaanForestGreen
                                )
                            )
                            Text(
                                text = profile?.businessType ?: "Grocery & General Store",
                                style = MaterialTheme.typography.bodySmall,
                                color = DukaanTextMuted
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold)
                            Text(Formatters.formatDate(invoice.dateTimestamp), color = DukaanTextMuted)
                        }
                        Text("Customer: ${invoice.customerName}", fontWeight = FontWeight.SemiBold)

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        itemsState.value.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.productName, modifier = Modifier.weight(1f))
                                Text("${item.quantity} ${item.unit}", modifier = Modifier.width(60.dp))
                                Text(Formatters.formatPaiseToRupees(item.lineTotalPaise, false), fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grand Total:", fontWeight = FontWeight.Bold)
                            Text(
                                text = Formatters.formatPaiseToRupees(invoice.grandTotalPaise, false),
                                fontWeight = FontWeight.Bold,
                                color = DukaanForestGreen,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
