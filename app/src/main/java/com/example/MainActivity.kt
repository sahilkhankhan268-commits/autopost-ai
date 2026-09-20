package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ads.AdManager
import com.example.ads.AdMobBannerView
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.ChallengeScreen
import com.example.ui.screens.DailyPuzzleScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelMapScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.ShopScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize Google AdMob SDK
        AdManager.initialize(this)

        setContent {
            MyApplicationTheme {
                FruitSort3DApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.soundManager.musicEnabled) {
            viewModel.soundManager.startBackgroundMusic()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.soundManager.stopBackgroundMusic()
    }
}

@Composable
fun FruitSort3DApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val showSettings by viewModel.showSettingsDialog.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F0826)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = currentScreen,
                label = "screen_transition",
                modifier = if (currentScreen != Screen.GAME) Modifier.padding(bottom = 50.dp) else Modifier
            ) { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(viewModel = viewModel, userData = userData)
                    Screen.GAME -> GameScreen(viewModel = viewModel)
                    Screen.LEVEL_MAP -> LevelMapScreen(viewModel = viewModel, userData = userData)
                    Screen.DAILY_PUZZLE -> DailyPuzzleScreen(viewModel = viewModel, userData = userData)
                    Screen.CHALLENGE -> ChallengeScreen(viewModel = viewModel, userData = userData)
                    Screen.SHOP -> ShopScreen(viewModel = viewModel, userData = userData)
                    Screen.ACHIEVEMENTS -> AchievementsScreen(viewModel = viewModel, userData = userData)
                }
            }

            // Bottom Banner Ad: shown on menu screens without overlapping gameplay
            if (currentScreen != Screen.GAME) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                ) {
                    AdMobBannerView()
                }
            }

            if (showSettings) {
                SettingsDialog(
                    viewModel = viewModel,
                    userData = userData,
                    onDismiss = { viewModel.closeSettings() }
                )
            }
        }
    }
}

