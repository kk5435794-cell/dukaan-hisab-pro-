package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DukaanBorderLight
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
import com.example.ui.theme.DukaanYellowAccent

val DUKAAN_MORE_TABS = listOf(
    "Suppliers",
    "Products & Stock",
    "Purchases",
    "Cashbook",
    "Reports",
    "Backup",
    "Settings"
)

@Composable
fun DukaanScreenshotTopBar(
    title: String,
    subtitle: String,
    topTabs: List<String>? = null,
    activeTab: String? = null,
    onTabClick: ((String) -> Unit)? = null,
    onLanguageClick: () -> Unit,
    navigationIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        color = DukaanForestGreen,
        contentColor = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Optional upper horizontal scrollable tab strip
            if (!topTabs.isNullOrEmpty()) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    topTabs.forEach { tabName ->
                        val isSelected = tabName == activeTab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onTabClick?.invoke(tabName) }
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tabName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) DukaanYellowAccent else Color.White.copy(alpha = 0.85f),
                                    fontSize = 14.sp
                                )
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(2.dp)
                                        .background(DukaanYellowAccent)
                                )
                            }
                        }
                    }
                }
            }

            // Main Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (navigationIcon != null) {
                    navigationIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Circular Dukaan Hisab Logo Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.dukaan_logo),
                        contentDescription = "Dukaan Hisab Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }

                // Circular Globe Button for Language
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onLanguageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) DukaanMintBg else Color.White)
            .border(
                width = 1.dp,
                color = if (isSelected) DukaanMintBorder else DukaanBorderLight,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DukaanForestGreen else DukaanTextDark,
                fontSize = 13.sp
            )
        )
    }
}

@Composable
fun PastelMetricCard(
    title: String,
    amountStr: String,
    subtitle: String? = null,
    bgColor: Color,
    borderColor: Color,
    amountColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = DukaanTextDark,
                        fontSize = 13.sp
                    )
                )
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(amountColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = amountColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = amountStr,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    fontSize = 20.sp
                )
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DukaanTextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun StatusPill(
    label: String,
    isGreen: Boolean = true
) {
    StatusPill(
        text = label,
        bgColor = if (isGreen) DukaanMintBg else DukaanPinkBg,
        textColor = if (isGreen) DukaanMintText else DukaanRedText
    )
}

@Composable
fun DukaanTopBar(
    title: String,
    subtitle: String = "",
    navigationIcon: (@Composable () -> Unit)? = null,
    onLanguageClick: (() -> Unit)? = null
) {
    DukaanScreenshotTopBar(
        title = title,
        subtitle = subtitle,
        navigationIcon = navigationIcon,
        onLanguageClick = onLanguageClick ?: {}
    )
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DukaanMintBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DukaanForestGreen,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DukaanTextDark
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DukaanTextMuted
            ),
            textAlign = TextAlign.Center
        )
        if (!buttonText.isNullOrBlank() && onButtonClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DukaanForestGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = buttonText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
