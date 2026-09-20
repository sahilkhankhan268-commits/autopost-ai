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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserGameData
import com.example.model.GameMode
import com.example.ui.components.GameButton
import com.example.ui.components.TopCurrencyBar
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun ChallengeScreen(
    viewModel: GameViewModel,
    userData: UserGameData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF260D07),
                        Color(0xFF3E160C),
                        Color(0xFF190602)
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
                        .testTag("btn_challenge_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "🔥 CHALLENGES",
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

            Spacer(modifier = Modifier.height(10.dp))

            // Endless Mode Banner
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF4E1D10),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF7043)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(10.dp, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "♾️ ENDLESS FRUIT SURVIVAL",
                        color = Color(0xFFFFCCBC),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sort through continuous procedural waves of mixed fruits. See how high of a wave you can reach!",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    GameButton(
                        text = "START ENDLESS WAVE 1 ▶",
                        gradientColors = listOf(Color(0xFFFF7043), Color(0xFFD84315)),
                        onClick = { viewModel.startLevel(1, GameMode.ENDLESS) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_play_endless"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "⚡ SPEED & MOVE TRIALS",
                color = Color(0xFFFFD54F),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Trial tiers 1 to 6
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val trials = listOf(
                    1 to "Novice Rush (60s limit)",
                    2 to "Apprentice Sorter (50s limit)",
                    3 to "Speedy Citrus (45s limit)",
                    4 to "Tight Moves Master (Strict limit)",
                    5 to "Turbo Berry Blitz (35s limit)",
                    6 to "Grandmaster Trial (Ultra tight)"
                )

                for ((tier, title) in trials) {
                    ChallengeTierCard(
                        tier = tier,
                        title = title,
                        onPlay = { viewModel.startLevel(tier, GameMode.CHALLENGE) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ChallengeTierCard(
    tier: Int,
    title: String,
    onPlay: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF38140B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FF7043)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tier $tier: $title",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Reward: 🪙 +${tier * 100}  💎 +${tier * 4}",
                    color = Color(0xFFFFD54F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            GameButton(
                text = "PLAY ▶",
                gradientColors = listOf(Color(0xFFFF9800), Color(0xFFE65100)),
                onClick = onPlay,
                modifier = Modifier
                    .height(44.dp)
                    .width(100.dp),
                testTag = "btn_challenge_tier_$tier"
            )
        }
    }
}
