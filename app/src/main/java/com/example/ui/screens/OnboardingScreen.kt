package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.model.BusinessType
import com.example.core.util.Formatters
import com.example.ui.DukaanViewModel
import com.example.ui.theme.DukaanGreenJama
import com.example.ui.theme.DukaanNavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: DukaanViewModel,
    onComplete: () -> Unit
) {
    val existingProfile by viewModel.profile.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()

    var businessName by remember(existingProfile) { mutableStateOf(existingProfile?.businessName ?: "") }
    var selectedBusinessType by remember(existingProfile) {
        mutableStateOf(existingProfile?.businessType ?: BusinessType.KIRANA.hindiTitle)
    }
    var ownerName by remember(existingProfile) { mutableStateOf(existingProfile?.ownerName ?: "") }
    var phone by remember(existingProfile) { mutableStateOf(existingProfile?.phone ?: "") }
    var address by remember(existingProfile) { mutableStateOf(existingProfile?.address ?: "") }
    var openingCashStr by remember(existingProfile) {
        mutableStateOf(
            if ((existingProfile?.openingCashPaise ?: 0L) > 0L)
                (existingProfile!!.openingCashPaise / 100).toString()
            else ""
        )
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                // Brand Logo icon
                // Official Dukaan Hisab Brand Logo
                Image(
                    painter = painterResource(id = R.drawable.dukaan_logo),
                    contentDescription = "Dukaan Hisab Pro",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "दुकान हिसाब प्रो (Dukaan Hisab Pro)",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = DukaanNavyPrimary
                )
                Text(
                    text = if (isHindi)
                        "दुकानदारों का भरोसेमंद डिजिटल बहीखाता व बिलिंग"
                    else
                        "Complete Offline Retail Management, Udhaar Khata & Billing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (isHindi) "दुकान का विवरण दर्ज करें" else "Setup Your Business Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text(if (isHindi) "दुकान / व्यापार का नाम *" else "Shop / Business Name *") },
                            placeholder = { Text("उदा. गुप्ता किराना स्टोर") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_shop_name_input"),
                            singleLine = true
                        )

                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedBusinessType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isHindi) "दुकान का प्रकार (Category)" else "Business Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                BusinessType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(if (isHindi) type.hindiTitle else type.englishTitle) },
                                        onClick = {
                                            selectedBusinessType = if (isHindi) type.hindiTitle else type.englishTitle
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text(if (isHindi) "दुकानदार का नाम" else "Owner Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(if (isHindi) "मोबाइल नंबर" else "Mobile Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(if (isHindi) "दुकान का पता / बाजार" else "Shop Address / Market") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = openingCashStr,
                            onValueChange = { openingCashStr = it },
                            label = { Text(if (isHindi) "गल्ले में शुरुआती रोकड़ (₹)" else "Opening Cash Balance (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                val name = businessName.ifBlank { "मेरी दुकान (Dukaan Hisab)" }
                                val cashPaise = Formatters.parseRupeesToPaise(openingCashStr)
                                viewModel.saveBusinessProfile(name, selectedBusinessType, ownerName, phone, address, cashPaise)
                                onComplete()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboarding_save_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DukaanGreenJama)
                        ) {
                            Text(
                                text = if (isHindi) "शुरू करें (Get Started)" else "Get Started",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // Demo Mode Button (Section 5)
                        OutlinedButton(
                            onClick = {
                                val name = "गुप्ता किराना स्टोर (Gupta Kirana)"
                                viewModel.saveBusinessProfile(name, BusinessType.KIRANA.hindiTitle, "रमेश गुप्ता", "9876543210", "मेन मार्केट, वार्ड 3", 500000L)
                                viewModel.seedDemoData()
                                onComplete()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboarding_demo_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFF673AB7))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "डेमो स्टोर से शुरू करें (Sample Data)" else "Start with Sample Demo Store",
                                color = Color(0xFF673AB7),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
