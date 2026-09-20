package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.PowerUpType

@Composable
fun PowerUpsBar(
    powerUpCounts: Map<PowerUpType, Int>,
    activeMode: PowerUpType?,
    onUndoClick: () -> Unit,
    onHammerClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onBombClick: () -> Unit,
    onHintClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E153B).copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x33FFFFFF)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PowerUpButton(
                powerUp = PowerUpType.UNDO,
                count = powerUpCounts[PowerUpType.UNDO] ?: 0,
                isActive = activeMode == PowerUpType.UNDO,
                onClick = onUndoClick,
                testTag = "btn_powerup_undo"
            )

            PowerUpButton(
                powerUp = PowerUpType.HAMMER,
                count = powerUpCounts[PowerUpType.HAMMER] ?: 0,
                isActive = activeMode == PowerUpType.HAMMER,
                onClick = onHammerClick,
                testTag = "btn_powerup_hammer"
            )

            PowerUpButton(
                powerUp = PowerUpType.SHUFFLE,
                count = powerUpCounts[PowerUpType.SHUFFLE] ?: 0,
                isActive = activeMode == PowerUpType.SHUFFLE,
                onClick = onShuffleClick,
                testTag = "btn_powerup_shuffle"
            )

            PowerUpButton(
                powerUp = PowerUpType.BOMB,
                count = powerUpCounts[PowerUpType.BOMB] ?: 0,
                isActive = activeMode == PowerUpType.BOMB,
                onClick = onBombClick,
                testTag = "btn_powerup_bomb"
            )

            PowerUpButton(
                powerUp = PowerUpType.HINT,
                count = powerUpCounts[PowerUpType.HINT] ?: 0,
                isActive = activeMode == PowerUpType.HINT,
                onClick = onHintClick,
                testTag = "btn_powerup_hint"
            )
        }
    }
}

@Composable
fun PowerUpButton(
    powerUp: PowerUpType,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (isActive) Brush.radialGradient(listOf(Color(0xFFFF5252), Color(0xFFB71C1C)))
                    else Brush.verticalGradient(listOf(Color(0xFF3F2B96), Color(0xFF231651)))
                )
                .border(
                    width = if (isActive) 2.5.dp else 1.5.dp,
                    color = if (isActive) Color(0xFFFFD54F) else Color(0x66FFFFFF),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = powerUp.iconEmoji, fontSize = 22.sp)

            // Badge Count (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (count > 0) Color(0xFF4CAF50) else Color(0xFFFF9800))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 0) "$count" else "+",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = powerUp.displayName,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Price indicator if count is 0
        if (count == 0) {
            Text(
                text = if (powerUp.coinCost > 0) "🪙${powerUp.coinCost}" else "💎${powerUp.gemCost}",
                color = Color(0xFFFFD54F),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
