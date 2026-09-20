package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.ContainerStyle
import com.example.model.FruitSkinPack
import com.example.model.PowerUpType
import com.example.model.ThemeBackground
import com.example.ui.components.GameButton
import com.example.ui.components.TopCurrencyBar
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun ShopScreen(
    viewModel: GameViewModel,
    userData: UserGameData,
    modifier: Modifier = Modifier
) {
    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tubes, 1: Themes, 2: Fruits, 3: Power-ups

    val bgTop = userData.selectedBackground.topColor
    val bgMid = userData.selectedBackground.midColor
    val bgBot = userData.selectedBackground.botColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(bgTop, bgMid, bgBot)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.HOME) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .testTag("btn_shop_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "🛍️ LUXURY SHOP",
                    color = Color(0xFFFFD54F),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Box(modifier = Modifier.size(44.dp))
            }

            TopCurrencyBar(
                userData = userData,
                onShopClick = {},
                onSettingsClick = { viewModel.openSettings() },
                showSettings = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Free Ad Reward Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x33000000),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📺 Free Sponsor Bonus",
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Watch video for 🪙 +100 and 💎 +3!",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }

                    GameButton(
                        text = "WATCH ▶",
                        gradientColors = listOf(Color(0xFF00E676), Color(0xFF1B5E20)),
                        onClick = { viewModel.watchRewardAd(activity) },
                        modifier = Modifier
                            .height(40.dp)
                            .width(110.dp),
                        testTag = "btn_shop_watch_ad"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShopTabItem(
                    label = "🏺 Tubes",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                ShopTabItem(
                    label = "🎨 Themes",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                ShopTabItem(
                    label = "🍉 Fruits",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
                ShopTabItem(
                    label = "⚡ Boosts",
                    isSelected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Container Tube Styles
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (style in ContainerStyle.entries) {
                            val isUnlocked = userData.unlockedStyles.contains(style)
                            val isSelected = userData.selectedStyle == style

                            StyleShopCard(
                                style = style,
                                isUnlocked = isUnlocked,
                                isSelected = isSelected,
                                onEquip = { viewModel.equipStyle(style) },
                                onBuy = { viewModel.buyShopStyle(style) }
                            )
                        }
                    }
                }
                1 -> {
                    // Board Environment Background Themes
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (theme in ThemeBackground.entries) {
                            val isUnlocked = userData.unlockedBackgrounds.contains(theme)
                            val isSelected = userData.selectedBackground == theme

                            ThemeShopCard(
                                theme = theme,
                                isUnlocked = isUnlocked,
                                isSelected = isSelected,
                                onEquip = { viewModel.equipBackground(theme) },
                                onBuy = { viewModel.buyShopBackground(theme) }
                            )
                        }
                    }
                }
                2 -> {
                    // Fruit Skin Packs
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (pack in FruitSkinPack.entries) {
                            val isUnlocked = userData.unlockedFruitPacks.contains(pack)
                            val isSelected = userData.selectedFruitPack == pack

                            FruitPackShopCard(
                                pack = pack,
                                isUnlocked = isUnlocked,
                                isSelected = isSelected,
                                onEquip = { viewModel.equipFruitPack(pack) },
                                onBuy = { viewModel.buyShopFruitPack(pack) }
                            )
                        }
                    }
                }
                3 -> {
                    // Power-Up Bundles
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (powerUp in PowerUpType.entries) {
                            val currentCount = userData.powerUps[powerUp] ?: 0
                            PowerUpShopCard(
                                powerUp = powerUp,
                                count = currentCount,
                                onBuy = { viewModel.buyShopPowerUp(powerUp) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ShopTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFFFFD54F) else Color(0x33FFFFFF),
        modifier = modifier
            .height(40.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) Color(0xFF1E1002) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun StyleShopCard(
    style: ContainerStyle,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onEquip: () -> Unit,
    onBuy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) Color(0xFF3E2C04) else Color(0x44000000),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFFFFD54F) else Color(0x33FFFFFF)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = style.iconEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = style.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = style.description,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }

            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1B5E20),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Equipped",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (isUnlocked) {
                GameButton(
                    text = "EQUIP",
                    gradientColors = listOf(Color(0xFF29B6F6), Color(0xFF0288D1)),
                    onClick = onEquip,
                    modifier = Modifier
                        .height(38.dp)
                        .width(96.dp),
                    testTag = "btn_equip_${style.name}"
                )
            } else {
                GameButton(
                    text = "🪙 ${style.costCoins}",
                    gradientColors = listOf(Color(0xFFFFCA28), Color(0xFFE65100)),
                    onClick = onBuy,
                    modifier = Modifier
                        .height(38.dp)
                        .width(96.dp),
                    testTag = "btn_buy_${style.name}"
                )
            }
        }
    }
}

