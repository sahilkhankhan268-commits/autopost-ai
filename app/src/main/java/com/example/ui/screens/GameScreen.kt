package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ContainerState
import com.example.model.FruitType
import com.example.model.GameMode
import com.example.model.PowerUpType
import com.example.ui.components.CurrencyChip
import com.example.ui.components.FruitJarView
import com.example.ui.components.GameButton
import com.example.ui.components.PowerUpsBar
import com.example.ui.components.StarRatingRow
import com.example.ui.render.Fruit3DRenderer
import com.example.viewmodel.ActiveGameUiState
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.MovingFruitAnimation
import com.example.viewmodel.Screen
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
    val state by viewModel.gameUiState.collectAsState()
    val userData by viewModel.userData.collectAsState()

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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Header Bar
            GameTopBar(
                state = state,
                userData = userData,
                onPauseClick = { viewModel.pauseGame() },
                onShopClick = { viewModel.navigateTo(Screen.SHOP) },
                onAddTubeClick = { viewModel.addExtraJar() }
            )

            // 2. Status / Move Counter / Timer Bar
            GameStatusBar(
                state = state,
                onAddExtraTime = { viewModel.addExtraTime(30) }
            )

            // 3. Central Puzzle Containers Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                ContainersGrid(
                    state = state,
                    onContainerClick = { viewModel.onContainerTapped(it) }
                )
            }

            // 4. Power-Up Toolbar
            PowerUpsBar(
                powerUpCounts = userData.powerUps,
                activeMode = state.activePowerUpMode,
                onUndoClick = { viewModel.useUndo() },
                onHammerClick = { viewModel.activateHammerMode() },
                onShuffleClick = { viewModel.useShuffle() },
                onBombClick = { viewModel.activateBombMode() },
                onHintClick = { viewModel.requestHint() }
            )
        }

        // Active PowerUp Mode Banner Overlay
        if (state.activePowerUpMode != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFD32F2F),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFEB3B)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 110.dp)
                    .shadow(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mode Active: ${state.activePowerUpMode?.displayName} ${state.activePowerUpMode?.iconEmoji}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(Tap jar to apply)",
                        color = Color(0xFFFFEB3B),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Toast message banner
        state.toastMessage?.let { msg ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF211545).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp, start = 20.dp, end = 20.dp)
                    .shadow(12.dp)
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }

        // 5. Victory Dialog
        if (state.isVictory) {
            VictoryDialog(
                state = state,
                onNextLevel = { viewModel.nextLevel(activity) },
                onReplay = { viewModel.restartCurrentLevel(activity) },
                onHome = { viewModel.navigateTo(Screen.HOME) }
            )
        }

        // 6. Defeat Dialog
        if (state.isDefeat) {
            DefeatDialog(
                state = state,
                onRestart = { viewModel.restartCurrentLevel(activity) },
                onAddExtraTime = { viewModel.addExtraTime(30) },
                onHome = { viewModel.navigateTo(Screen.HOME) }
            )
        }

        // 7. Pause Dialog
        if (state.isPaused) {
            PauseDialog(
                onResume = { viewModel.resumeGame() },
                onRestart = {
                    viewModel.resumeGame()
                    viewModel.restartCurrentLevel(activity)
                },
                onHome = {
                    viewModel.resumeGame()
                    viewModel.navigateTo(Screen.HOME)
                }
            )
        }
    }
}

@Composable
fun GameTopBar(
    state: ActiveGameUiState,
    userData: com.example.data.UserGameData,
    onPauseClick: () -> Unit,
    onShopClick: () -> Unit,
    onAddTubeClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pause button
        IconButton(
            onClick = onPauseClick,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
                .border(1.dp, Color(0x66FFFFFF), CircleShape)
                .testTag("btn_game_pause")
        ) {
            Text(text = "⏸️", fontSize = 18.sp)
        }

        // Level Title & Stars
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val levelNum = state.levelData?.levelNumber ?: 1
            val modeName = when (state.gameMode) {
                GameMode.CLASSIC -> "Level $levelNum"
                GameMode.DAILY_PUZZLE -> "Daily Puzzle"
                GameMode.CHALLENGE -> "Challenge #$levelNum"
                GameMode.ENDLESS -> "Wave $levelNum"
            }
            Text(
                text = modeName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            StarRatingRow(stars = state.earnedStars, starSize = 16.dp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add Tube Helper Booster
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF2E1A47),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFAB47BC)),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAddTubeClick() }
                    .testTag("btn_add_tube")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🧪", fontSize = 13.sp)
                    Text(text = "+Tube", color = Color(0xFFE1BEE7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Coins Chip
            CurrencyChip(
                icon = "🪙",
                amount = userData.coins,
                color = Color(0xFFFFD54F),
                bgColor = Color(0xFF2E2007),
                borderColor = Color(0xFFFFB300),
                onPlusClick = onShopClick,
                testTag = "game_coins_chip"
            )
        }
    }
}

