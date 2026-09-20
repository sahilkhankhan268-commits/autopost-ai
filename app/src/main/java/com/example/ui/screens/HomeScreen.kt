package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserGameData
import com.example.model.FruitItem
import com.example.model.FruitType
import com.example.model.GameMode
import com.example.ui.components.GameButton
import com.example.ui.components.TopCurrencyBar
import com.example.ui.render.Fruit3DRenderer
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    userData: UserGameData,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val heroTapScale = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "home_float")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val rotateAnim by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate"
    )

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Currency Header
            TopCurrencyBar(
                userData = userData,
                onShopClick = { viewModel.navigateTo(Screen.SHOP) },
                onCoinClick = {
                    viewModel.onCurrencyTap(false)
                    viewModel.navigateTo(Screen.SHOP)
                },
                onGemClick = {
                    viewModel.onCurrencyTap(true)
                    viewModel.navigateTo(Screen.SHOP)
                },
                onSettingsClick = { viewModel.openSettings() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Game Logo & 3D Fruit Hero Showcase (Interactive Tap Easter Egg)
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(heroTapScale.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.onFruitHeroTap()
                        coroutineScope.launch {
                            heroTapScale.animateTo(
                                targetValue = 1.18f,
                                animationSpec = tween(70, easing = FastOutSlowInEasing)
                            )
                            heroTapScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background soft radial aura
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x66FF9800), Color(0x33E91E63), Color.Transparent),
                            center = center,
                            radius = size.width * 0.65f
                        )
                    )

                    // 3D Apple & Orange & Banana floating
                    val r = size.width * 0.22f
                    Fruit3DRenderer.draw3DFruit(
                        drawScope = this,
                        fruit = FruitItem("hero_apple", FruitType.APPLE_RED),
                        center = Offset(center.x - 32f, center.y + floatAnim - 10f),
                        radius = r,
                        rotationDegrees = rotateAnim
                    )

                    Fruit3DRenderer.draw3DFruit(
                        drawScope = this,
                        fruit = FruitItem("hero_orange", FruitType.ORANGE),
                        center = Offset(center.x + 36f, center.y - floatAnim + 12f),
                        radius = r * 0.95f,
                        rotationDegrees = -rotateAnim
                    )

                    Fruit3DRenderer.draw3DFruit(
                        drawScope = this,
                        fruit = FruitItem("hero_banana", FruitType.BANANA),
                        center = Offset(center.x, center.y + 40f + floatAnim * 0.5f),
                        radius = r * 1.1f,
                        rotationDegrees = 12f
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF281754).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🍎 FRUIT SORT 3D",
                        color = Color(0xFFFFD54F),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Whole Fruit Puzzle",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Navigation Menu Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Play Button
                GameButton(
                    text = "▶ PLAY (LVL ${userData.currentLevel})",
                    leadingEmoji = "🍎",
                    gradientColors = listOf(Color(0xFF00E676), Color(0xFF1B5E20)),
                    onClick = { viewModel.startLevel(userData.currentLevel, GameMode.CLASSIC) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_home_play"
                )

                // Levels Button
                GameButton(
                    text = "🏆 LEVELS",
                    leadingEmoji = "🗺️",
                    gradientColors = listOf(Color(0xFF29B6F6), Color(0xFF0D47A1)),
                    onClick = { viewModel.navigateTo(Screen.LEVEL_MAP) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_home_levels"
                )

                // Daily Puzzle Button
                GameButton(
                    text = "🎯 DAILY PUZZLE",
                    leadingEmoji = "🔥",
                    gradientColors = listOf(Color(0xFFAB47BC), Color(0xFF4A148C)),
                    onClick = { viewModel.navigateTo(Screen.DAILY_PUZZLE) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_home_daily"
                )

                // Challenge Button
                GameButton(
                    text = "🔥 CHALLENGE",
                    leadingEmoji = "⚡",
                    gradientColors = listOf(Color(0xFFFF7043), Color(0xFFBF360C)),
                    onClick = { viewModel.navigateTo(Screen.CHALLENGE) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_home_challenge"
                )

                // Shop Button
                GameButton(
                    text = "🛍 SHOP",
                    leadingEmoji = "✨",
                    gradientColors = listOf(Color(0xFFFFCA28), Color(0xFFE65100)),
                    onClick = { viewModel.navigateTo(Screen.SHOP) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_home_shop"
                )

                // Achievements Button
                GameButton(
                    text = "🏅 ACHIEVEMENTS",
                    leadingEmoji = "👑",
                    gradientColors = listOf(Color(0xFF26A69A), Color(0xFF004D40)),
                    onClick = { viewModel.navigateTo(Screen.ACHIEVEMENTS) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_home_achievements"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
