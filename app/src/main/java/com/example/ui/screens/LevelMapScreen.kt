package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.GameMode
import com.example.ui.components.StarRatingRow
import com.example.ui.components.TopCurrencyBar
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun LevelMapScreen(
    viewModel: GameViewModel,
    userData: UserGameData,
    modifier: Modifier = Modifier
) {
    // 1000 levels divided into 10 Chapters of 100 levels each
    val currentChapter = ((userData.highestLevelUnlocked - 1) / 100).coerceIn(0, 9)
    var selectedChapter by remember { mutableIntStateOf(currentChapter) }

    val startLevel = selectedChapter * 100 + 1
    val endLevel = (selectedChapter + 1) * 100
    val totalStars = userData.levelStars.values.sum()

    val bgTop = userData.selectedBackground.topColor
    val bgMid = userData.selectedBackground.midColor
    val bgBot = userData.selectedBackground.botColor

    val gridState = rememberLazyGridState()

    LaunchedEffect(selectedChapter) {
        // Scroll to the user's highest unlocked level if in this chapter
        val highest = userData.highestLevelUnlocked
        if (highest in startLevel..endLevel) {
            val idx = (highest - startLevel).coerceAtLeast(0)
            gridState.scrollToItem(idx)
        } else {
            gridState.scrollToItem(0)
        }
    }

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
        Column(modifier = Modifier.fillMaxSize()) {
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
                        .testTag("btn_levelmap_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🏆 1000 LEVEL SAGA",
                        color = Color(0xFFFFD54F),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Stars",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalStars / 3000 Stars",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(modifier = Modifier.size(44.dp))
            }

            TopCurrencyBar(
                userData = userData,
                onShopClick = { viewModel.navigateTo(Screen.SHOP) },
                onSettingsClick = { viewModel.openSettings() },
                showSettings = false
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 10 Chapter Selectors (1-100, 101-200, ..., 901-1000)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(10) { chapterIdx ->
                    val chStart = chapterIdx * 100 + 1
                    val chEnd = (chapterIdx + 1) * 100
                    val isChUnlocked = userData.highestLevelUnlocked >= chStart
                    val isSelected = selectedChapter == chapterIdx

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isSelected -> Color(0xFFFFD54F)
                            isChUnlocked -> Color(0x33FFFFFF)
                            else -> Color(0x15FFFFFF)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFFFC107) else Color(0x22FFFFFF)
                        ),
                        modifier = Modifier
                            .clickable { selectedChapter = chapterIdx }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isChUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "Ch ${chapterIdx + 1}: $chStart-$chEnd",
                                color = if (isSelected) Color(0xFF1E1002) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid of 100 Levels in Current Chapter (e.g. 1-100)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                state = gridState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(100) { offset ->
                    val level = startLevel + offset
                    val isUnlocked = level <= userData.highestLevelUnlocked
                    val isCurrent = level == userData.currentLevel
                    val stars = userData.levelStars[level] ?: 0
                    val isBoss = level % 25 == 0
                    val isMilestone = level % 50 == 0
                    val isMilestoneClaimed = userData.claimedMilestones.contains(level)

                    LevelTile(
                        level = level,
                        isUnlocked = isUnlocked,
                        isCurrent = isCurrent,
                        isBoss = isBoss,
                        isMilestone = isMilestone,
                        isMilestoneClaimed = isMilestoneClaimed,
                        stars = stars,
                        onMilestoneClaim = { viewModel.claimMilestoneChest(level) },
                        onClick = {
                            if (isUnlocked) {
                                viewModel.startLevel(level, GameMode.CLASSIC)
                            } else {
                                viewModel.showToast("🔒 Complete Level ${level - 1} first!")
                            }
                        }
                    )
                }
            }
        }

        // Floating Action Button: Play Highest Unlocked Level
        FloatingActionButton(
            onClick = {
                viewModel.startLevel(userData.highestLevelUnlocked, GameMode.CLASSIC)
            },
            containerColor = Color(0xFFFF9800),
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .shadow(8.dp, RoundedCornerShape(18.dp))
                .testTag("btn_resume_level")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PLAY LVL ${userData.highestLevelUnlocked}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun LevelTile(
    level: Int,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    isBoss: Boolean,
    isMilestone: Boolean,
    isMilestoneClaimed: Boolean,
    stars: Int,
    onMilestoneClaim: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when {
            isCurrent -> Color(0xFFFF9800)
            isBoss && isUnlocked -> Color(0xFF6A1B9A)
            isUnlocked -> Color(0xFF1E2659)
            else -> Color(0xFF131733)
        },
        border = androidx.compose.foundation.BorderStroke(
            width = if (isCurrent) 2.5.dp else if (isBoss && isUnlocked) 2.dp else 1.dp,
            color = when {
                isCurrent -> Color(0xFFFFEB3B)
                isBoss && isUnlocked -> Color(0xFFE040FB)
                isUnlocked -> Color(0xFF42A5F5)
                else -> Color(0x22FFFFFF)
            }
        ),
        modifier = Modifier
            .size(76.dp)
            .shadow(if (isUnlocked) 6.dp else 1.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("level_tile_$level")
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isUnlocked) {
                    if (isBoss) {
                        Text(text = "👑", fontSize = 11.sp)
                    }
                    Text(
                        text = "$level",
                        color = if (isCurrent) Color.Black else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (level >= 100) 15.sp else 17.sp
                    )
                    if (stars > 0) {
                        StarRatingRow(stars = stars, starSize = 10.dp)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0x66FFFFFF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$level",
                        color = Color(0x55FFFFFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Milestone Chest Gift Badge on top corner
            if (isMilestone && isUnlocked) {
                Surface(
                    shape = CircleShape,
                    color = if (isMilestoneClaimed) Color(0xFF388E3C) else Color(0xFFFFC107),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(20.dp)
                        .clickable { onMilestoneClaim() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = if (isMilestoneClaimed) "✓" else "🎁", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
