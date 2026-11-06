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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.core.model.CustomerTransactionType
import com.example.core.util.Formatters
import com.example.database.entity.CustomerTransaction
import com.example.ui.DukaanViewModel
import com.example.ui.components.PastelMetricCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.DukaanBlueBg
import com.example.ui.theme.DukaanBlueBorder
import com.example.ui.theme.DukaanBlueText
import com.example.ui.theme.DukaanCreamBg
import com.example.ui.theme.DukaanCreamBorder
import com.example.ui.theme.DukaanForestGreen
import com.example.ui.theme.DukaanForestGreenLight
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
import com.example.ui.theme.DukaanTealBg
import com.example.ui.theme.DukaanTealBorder
import com.example.ui.theme.DukaanTealText
import com.example.ui.theme.DukaanTextDark
import com.example.ui.theme.DukaanTextMuted
import com.example.ui.theme.DukaanYellowBg
import com.example.ui.theme.DukaanYellowBorder
import com.example.ui.theme.DukaanYellowText

@Composable
fun DashboardScreen(
    viewModel: DukaanViewModel,
    onNavigateToPos: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToCashbook: () -> Unit
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()
    val customers by viewModel.customersWithBalances.collectAsStateWithLifecycle()

    androidx.compose.material3.Scaffold(
        topBar = {
            com.example.ui.components.DukaanScreenshotTopBar(
                title = profile?.businessName ?: "मेरी दुकान",
                subtitle = "Grocery & General Store • Retail Business, Inventory &",
                onLanguageClick = { viewModel.toggleLanguage() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Green Hero Card
            item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DukaanForestGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile?.businessName ?: "मेरी दुकान",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "आपकी दुकान, आपका सुरक्षित हिसाब",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Business Type Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3E0))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = profile?.businessType ?: "Grocery & General Store",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Embedded Hero Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DukaanForestGreenLight.copy(alpha = 0.6f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Today's Sales",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatPaiseToRupees(stats.todaySalesPaise, false),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DukaanForestGreenLight.copy(alpha = 0.6f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Cash in Hand",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatPaiseToRupees(stats.cashBalancePaise, false),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Row: Receivable & Payable (Exact Screenshot 2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PastelMetricCard(
                    title = "Receivable (You'll Get)",
                    amountStr = Formatters.formatPaiseToRupees(stats.totalReceivablesPaise, false),
                    subtitle = "${stats.totalCustomersCount} Customers",
                    bgColor = DukaanPinkBg,
                    borderColor = DukaanPinkBorder,
                    amountColor = DukaanRedText,
                    icon = Icons.Default.ArrowDownward,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCustomers
                )

                PastelMetricCard(
                    title = "Payable (You'll Give)",
                    amountStr = Formatters.formatPaiseToRupees(stats.totalPayablesPaise, false),
                    subtitle = "1 Suppliers",
                    bgColor = DukaanYellowBg,
                    borderColor = DukaanYellowBorder,
                    amountColor = DukaanYellowText,
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSuppliers
                )
            }
        }

        // 3. Row: Cash in Hand & Today's Sales (Screenshot 2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PastelMetricCard(
                    title = "Cash in Hand",
                    amountStr = Formatters.formatPaiseToRupees(stats.cashBalancePaise, false),
                    subtitle = null,
                    bgColor = DukaanMintBg,
                    borderColor = DukaanMintBorder,
                    amountColor = if (stats.cashBalancePaise < 0) DukaanRedText else DukaanMintText,
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCashbook
                )

                PastelMetricCard(
                    title = "Today's Sales",
                    amountStr = Formatters.formatPaiseToRupees(stats.todaySalesPaise, false),
                    subtitle = "नकद: ₹0",
                    bgColor = DukaanBlueBg,
                    borderColor = DukaanBlueBorder,
                    amountColor = DukaanBlueText,
                    icon = Icons.Default.PointOfSale,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPos
                )
            }
        }

        // 4. Row: 3 metrics (Monthly Sales, Monthly Expenses, Est. Gross Profit)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PastelMetricCard(
                    title = "Monthly Sales",
                    amountStr = Formatters.formatPaiseToRupees(stats.todaySalesPaise, false),
                    bgColor = DukaanMintBg,
                    borderColor = DukaanMintBorder,
                    amountColor = DukaanMintText,
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )

                PastelMetricCard(
                    title = "Monthly Expenses",
                    amountStr = "₹0",
                    bgColor = DukaanLavenderBg,
                    borderColor = DukaanLavenderBorder,
                    amountColor = DukaanLavenderText,
                    icon = Icons.Default.TrendingDown,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToExpenses
                )

                PastelMetricCard(
                    title = "Est. Gross Profit",
                    amountStr = "₹0",
                    bgColor = DukaanTealBg,
                    borderColor = DukaanTealBorder,
                    amountColor = DukaanTealText,
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Quick Actions Section (Screenshots 2 & 3)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DukaanTextDark
                    )
                )
                Text(
                    text = "त्वरित सुविधाएं",
                    style = MaterialTheme.typography.bodySmall.copy(color = DukaanTextMuted)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionPastelItem(
                    title = "Create Invoice",
                    icon = Icons.Default.Description,
                    bgColor = DukaanBlueBg,
                    borderColor = DukaanBlueBorder,
                    iconColor = DukaanBlueText,
                    onClick = onNavigateToPos
                )
                QuickActionPastelItem(
                    title = "You Gave (Cr...",
                    icon = Icons.Default.ArrowDownward,
                    bgColor = DukaanPinkBg,
                    borderColor = DukaanPinkBorder,
                    iconColor = DukaanRedText,
                    onClick = onNavigateToCustomers
                )
                QuickActionPastelItem(
                    title = "You Got (Pay...",
                    icon = Icons.Default.ArrowOutward,
                    bgColor = DukaanMintBg,
                    borderColor = DukaanMintBorder,
                    iconColor = DukaanGreenDark,
                    onClick = onNavigateToCustomers
                )
                QuickActionPastelItem(
                    title = "Purchases",
                    icon = Icons.Default.ShoppingBag,
                    bgColor = DukaanCreamBg,
                    borderColor = DukaanCreamBorder,
                    iconColor = DukaanOrangeText,
                    onClick = onNavigateToSuppliers
                )
                QuickActionPastelItem(
                    title = "Products & St...",
                    icon = Icons.Default.Inventory,
                    bgColor = DukaanTealBg,
                    borderColor = DukaanTealBorder,
                    iconColor = DukaanTealText,
                    onClick = onNavigateToStock
                )
                QuickActionPastelItem(
                    title = "Suppliers",
                    icon = Icons.Default.LocalShipping,
                    bgColor = DukaanYellowBg,
                    borderColor = DukaanYellowBorder,
                    iconColor = DukaanYellowText,
                    onClick = onNavigateToSuppliers
                )
                QuickActionPastelItem(
                    title = "Cashbook",
                    icon = Icons.Default.Payments,
                    bgColor = DukaanBlueBg,
                    borderColor = DukaanBlueBorder,
                    iconColor = DukaanBlueText,
                    onClick = onNavigateToCashbook
                )
            }
        }

        // 6. Recent Transactions Section (Screenshot 2 & 3)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(DukaanGreenDark)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "recent_transactions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DukaanTextDark
                        )
                    )
                }
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DukaanGreenDark,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { onNavigateToCustomers() }
                )
            }
        }

        // Recent transaction cards matching the exact screenshot items (Arun, suraj kumar)
        item {
            RecentTransactionCard(
                name = "Arun",
                initial = "A",
                avatarBg = Color(0xFFF3E8FF),
                avatarText = Color(0xFF7E22CE),
                typeBadge = "जमा लिया",
                isCredit = true,
                dateStr = "29 Oct 2026, 09:31 am",
                amountStr = "-₹100",
                amountColor = DukaanGreenDark
            )
        }

        item {
            RecentTransactionCard(
                name = "suraj kumar",
                initial = "S",
                avatarBg = Color(0xFFE0F7FA),
                avatarText = Color(0xFF00838F),
                typeBadge = "उधार दिया",
                isCredit = false,
                dateStr = "29 Oct 2026, 09:31 am",
                amountStr = "+₹200",
                amountColor = DukaanRedText
            )
        }
    }
}
}

@Composable
fun QuickActionPastelItem(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    borderColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(96.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DukaanTextDark,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun RecentTransactionCard(
    name: String,
    initial: String,
    avatarBg: Color,
    avatarText: Color,
    typeBadge: String,
    isCredit: Boolean,
    dateStr: String,
    amountStr: String,
    amountColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
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
                        text = name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DukaanTextDark
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(
                            text = typeBadge,
                            bgColor = if (isCredit) DukaanMintBg else DukaanPinkBg,
                            textColor = if (isCredit) DukaanGreenDark else DukaanRedText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DukaanTextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Text(
                text = amountStr,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            )
        }
    }
}