@Composable
fun GameStatusBar(
    state: ActiveGameUiState,
    onAddExtraTime: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0x33FFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Moves: ${state.movesCount}${state.levelData?.moveLimit?.let { "/$it" } ?: ""}",
                color = Color(0xFFE0E0E0),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            state.timeRemainingSeconds?.let { time ->
                val mins = time / 60
                val secs = time % 60
                val timeFormatted = String.format("%02d:%02d", mins, secs)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { if (time <= 20) onAddExtraTime() }
                ) {
                    Text(
                        text = "⏳ $timeFormatted",
                        color = if (time <= 10) Color(0xFFFF5252) else Color(0xFFFFD54F),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    if (time <= 15) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.clip(CircleShape).clickable { onAddExtraTime() }
                        ) {
                            Text(
                                text = "+30s",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            state.levelData?.difficultyName?.let { diff ->
                Text(
                    text = "• $diff",
                    color = Color(0xFF80D8FF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ContainersGrid(
    state: ActiveGameUiState,
    onContainerClick: (Int) -> Unit
) {
    val count = state.containers.size
    val jarPositions = remember { mutableStateMapOf<Int, Pair<Offset, Size>>() }

    // Group containers into 1 or 2 balanced rows for stable positioning
    val rows: List<List<ContainerState>> = remember(count, state.containers) {
        if (count <= 4) {
            listOf(state.containers)
        } else {
            val half = (count + 1) / 2
            listOf(state.containers.take(half), state.containers.drop(half))
        }
    }

    val animProgress = remember(state.movingFruit?.id) { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(state.movingFruit?.id) {
        if (state.movingFruit != null) {
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 440,
                    easing = androidx.compose.animation.core.LinearEasing
                )
            )
        }
    }

    // Compute real-time landing impact pulse for destination jar stack
    val progressVal = animProgress.value
    val destImpactPulse = if (state.movingFruit != null && progressVal >= 0.76f) {
        val u = (progressVal - 0.76f) / 0.24f
        (exp((-4.8 * u).toDouble()) * sin((PI * u).toDouble())).toFloat() * 1.5f
    } else 0f

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (rowContainers in rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (container in rowContainers) {
                        val isSelected = state.selectedContainerId == container.id
                        val isSourceHint = state.hintMove?.first == container.id
                        val isDestHint = state.hintMove?.second == container.id
                        val isMoveSource = state.movingFruit?.fromContainerId == container.id
                        val isMoveDest = state.movingFruit?.toContainerId == container.id

                        FruitJarView(
                            container = container,
                            isSelected = isSelected,
                            isHintHighlighted = isSourceHint,
                            isTargetHint = isDestHint,
                            hiddenTopFruit = isMoveSource,
                            impactSquashPulse = if (isMoveDest) destImpactPulse else 0f,
                            onPositionChanged = { offset, w, h ->
                                jarPositions[container.id] = Pair(offset, Size(w, h))
                            },
                            onClick = { onContainerClick(container.id) }
                        )
                    }
                }
            }
        }

        // 3D Flying & Dropping Fruit Physics Overlay
        state.movingFruit?.let { movingAnim ->
            FruitFlightDropOverlay(
                movingAnim = movingAnim,
                animProgress = animProgress.value,
                jarPositions = jarPositions
            )
        }
    }
}

@Composable
fun FruitFlightDropOverlay(
    movingAnim: MovingFruitAnimation,
    animProgress: Float,
    jarPositions: Map<Int, Pair<Offset, Size>>
) {
    val fromPos = jarPositions[movingAnim.fromContainerId]
    val toPos = jarPositions[movingAnim.toContainerId]

    if (fromPos != null && toPos != null) {
        val (fromOffset, fromSize) = fromPos
        val (toOffset, toSize) = toPos

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val t: Float = animProgress

            val fruitRadius: Float = (fromSize.width - 20f) / 2f
            val totalSlots = movingAnim.totalCapacity.coerceAtLeast(4)
            val usableHeight = toSize.height - 42f
            val slotHeight = usableHeight / totalSlots

            // Coordinate calculations
            val sourceCenterX: Float = fromOffset.x + fromSize.width / 2f
            val sourceMouthY: Float = fromOffset.y + 16f

            val destCenterX: Float = toOffset.x + toSize.width / 2f
            val destMouthY: Float = toOffset.y + 16f
            val destSlotY: Float = toOffset.y + toSize.height - 22f - (movingAnim.targetSlotIndex + 0.5f) * slotHeight

            val currentCenter: Offset
            val scaleX: Float
            val scaleY: Float
            val rotationDeg: Float

            if (t <= 0.46f) {
                // Phase 1: Parabolic Flight Arc from Source to Destination Mouth
                val u = t / 0.46f
                // Smooth horizontal interpolation
                val uSmooth = u * u * (3f - 2f * u)
                val currentX = sourceCenterX + (destCenterX - sourceCenterX) * uSmooth

                // Base height + parabolic arc elevation
                val baseY = sourceMouthY + (destMouthY - sourceMouthY) * u
                val arcHeight = 75f + abs(destCenterX - sourceCenterX) * 0.12f
                val arcLift = 4f * u * (1f - u) * arcHeight
                val currentY = baseY - arcLift

                currentCenter = Offset(currentX, currentY)
                val flightDir = if (destCenterX >= sourceCenterX) 1f else -1f
                rotationDeg = sin((u * PI).toDouble()).toFloat() * 18f * flightDir
                scaleX = 1.02f
                scaleY = 1.02f
            } else if (t <= 0.76f) {
                // Phase 2: Accelerated Gravity Drop into Destination Jar
                val u = (t - 0.46f) / 0.30f
                // Quadratic gravity acceleration
                val g = u * u * (1.18f - 0.18f * u)
                val currentX = destCenterX
                val currentY = destMouthY + (destSlotY - destMouthY) * g

                currentCenter = Offset(currentX, currentY)
                rotationDeg = (1f - u) * (if (destCenterX >= sourceCenterX) 8f else -8f)

                // Air-resistance velocity elongation (stretch in Y, narrow in X)
                val stretch = sin((u * PI).toDouble()).toFloat()
                scaleY = 1.0f + stretch * 0.22f
                scaleX = 1.0f - stretch * 0.16f
            } else {
                // Phase 3: Immediate Tactile Squishy Squash-and-Stretch Deformation upon landing
                val u = (t - 0.76f) / 0.24f

                // Non-linear damped elastoplastic oscillation for juicy tactile sensation
                val decay = exp((-5.0 * u).toDouble()).toFloat()
                val oscillation = cos((3.6 * PI * u).toDouble()).toFloat()
                val squashFactor = 0.44f * decay * oscillation

                // Volume-conserving asymmetric deformation (squash Y down to ~0.66, stretch X out to ~1.38)
                scaleY = (1.0f - squashFactor).coerceIn(0.64f, 1.22f)
                scaleX = (1.0f + squashFactor * 1.12f).coerceIn(0.82f, 1.40f)

                // Grounded bottom contact alignment: center shifts down when compressed so bottom stays planted
                val groundedOffsetY = if (scaleY < 1.0f) {
                    (1.0f - scaleY) * fruitRadius * 0.50f
                } else {
                    (1.0f - scaleY) * fruitRadius * 0.20f
                }

                currentCenter = Offset(destCenterX, destSlotY + groundedOffsetY)
                rotationDeg = 3.5f * decay * sin((3.6 * PI * u).toDouble()).toFloat()

                // Draw Juicy Splash Droplets & Expanding Ripple Ring on Impact
                val splashColor = getFruitSplashColor(movingAnim.fruit.type)
                val particleProgress = u
                val splashAlpha = (1f - particleProgress) * (1f - particleProgress)

                if (splashAlpha > 0.05f) {
                    val splashOriginY = destSlotY + fruitRadius * 0.45f
                    // 1. Soft impact ripple oval ring expanding laterally
                    val ringRadiusW = fruitRadius * (0.85f + particleProgress * 1.05f)
                    val ringRadiusH = fruitRadius * (0.32f + particleProgress * 0.42f)
                    drawOval(
                        color = splashColor.copy(alpha = splashAlpha * 0.65f),
                        topLeft = Offset(destCenterX - ringRadiusW, splashOriginY - ringRadiusH),
                        size = Size(ringRadiusW * 2f, ringRadiusH * 2f),
                        style = Stroke(width = 3.5f * (1f - particleProgress))
                    )

                    // 2. 8 Bursting juicy droplets
                    for (i in 0 until 8) {
                        val angle = (i * (PI * 2.0 / 8.0) + 0.2).toFloat()
                        val dist = particleProgress * (fruitRadius * 0.90f + (i % 3) * 7f)
                        val px = destCenterX + cos(angle.toDouble()).toFloat() * dist
                        val py = splashOriginY + sin(angle.toDouble()).toFloat() * dist * 0.55f - particleProgress * 14f * (1f - particleProgress)
                        val pRadius = (3.8f * (1f - particleProgress * 0.5f)).coerceAtLeast(1f)

                        drawCircle(
                            color = splashColor.copy(alpha = splashAlpha * 0.90f),
                            radius = pRadius,
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // Draw 3D Fruit with exact physics deformation
            Fruit3DRenderer.draw3DFruit(
                drawScope = this,
                fruit = movingAnim.fruit,
                center = currentCenter,
                radius = fruitRadius,
                rotationDegrees = rotationDeg,
                scaleX = scaleX,
                scaleY = scaleY,
                sparklePhase = 0f
            )
        }
    }
}

private fun getFruitSplashColor(type: com.example.model.FruitType): Color = when (type) {
    com.example.model.FruitType.APPLE_RED -> Color(0xFFFF3B30)
    com.example.model.FruitType.APPLE_GREEN -> Color(0xFF7CB342)
    com.example.model.FruitType.ORANGE -> Color(0xFFFF9800)
    com.example.model.FruitType.LEMON -> Color(0xFFFFEB3B)
    com.example.model.FruitType.BANANA -> Color(0xFFFFEE58)
    com.example.model.FruitType.KIWI -> Color(0xFF8BC34A)
    com.example.model.FruitType.PEAR -> Color(0xFFC0CA33)
    com.example.model.FruitType.PEACH -> Color(0xFFFF8A65)
    com.example.model.FruitType.STRAWBERRY -> Color(0xFFE91E63)
    com.example.model.FruitType.GRAPES -> Color(0xFFAB47BC)
    com.example.model.FruitType.WATERMELON -> Color(0xFFFF5252)
}

@Composable
fun VictoryDialog(
    state: ActiveGameUiState,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1E1145),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(28.dp))
                .testTag("dialog_victory")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 LEVEL COMPLETE!",
                    color = Color(0xFFFFD700),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Star Rating animation
                StarRatingRow(stars = state.earnedStars, starSize = 36.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // Rewards Earned
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF2E2007),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                    ) {
                        Text(
                            text = "🪙 +${state.earnedCoins}",
                            color = Color(0xFFFFD54F),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    if (state.earnedGems > 0) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF04243A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B0FF))
                        ) {
                            Text(
                                text = "💎 +${state.earnedGems}",
                                color = Color(0xFF80D8FF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Next Level Button
                GameButton(
                    text = "NEXT LEVEL ▶",
                    gradientColors = listOf(Color(0xFF00E676), Color(0xFF1B5E20)),
                    onClick = onNextLevel,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_victory_next"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GameButton(
                        text = "REPLAY 🔄",
                        gradientColors = listOf(Color(0xFF42A5F5), Color(0xFF1565C0)),
                        onClick = onReplay,
                        modifier = Modifier.weight(1f),
                        testTag = "btn_victory_replay"
                    )

                    GameButton(
                        text = "HOME 🏠",
                        gradientColors = listOf(Color(0xFF7E57C2), Color(0xFF311B92)),
                        onClick = onHome,
                        modifier = Modifier.weight(1f),
                        testTag = "btn_victory_home"
                    )
                }
            }
        }
    }
}

@Composable
fun DefeatDialog(
    state: ActiveGameUiState,
    onRestart: () -> Unit,
    onAddExtraTime: () -> Unit,
    onHome: () -> Unit
) {
    val isTimeOut = state.timeRemainingSeconds != null && state.timeRemainingSeconds <= 0
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF24101A),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF5252)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(28.dp))
                .testTag("dialog_defeat")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isTimeOut) "⏳ TIME'S UP!" else "💔 OUT OF MOVES",
                    color = Color(0xFFFF5252),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isTimeOut) "You ran out of time! Add +30s to keep playing or try again."
                    else "Don't give up! Use power-ups like Hammer or Undo to solve challenging levels.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isTimeOut) {
                    GameButton(
                        text = "+30s EXTRA TIME ⏱️",
                        gradientColors = listOf(Color(0xFF00E676), Color(0xFF1B5E20)),
                        onClick = onAddExtraTime,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_defeat_extra_time"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                GameButton(
                    text = "TRY AGAIN 🔄",
                    gradientColors = listOf(Color(0xFFFF7043), Color(0xFFD84315)),
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_defeat_retry"
                )

                Spacer(modifier = Modifier.height(12.dp))

                GameButton(
                    text = "MAIN MENU 🏠",
                    gradientColors = listOf(Color(0xFF5C6BC0), Color(0xFF283593)),
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_defeat_home"
                )
            }
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1B1238),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF7E57C2)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .testTag("dialog_pause")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⏸️ GAME PAUSED",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(20.dp))

                GameButton(
                    text = "RESUME ▶",
                    gradientColors = listOf(Color(0xFF00E676), Color(0xFF1B5E20)),
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_pause_resume"
                )

                Spacer(modifier = Modifier.height(12.dp))

                GameButton(
                    text = "RESTART 🔄",
                    gradientColors = listOf(Color(0xFF42A5F5), Color(0xFF1565C0)),
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_pause_restart"
                )

                Spacer(modifier = Modifier.height(12.dp))

                GameButton(
                    text = "HOME 🏠",
                    gradientColors = listOf(Color(0xFF7E57C2), Color(0xFF311B92)),
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_pause_home"
                )
            }
        }
    }
}
