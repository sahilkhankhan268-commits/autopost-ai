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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserGameData
import com.example.model.GameMode
import com.example.ui.components.GameButton
import com.example.ui.components.TopCurrencyBar
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen
import java.time.LocalDate

@Composable
fun DailyPuzzleScreen(
    viewModel: GameViewModel,
    userData: UserGameData,
    modifier: Modifier = Modifier
) {
    val todayEpoch = LocalDate.now().toEpochDay()
    val isClaimedToday = userData.lastDailyClaimEpochDay >= todayEpoch

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF190B38),
                        Color(0xFF281155),
                        Color(0xFF0F0523)
                    )
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
            // Header
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
                        .testTag("btn_daily_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "🎯 DAILY PUZZLE",
                    color = Color(0xFFFFD54F),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Box(modifier = Modifier.size(44.dp))
            }

            TopCurrencyBar(
                userData = userData,
                onShopClick = { viewModel.navigateTo(Screen.SHOP) },
                onSettingsClick = { viewModel.openSettings() },
                showSettings = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 7-Day Streak Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF2E195E),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFBA68C8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(10.dp, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 Daily Streak: ${userData.dailyStreak} Days",
                            color = Color(0xFFFFD54F),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isClaimedToday) "✅ Claimed" else "🎁 Claim Today",
                            color = if (isClaimedToday) Color(0xFF00E676) else Color(0xFFFFEB3B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7 Day Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val streakRewards = listOf(
                            50 to 1,
                            75 to 1,
                            100 to 2,
                            150 to 2,
                            200 to 3,
                            300 to 4,
                            500 to 10
                        )

                        for (day in 1..7) {
                            val (coins, gems) = streakRewards[day - 1]
                            val isDone = day < userData.dailyStreak || (day == userData.dailyStreak && isClaimedToday)
                            val isCurrent = day == userData.dailyStreak && !isClaimedToday

                            DailyDayCard(
                                day = day,
                                coins = coins,
                                gems = gems,
                                isDone = isDone,
                                isCurrent = isCurrent,
                                onClaim = {
                                    if (isCurrent) {
                                        viewModel.claimDailyStreakReward(day, coins, gems)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Daily Puzzle Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF3B1E78),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🌟 TODAY'S EXCLUSIVE PUZZLE",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "A special handcrafted daily puzzle refreshed every 24 hours. Solve it to earn massive bonus Coins & Gems!",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF2E2007),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                        ) {
                            Text(
                                text = "🪙 +150 Coins",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF04243A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B0FF))
                        ) {
                            Text(
                                text = "💎 +5 Gems",
                                color = Color(0xFF80D8FF),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    GameButton(
                        text = "PLAY DAILY PUZZLE ▶",
                        gradientColors = listOf(Color(0xFFAB47BC), Color(0xFF4A148C)),
                        onClick = { viewModel.startLevel(1, GameMode.DAILY_PUZZLE) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_play_daily_puzzle"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DailyDayCard(
    day: Int,
    coins: Int,
    gems: Int,
    isDone: Boolean,
    isCurrent: Boolean,
    onClaim: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when {
            isDone -> Color(0xFF1B5E20)
            isCurrent -> Color(0xFFFF9800)
            else -> Color(0xFF1F113F)
        },
        border = androidx.compose.foundation.BorderStroke(
            width = if (isCurrent) 2.dp else 1.dp,
            color = if (isCurrent) Color(0xFFFFEB3B) else Color(0x44FFFFFF)
        ),
        modifier = Modifier
            .size(42.dp, 66.dp)
            .clickable { onClaim() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = "D$day",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = if (day == 7) "👑" else "🪙",
                    fontSize = 14.sp
                )
            }

            Text(
                text = "+$coins",
                color = Color(0xFFFFD54F),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
