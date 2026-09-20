package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.UserGameData
import com.example.ui.components.GameButton
import com.example.viewmodel.GameViewModel

@Composable
fun SettingsDialog(
    viewModel: GameViewModel,
    userData: UserGameData,
    onDismiss: () -> Unit
) {
    var showHowToPlay by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1B1238),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF7E57C2)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(28.dp))
                .testTag("dialog_settings")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ SETTINGS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("btn_settings_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sound Toggle
                SettingToggleRow(
                    title = "🔊 Sound Effects",
                    checked = userData.soundEnabled,
                    onCheckedChange = { viewModel.toggleSound(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Music Toggle
                SettingToggleRow(
                    title = "🎵 Background Music",
                    checked = userData.musicEnabled,
                    onCheckedChange = { viewModel.toggleMusic(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Haptic Toggle
                SettingToggleRow(
                    title = "📳 Haptic Vibration",
                    checked = userData.hapticEnabled,
                    onCheckedChange = { viewModel.toggleHaptic(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // How to Play Button
                GameButton(
                    text = if (showHowToPlay) "HIDE RULES 📖" else "HOW TO PLAY 📖",
                    gradientColors = listOf(Color(0xFF5C6BC0), Color(0xFF283593)),
                    onClick = { showHowToPlay = !showHowToPlay },
                    modifier = Modifier.fillMaxWidth()
                )

                if (showHowToPlay) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF281754),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🍎 FRUIT SORT 3D RULES:",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1. Tap any glass jar to lift its top fruit.\n" +
                                        "2. Tap another jar to drop the fruit into it.\n" +
                                        "3. You can only place fruit into an empty jar OR on top of a matching fruit type.\n" +
                                        "4. Fill each jar with 4 identical fruits to sort it and claim victory!\n" +
                                        "5. Use Undo, Hammer, Shuffle, and Bomb when stuck.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reset Progress
                if (!showResetConfirm) {
                    GameButton(
                        text = "RESET PROGRESS ⚠️",
                        gradientColors = listOf(Color(0xFFE53935), Color(0xFFB71C1C)),
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF4A0E17),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Are you sure you want to reset all levels, stars, coins, and power-ups?",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GameButton(
                                    text = "CONFIRM 🗑️",
                                    gradientColors = listOf(Color(0xFFD32F2F), Color(0xFFB71C1C)),
                                    onClick = {
                                        viewModel.resetAllProgress()
                                        showResetConfirm = false
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                GameButton(
                                    text = "CANCEL",
                                    gradientColors = listOf(Color(0xFF616161), Color(0xFF424242)),
                                    onClick = { showResetConfirm = false },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF281754),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFFFD54F),
                    checkedTrackColor = Color(0xFF00E676),
                    uncheckedThumbColor = Color(0xFFBDBDBD),
                    uncheckedTrackColor = Color(0xFF424242)
                )
            )
        }
    }
}
