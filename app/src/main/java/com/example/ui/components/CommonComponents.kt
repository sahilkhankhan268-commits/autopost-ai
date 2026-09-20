package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserGameData

@Composable
fun TopCurrencyBar(
    userData: UserGameData,
    onShopClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCoinClick: (() -> Unit)? = null,
    onGemClick: (() -> Unit)? = null,
    showSettings: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Currency Badges
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coins Chip
            CurrencyChip(
                icon = "🪙",
                amount = userData.coins,
                color = Color(0xFFFFD54F),
                bgColor = Color(0xFF2E2007),
                borderColor = Color(0xFFFFB300),
                onPlusClick = {
                    onCoinClick?.invoke() ?: onShopClick()
                },
                testTag = "currency_coins_chip"
            )

            // Gems Chip
            CurrencyChip(
                icon = "💎",
                amount = userData.gems,
                color = Color(0xFF80D8FF),
                bgColor = Color(0xFF04243A),
                borderColor = Color(0xFF00B0FF),
                onPlusClick = {
                    onGemClick?.invoke() ?: onShopClick()
                },
                testTag = "currency_gems_chip"
            )
        }

        if (showSettings) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .border(1.dp, Color(0x66FFFFFF), CircleShape)
                    .testTag("btn_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun CurrencyChip(
    icon: String,
    amount: Int,
    color: Color,
    bgColor: Color,
    borderColor: Color,
    onPlusClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable { onPlusClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = amount.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingEmoji: String? = null,
    gradientColors: List<Color> = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)),
    textColor: Color = Color.White,
    testTag: String = "game_button"
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = modifier
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradientColors.first().copy(alpha = 0.95f),
                            gradientColors.last()
                        )
                    )
                )
                .border(1.5.dp, Color(0x66FFFFFF), RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingEmoji != null) {
                    Text(text = leadingEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun StarRatingRow(
    stars: Int,
    maxStars: Int = 3,
    starSize: androidx.compose.ui.unit.Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val filled = i <= stars
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = if (filled) "Star Filled" else "Star Empty",
                tint = if (filled) Color(0xFFFFD700) else Color(0x44FFFFFF),
                modifier = Modifier
                    .size(starSize)
                    .padding(horizontal = 2.dp)
            )
        }
    }
}