@Composable
fun ThemeShopCard(
    theme: ThemeBackground,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onEquip: () -> Unit,
    onBuy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0x33000000),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFFFFD54F) else Color(0x33FFFFFF)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Theme Gradient Preview Thumbnail
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(theme.topColor, theme.midColor, theme.botColor)
                            )
                        )
                        .border(1.dp, theme.accentGlow, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = theme.iconEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = theme.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = theme.description,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }

            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1B5E20),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Equipped",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (isUnlocked) {
                GameButton(
                    text = "APPLY",
                    gradientColors = listOf(Color(0xFF29B6F6), Color(0xFF0288D1)),
                    onClick = onEquip,
                    modifier = Modifier
                        .height(38.dp)
                        .width(96.dp),
                    testTag = "btn_equip_theme_${theme.name}"
                )
            } else {
                val priceText = if (theme.costCoins > 0) "🪙 ${theme.costCoins}" else "💎 ${theme.gemCost}"
                GameButton(
                    text = priceText,
                    gradientColors = listOf(Color(0xFFFFCA28), Color(0xFFE65100)),
                    onClick = onBuy,
                    modifier = Modifier
                        .height(38.dp)
                        .width(96.dp),
                    testTag = "btn_buy_theme_${theme.name}"
                )
            }
        }
    }
}

@Composable
fun FruitPackShopCard(
    pack: FruitSkinPack,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onEquip: () -> Unit,
    onBuy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0x33000000),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFFFFD54F) else Color(0x33FFFFFF)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pack.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pack.sampleEmojis,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pack.description,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }

            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1B5E20),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Equipped",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "EQUIPPED",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (isUnlocked) {
                GameButton(
                    text = "EQUIP",
                    gradientColors = listOf(Color(0xFF29B6F6), Color(0xFF0288D1)),
                    onClick = onEquip,
                    modifier = Modifier
                        .height(38.dp)
                        .width(96.dp),
                    testTag = "btn_equip_fruit_${pack.name}"
                )
            } else {
                val priceText = if (pack.costCoins > 0) "🪙 ${pack.costCoins}" else "💎 ${pack.gemCost}"
                GameButton(
                    text = priceText,
                    gradientColors = listOf(Color(0xFFFFCA28), Color(0xFFE65100)),
                    onClick = onBuy,
                    modifier = Modifier
                        .height(38.dp)
                        .width(96.dp),
                    testTag = "btn_buy_fruit_${pack.name}"
                )
            }
        }
    }
}

@Composable
fun PowerUpShopCard(
    powerUp: PowerUpType,
    count: Int,
    onBuy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0x33000000),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = powerUp.iconEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${powerUp.displayName} (Owned: $count)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = powerUp.description,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }

            val priceText = if (powerUp.coinCost > 0) "🪙 ${powerUp.coinCost}" else "💎 ${powerUp.gemCost}"
            GameButton(
                text = priceText,
                gradientColors = listOf(Color(0xFFFFB300), Color(0xFFEF6C00)),
                onClick = onBuy,
                modifier = Modifier
                    .height(38.dp)
                    .width(96.dp),
                testTag = "btn_buy_powerup_${powerUp.name}"
            )
        }
    }
}
