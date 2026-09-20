package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.audio.SoundManager
import com.example.data.AchievementItem
import com.example.data.GameRepository
import com.example.data.UserGameData
import com.example.game.LevelData
import com.example.game.LevelGenerator
import com.example.model.ContainerState
import com.example.model.ContainerStyle
import com.example.model.FruitItem
import com.example.model.FruitSkinPack
import com.example.model.GameMode
import com.example.model.MoveRecord
import com.example.model.PowerUpType
import com.example.model.SpecialFruit
import com.example.model.ThemeBackground
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

enum class Screen {
    HOME,
    GAME,
    LEVEL_MAP,
    DAILY_PUZZLE,
    CHALLENGE,
    SHOP,
    ACHIEVEMENTS
}

data class MovingFruitAnimation(
    val fruit: FruitItem,
    val fromContainerId: Int,
    val toContainerId: Int,
    val targetSlotIndex: Int = 0,
    val totalCapacity: Int = 4,
    val id: String = UUID.randomUUID().toString()
)

data class ActiveGameUiState(
    val levelData: LevelData? = null,
    val gameMode: GameMode = GameMode.CLASSIC,
    val containers: List<ContainerState> = emptyList(),
    val selectedContainerId: Int? = null,
    val movingFruit: MovingFruitAnimation? = null,
    val movesCount: Int = 0,
    val timeRemainingSeconds: Int? = null,
    val isVictory: Boolean = false,
    val isDefeat: Boolean = false,
    val activePowerUpMode: PowerUpType? = null,
    val hintMove: Pair<Int, Int>? = null,
    val earnedStars: Int = 3,
    val earnedCoins: Int = 0,
    val earnedGems: Int = 0,
    val isPaused: Boolean = false,
    val usedUndoThisLevel: Boolean = false,
    val justCompletedJarId: Int? = null,
    val toastMessage: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val repository = GameRepository(application)
    val soundManager = SoundManager(application)

    val userData: StateFlow<UserGameData> = repository.userData

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _gameUiState = MutableStateFlow(ActiveGameUiState())
    val gameUiState: StateFlow<ActiveGameUiState> = _gameUiState.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private var timerJob: Job? = null
    private val moveHistory = mutableListOf<MoveRecord>()

    init {
        // Sync audio settings with repository and start background music
        val data = repository.userData.value
        soundManager.soundEnabled = data.soundEnabled
        soundManager.musicEnabled = data.musicEnabled
        soundManager.hapticEnabled = data.hapticEnabled
        if (data.musicEnabled) {
            soundManager.startBackgroundMusic()
        }
    }

    fun navigateTo(screen: Screen) {
        when (screen) {
            Screen.LEVEL_MAP -> soundManager.playMenuTabSound("levels")
            Screen.DAILY_PUZZLE -> soundManager.playMenuTabSound("daily")
            Screen.CHALLENGE -> soundManager.playMenuTabSound("challenge")
            Screen.SHOP -> soundManager.playMenuTabSound("shop")
            Screen.ACHIEVEMENTS -> soundManager.playMenuTabSound("achievements")
            else -> soundManager.playButtonClick()
        }
        _currentScreen.value = screen
    }

    fun onFruitHeroTap() {
        soundManager.playFruitEasterEggTap()
    }

    fun onCurrencyTap(isGems: Boolean) {
        if (isGems) {
            soundManager.playGemCollect()
        } else {
            soundManager.playCoinCollect()
        }
    }

    fun openSettings() {
        soundManager.playButtonClick()
        _showSettingsDialog.value = true
    }

    fun closeSettings() {
        soundManager.playButtonClick()
        _showSettingsDialog.value = false
    }

    fun toggleSound(enabled: Boolean) {
        soundManager.soundEnabled = enabled
        soundManager.playToggleSound(enabled)
        repository.updateSettings(sound = enabled, music = soundManager.musicEnabled, haptic = soundManager.hapticEnabled)
    }

    fun toggleMusic(enabled: Boolean) {
        soundManager.musicEnabled = enabled
        soundManager.playToggleSound(enabled)
        repository.updateSettings(sound = soundManager.soundEnabled, music = enabled, haptic = soundManager.hapticEnabled)
    }

    fun toggleHaptic(enabled: Boolean) {
        soundManager.hapticEnabled = enabled
        soundManager.playToggleSound(enabled)
        repository.updateSettings(sound = soundManager.soundEnabled, music = soundManager.musicEnabled, haptic = enabled)
    }

    fun startLevel(levelNumber: Int, mode: GameMode = GameMode.CLASSIC) {
        soundManager.playPlayGame()
        moveHistory.clear()
        timerJob?.cancel()

        val style = userData.value.selectedStyle
        val levelData = when (mode) {
            GameMode.CLASSIC -> LevelGenerator.generateLevel(levelNumber, style)
            GameMode.DAILY_PUZZLE -> LevelGenerator.generateDailyPuzzle(LocalDate.now().toEpochDay(), style)
            GameMode.CHALLENGE -> LevelGenerator.generateChallengeLevel(levelNumber, style)
            GameMode.ENDLESS -> LevelGenerator.generateEndlessRound(levelNumber, style)
        }

        _gameUiState.value = ActiveGameUiState(
            levelData = levelData,
            gameMode = mode,
            containers = levelData.containers,
            timeRemainingSeconds = levelData.timeLimitSeconds,
            earnedCoins = levelData.coinReward,
            earnedGems = levelData.gemReward
        )

        _currentScreen.value = Screen.GAME

        if (levelData.timeLimitSeconds != null) {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _gameUiState.value
                if (state.isVictory || state.isDefeat || state.isPaused) continue
                val current = state.timeRemainingSeconds ?: break
                if (current <= 10 && current > 1) {
                    soundManager.playTimerTick()
                }
                if (current <= 1) {
                    _gameUiState.value = state.copy(timeRemainingSeconds = 0, isDefeat = true)
                    soundManager.playFruitDrop()
                    break
                } else {
                    _gameUiState.value = state.copy(timeRemainingSeconds = current - 1)
                }
            }
        }
    }

    fun addExtraTime(seconds: Int = 30) {
        soundManager.playCoinCollect()
        val current = _gameUiState.value.timeRemainingSeconds ?: 60
        val wasDefeat = _gameUiState.value.isDefeat
        _gameUiState.value = _gameUiState.value.copy(
            timeRemainingSeconds = current + seconds,
            isDefeat = false
        )
        showToast("⏱️ +$seconds Seconds Added!")
        if (wasDefeat) {
            startTimer()
        }
    }

    fun addExtraJar() {
        val state = _gameUiState.value
        if (state.containers.size >= 10 || state.isVictory || state.isDefeat) return
        if (repository.spendCoins(50)) {
            soundManager.playBottleCompleted()
            val newId = (state.containers.maxOfOrNull { it.id } ?: 0) + 1
            val newJar = ContainerState(id = newId, capacity = 4, fruits = emptyList(), style = userData.value.selectedStyle)
            val updated = state.containers + newJar
            _gameUiState.value = state.copy(containers = updated)
            showToast("🧪 Extra Empty Tube Added!")
        } else {
            showToast("Need 50 Coins for Extra Tube!")
        }
    }

    private fun checkBoardCompleted(containers: List<ContainerState>): Boolean {
        val nonEmpties = containers.filter { !it.isEmpty }
        // If all containers were cleared/bombed -> Instant Victory!
        if (nonEmpties.isEmpty()) return true

        // Standard check: all non-empty jars are full and sorted
        if (nonEmpties.all { it.isSorted }) return true

        // Universal tool-aware check:
        // All non-empty jars are homogeneous (each jar has only 1 fruit type, not locked/frozen),
        // and no two jars have the same fruit type
        val allHomogeneous = nonEmpties.all { it.isHomogeneous && !it.isLocked }
        if (allHomogeneous) {
            val fruitTypes = nonEmpties.map { it.fruits.first().type }
            if (fruitTypes.size == fruitTypes.distinct().size) {
                return true
            }
        }
        return false
    }

    private fun checkAndApplyVictory(updatedContainers: List<ContainerState>, movesCount: Int = _gameUiState.value.movesCount) {
        val isWon = checkBoardCompleted(updatedContainers)
        if (isWon) {
            soundManager.playLevelWin()
            timerJob?.cancel()

            val activeJars = updatedContainers.filter { !it.isEmpty }
            val totalFruits = (activeJars.size * 4).coerceAtLeast(4)
            val baseline = (totalFruits * 1.5).toInt()
            val stars = when {
                movesCount <= baseline -> 3
                movesCount <= baseline * 1.5 -> 2
                else -> 1
            }

            val lvlData = _gameUiState.value.levelData
            if (lvlData != null && _gameUiState.value.gameMode == GameMode.CLASSIC) {
                repository.onLevelComplete(
                    level = lvlData.levelNumber,
                    starsEarned = stars,
                    coinsEarned = lvlData.coinReward,
                    gemsEarned = lvlData.gemReward,
                    usedUndo = _gameUiState.value.usedUndoThisLevel
                )
            }

            _gameUiState.value = _gameUiState.value.copy(
                containers = updatedContainers,
                isVictory = true,
                isDefeat = false,
                earnedStars = stars,
                activePowerUpMode = null,
                selectedContainerId = null
            )
        }
    }

    fun onContainerTapped(containerId: Int) {
        val state = _gameUiState.value
        if (state.isVictory || state.isDefeat || state.isPaused || state.movingFruit != null) return

        val targetContainer = state.containers.find { it.id == containerId } ?: return

        // 1. Handle Active Power-Up Clicks
        if (state.activePowerUpMode != null) {
            handlePowerUpClick(containerId, state.activePowerUpMode)
            return
        }

        if (targetContainer.isLocked) {
            showToast("🔒 This container is locked! Sort other containers to unlock.")
            return
        }

        val selectedId = state.selectedContainerId

        if (selectedId == null) {
            // First Tap: Select source jar
            if (targetContainer.isEmpty) return
            if (targetContainer.isSorted) {
                showToast("✨ This jar is already sorted!")
                return
            }
            val topFruit = targetContainer.topFruit
            if (topFruit?.isFrozen == true) {
                // Thaw ice automatically on tap!
                soundManager.playIceCrack()
                val updatedContainers = state.containers.map { jar ->
                    if (jar.id == containerId) {
                        jar.copy(fruits = jar.fruits.mapIndexed { idx, f ->
                            if (idx == jar.fruits.size - 1) f.copy(isFrozen = false, special = SpecialFruit.NORMAL) else f
                        })
                    } else jar
                }
                _gameUiState.value = state.copy(
                    containers = updatedContainers,
                    selectedContainerId = containerId,
                    hintMove = null
                )
                showToast("🧊 Ice Shattered & Fruit Thawed!")
                return
            }
            _gameUiState.value = state.copy(selectedContainerId = containerId, hintMove = null)
            soundManager.playFruitPickup()
        } else if (selectedId == containerId) {
            // Deselect same jar
            _gameUiState.value = state.copy(selectedContainerId = null)
            soundManager.playFruitDrop()
        } else {
            // Second Tap: Attempt move from selectedId -> containerId
            val sourceJar = state.containers.find { it.id == selectedId } ?: return
            val fruitToMove = sourceJar.topFruit ?: return

            if (targetContainer.isFull) {
                // Cannot move into full container: switch selection if destination has valid top fruit
                if (!targetContainer.isEmpty && !targetContainer.isSorted) {
                    _gameUiState.value = state.copy(selectedContainerId = containerId)
                    soundManager.playFruitPickup()
                } else {
                    _gameUiState.value = state.copy(selectedContainerId = null)
                    soundManager.playFruitDrop()
                }
                return
            }

            // Check matching condition: Must be empty OR top fruit matches (and thaws destination fruit if frozen)
            val canPlace = targetContainer.isEmpty || targetContainer.topFruit?.type == fruitToMove.type

            if (canPlace) {
                executeMove(fromId = selectedId, toId = containerId, fruit = fruitToMove)
            } else {
                // Different fruit type: Switch selection to destination jar if valid
                if (!targetContainer.isSorted) {
                    _gameUiState.value = state.copy(selectedContainerId = containerId)
                    soundManager.playFruitPickup()
                } else {
                    _gameUiState.value = state.copy(selectedContainerId = null)
                    soundManager.playFruitDrop()
                }
            }
        }
    }

    private fun executeMove(fromId: Int, toId: Int, fruit: FruitItem) {
        val state = _gameUiState.value
        val destJar = state.containers.find { it.id == toId } ?: return
        moveHistory.add(MoveRecord(fromContainerId = fromId, toContainerId = toId, fruit = fruit))

        val anim = MovingFruitAnimation(
            fruit = fruit,
            fromContainerId = fromId,
            toContainerId = toId,
            targetSlotIndex = destJar.fruits.size,
            totalCapacity = destJar.capacity
        )

        // Trigger smooth visual flying animation
        _gameUiState.value = state.copy(
            selectedContainerId = null,
            movingFruit = anim,
            hintMove = null
        )

        viewModelScope.launch {
            // Flight arc (211ms) + accelerated gravity drop (132ms) = 343ms to impact
            delay(343)
            val isAtBottom = destJar.fruits.isEmpty()
            soundManager.playFruitDrop(fruitSize = fruit.type.size, isAtBottom = isAtBottom)
            // Settle squash & bounce (97ms) -> Total 440ms
            delay(97)

            val currentState = _gameUiState.value
            // Thaw any frozen fruit underneath if matching
            val updatedContainers = currentState.containers.map { jar ->
                when (jar.id) {
                    fromId -> jar.copy(fruits = jar.fruits.dropLast(1))
                    toId -> {
                        val thawedFruits = (jar.fruits + fruit).map { f ->
                            if (f.isFrozen && f.type == fruit.type) f.copy(isFrozen = false, special = SpecialFruit.NORMAL) else f
                        }
                        jar.copy(fruits = thawedFruits)
                    }
                    else -> jar
                }
            }

            val newMoves = currentState.movesCount + 1
            var movesDefeat = false
            currentState.levelData?.moveLimit?.let { limit ->
                if (newMoves >= limit) {
                    movesDefeat = true
                }
            }

            // Check if destination jar just became sorted!
            val updatedDestJar = updatedContainers.find { it.id == toId }
            var justSortedId: Int? = null
            if (updatedDestJar?.isSorted == true) {
                justSortedId = toId
                soundManager.playBottleCompleted()
                repository.recordSortComplete()
                // If special golden fruit sorted, give bonus
                if (fruit.special == SpecialFruit.GOLDEN) {
                    repository.addCoins(50)
                    showToast("✨ Golden Fruit Bonus! +50 Coins 🪙")
                }
            } else {
                soundManager.playFruitBounce()
            }

            // Check Win Condition: All non-empty jars are sorted or homogeneous
            val isBoardWon = checkBoardCompleted(updatedContainers)

            var stars = 3
            if (isBoardWon) {
                val activeJars = updatedContainers.filter { !it.isEmpty }
                val totalFruits = (activeJars.size * 4).coerceAtLeast(4)
                val baseline = (totalFruits * 1.5).toInt()
                stars = when {
                    newMoves <= baseline -> 3
                    newMoves <= baseline * 1.5 -> 2
                    else -> 1
                }
                soundManager.playLevelWin()
                timerJob?.cancel()

                val lvlData = currentState.levelData
                if (lvlData != null && currentState.gameMode == GameMode.CLASSIC) {
                    repository.onLevelComplete(
                        level = lvlData.levelNumber,
                        starsEarned = stars,
                        coinsEarned = lvlData.coinReward,
                        gemsEarned = lvlData.gemReward,
                        usedUndo = currentState.usedUndoThisLevel
                    )
                }
            }

            _gameUiState.value = currentState.copy(
                containers = updatedContainers,
                movingFruit = null,
                movesCount = newMoves,
                isVictory = isBoardWon,
                isDefeat = !isBoardWon && movesDefeat,
                earnedStars = stars,
                justCompletedJarId = justSortedId
            )
        }
    }

    fun useUndo() {
        val state = _gameUiState.value
        if (moveHistory.isEmpty() || state.isVictory || state.isDefeat) return

        if (!repository.usePowerUp(PowerUpType.UNDO)) {
            // Attempt to buy & use with coins
            if (repository.spendCoins(PowerUpType.UNDO.coinCost)) {
                // Bought and used
            } else {
                showToast("Need 30 Coins to Undo!")
                return
            }
        }

        soundManager.playUndo()
        val lastMove = moveHistory.removeAt(moveHistory.size - 1)
        val updatedContainers = state.containers.map { jar ->
            when (jar.id) {
                lastMove.toContainerId -> jar.copy(fruits = jar.fruits.dropLast(1))
                lastMove.fromContainerId -> jar.copy(fruits = jar.fruits + lastMove.fruit)
                else -> jar
            }
        }

        _gameUiState.value = state.copy(
            containers = updatedContainers,
            selectedContainerId = null,
            hintMove = null,
            usedUndoThisLevel = true
        )
    }

    fun useShuffle() {
        val state = _gameUiState.value
        if (state.isVictory || state.isDefeat) return

        if (!repository.usePowerUp(PowerUpType.SHUFFLE)) {
            if (!repository.spendCoins(PowerUpType.SHUFFLE.coinCost)) {
                showToast("Need 50 Coins to Shuffle!")
                return
            }
        }

        soundManager.playShuffle()
        // Collect all fruits from non-sorted containers and reshuffle them
        val nonSortedJars = state.containers.filter { !it.isSorted && !it.isLocked }
        val allFruits = nonSortedJars.flatMap { it.fruits }.shuffled()

        var fruitIdx = 0
        val updated = state.containers.map { jar ->
            if (!jar.isSorted && !jar.isLocked) {
                val takeCount = jar.fruits.size
                val newFruits = allFruits.subList(fruitIdx, (fruitIdx + takeCount).coerceAtMost(allFruits.size))
                fruitIdx += takeCount
                jar.copy(fruits = newFruits)
            } else jar
        }

        _gameUiState.value = state.copy(
            containers = updated,
            selectedContainerId = null,
            hintMove = null
        )
    }

    fun activateHammerMode() {
        val state = _gameUiState.value
        if (state.activePowerUpMode == PowerUpType.HAMMER) {
            _gameUiState.value = state.copy(activePowerUpMode = null)
            return
        }
        val count = userData.value.powerUps[PowerUpType.HAMMER] ?: 0
        if (count == 0 && userData.value.coins < PowerUpType.HAMMER.coinCost) {
            showToast("Need 40 Coins for Hammer!")
            return
        }
        _gameUiState.value = state.copy(activePowerUpMode = PowerUpType.HAMMER, selectedContainerId = null)
        showToast("🔨 Hammer Mode: Tap any container to smash top fruit!")
    }

    fun activateBombMode() {
        val state = _gameUiState.value
        if (state.activePowerUpMode == PowerUpType.BOMB) {
            _gameUiState.value = state.copy(activePowerUpMode = null)
            return
        }
        val count = userData.value.powerUps[PowerUpType.BOMB] ?: 0
        if (count == 0 && userData.value.gems < PowerUpType.BOMB.gemCost) {
            showToast("Need 8 Gems for Bomb!")
            return
        }
        _gameUiState.value = state.copy(activePowerUpMode = PowerUpType.BOMB, selectedContainerId = null)
        showToast("💣 Bomb Mode: Tap any container to blast it clear!")
    }

    private fun handlePowerUpClick(containerId: Int, powerUp: PowerUpType) {
        val state = _gameUiState.value
        val jar = state.containers.find { it.id == containerId } ?: return

        when (powerUp) {
            PowerUpType.HAMMER -> {
                if (jar.isEmpty) {
                    showToast("Container is already empty!")
                    return
                }
                if (!repository.usePowerUp(PowerUpType.HAMMER)) {
                    if (!repository.spendCoins(PowerUpType.HAMMER.coinCost)) return
                }
                val topFruit = jar.topFruit
                val updated = if (topFruit?.isFrozen == true) {
                    soundManager.playIceCrack()
                    showToast("🔨 Ice Smashed & Fruit Thawed!")
                    state.containers.map {
                        if (it.id == containerId) it.copy(fruits = it.fruits.mapIndexed { idx, f ->
                            if (idx == it.fruits.size - 1) f.copy(isFrozen = false, special = SpecialFruit.NORMAL) else f
                        }, isLocked = false) else it
                    }
                } else {
                    soundManager.playHammerSmash()
                    showToast("💥 Smashed fruit!")
                    state.containers.map {
                        if (it.id == containerId) it.copy(fruits = it.fruits.dropLast(1), isLocked = false) else it
                    }
                }
                _gameUiState.value = state.copy(containers = updated, activePowerUpMode = null)
                checkAndApplyVictory(updated)
            }
            PowerUpType.BOMB -> {
                if (!repository.usePowerUp(PowerUpType.BOMB)) {
                    if (!repository.spendGems(PowerUpType.BOMB.gemCost)) return
                }
                soundManager.playBombExplosion()
                val updated = state.containers.map {
                    if (it.id == containerId) it.copy(fruits = emptyList(), isLocked = false) else it
                }
                _gameUiState.value = state.copy(containers = updated, activePowerUpMode = null)
                showToast("💣 Jar cleared!")
                checkAndApplyVictory(updated)
            }
            else -> {}
        }
    }

    fun requestHint() {
        val state = _gameUiState.value
        if (state.isVictory || state.isDefeat) return

        val hint = LevelGenerator.findNextBestMove(state.containers)
        if (hint != null) {
            soundManager.playCoinCollect()
            _gameUiState.value = state.copy(hintMove = hint, selectedContainerId = hint.first)
            showToast("💡 Hint: Move from highlighted jar to target jar!")
        } else {
            showToast("Try using Shuffle or Hammer!")
        }
    }

    fun pauseGame() {
        soundManager.playButtonClick()
        _gameUiState.value = _gameUiState.value.copy(isPaused = true)
    }

    fun resumeGame() {
        soundManager.playButtonClick()
        _gameUiState.value = _gameUiState.value.copy(isPaused = false)
    }

    fun restartCurrentLevel(activity: Activity? = null) {
        soundManager.playButtonClick()
        val currentLvl = _gameUiState.value.levelData?.levelNumber ?: 1
        val mode = _gameUiState.value.gameMode
        if (activity != null) {
            AdManager.onLevelCompleted(activity) {
                startLevel(currentLvl, mode)
            }
        } else {
            startLevel(currentLvl, mode)
        }
    }

    fun nextLevel(activity: Activity? = null) {
        soundManager.playButtonClick()
        val nextLvl = (_gameUiState.value.levelData?.levelNumber ?: 1) + 1
        val mode = _gameUiState.value.gameMode
        if (activity != null) {
            AdManager.onLevelCompleted(activity) {
                startLevel(nextLvl, mode)
            }
        } else {
            startLevel(nextLvl, mode)
        }
    }

    fun buyShopPowerUp(type: PowerUpType) {
        soundManager.playButtonClick()
        if (repository.buyPowerUp(type)) {
            soundManager.playCoinCollect()
            showToast("Purchased ${type.displayName}! ${type.iconEmoji}")
        } else {
            showToast("Not enough currency!")
        }
    }

    fun buyShopStyle(style: ContainerStyle) {
        soundManager.playButtonClick()
        if (repository.unlockStyle(style)) {
            soundManager.playCoinCollect()
            showToast("Unlocked & Equipped ${style.displayName}!")
        } else {
            showToast("Need ${style.costCoins} Coins!")
        }
    }

    fun equipStyle(style: ContainerStyle) {
        soundManager.playButtonClick()
        repository.selectStyle(style)
        showToast("Equipped ${style.displayName}!")
    }

    fun buyShopBackground(theme: ThemeBackground) {
        soundManager.playButtonClick()
        if (repository.unlockBackground(theme)) {
            soundManager.playCoinCollect()
            showToast("Unlocked & Equipped ${theme.displayName}!")
        } else {
            val costMsg = if (theme.costCoins > 0) "${theme.costCoins} Coins" else "${theme.gemCost} Gems"
            showToast("Need $costMsg!")
        }
    }

    fun equipBackground(theme: ThemeBackground) {
        soundManager.playButtonClick()
        repository.selectBackground(theme)
        showToast("Equipped ${theme.displayName}!")
    }

    fun buyShopFruitPack(pack: FruitSkinPack) {
        soundManager.playButtonClick()
        if (repository.unlockFruitPack(pack)) {
            soundManager.playCoinCollect()
            showToast("Unlocked & Equipped ${pack.displayName}!")
        } else {
            val costMsg = if (pack.costCoins > 0) "${pack.costCoins} Coins" else "${pack.gemCost} Gems"
            showToast("Need $costMsg!")
        }
    }

    fun equipFruitPack(pack: FruitSkinPack) {
        soundManager.playButtonClick()
        repository.selectFruitPack(pack)
        showToast("Equipped ${pack.displayName}!")
    }

    fun claimMilestoneChest(milestoneLevel: Int) {
        soundManager.playButtonClick()
        val coins = 100 + milestoneLevel * 2
        val gems = 5 + milestoneLevel / 20
        if (repository.claimMilestoneReward(milestoneLevel, coins, gems)) {
            soundManager.playLevelWin()
            showToast("🎁 Level $milestoneLevel Milestone Claimed! 🪙+$coins 💎+$gems")
        } else {
            showToast("Milestone locked or already claimed!")
        }
    }

    fun spinLuckyWheel(activity: Activity? = null, onResult: (coins: Int, gems: Int, label: String) -> Unit) {
        soundManager.playButtonClick()
        val rewards = listOf(
            Triple(150, 0, "150 Coins 🪙"),
            Triple(300, 0, "300 Coins 🪙"),
            Triple(500, 2, "500 Coins + 2 Gems 💎"),
            Triple(1000, 5, "MEGA JACKPOT! 1000 Coins + 5 Gems 👑"),
            Triple(50, 1, "50 Coins + 1 Gem 💎"),
            Triple(200, 0, "200 Coins 🪙")
        )
        val selected = rewards.random()
        soundManager.playCoinCollect()
        repository.recordLuckySpin(LocalDate.now().toEpochDay(), selected.first, selected.second)
        onResult(selected.first, selected.second, selected.third)
        showToast("🎉 Lucky Spin Won: ${selected.third}")
    }

    fun claimDailyStreakReward(day: Int, coins: Int, gems: Int) {
        soundManager.playButtonClick()
        val today = LocalDate.now().toEpochDay()
        if (repository.claimDailyReward(today, day, coins, gems)) {
            soundManager.playLevelWin()
            showToast("Claimed Day $day Reward! 🪙+$coins 💎+$gems")
        } else {
            showToast("Already claimed today! Come back tomorrow.")
        }
    }

    fun claimAchievementReward(item: AchievementItem) {
        soundManager.playButtonClick()
        if (repository.claimAchievement(item.id, item.coinReward, item.gemReward)) {
            soundManager.playCoinCollect()
            showToast("🏆 Achievement Claimed: ${item.title}! 🪙+${item.coinReward} 💎+${item.gemReward}")
        }
    }

    fun watchRewardAd(activity: Activity? = null) {
        if (activity != null) {
            AdManager.showRewardedAd(
                activity = activity,
                onRewardEarned = { amount, type ->
                    soundManager.playCoinCollect()
                    repository.addCoins(100)
                    repository.addGems(3)
                    showToast("📺 Ad Reward Claimed! +100 Coins 🪙 +3 Gems 💎")
                },
                onAdUnavailable = {
                    showToast("⏳ Rewarded ad is loading. Please try again shortly!")
                }
            )
        } else {
            soundManager.playCoinCollect()
            repository.addCoins(100)
            repository.addGems(3)
            showToast("📺 Ad Reward Claimed! +100 Coins 🪙 +3 Gems 💎")
        }
    }

    fun showToast(msg: String) {
        _gameUiState.value = _gameUiState.value.copy(toastMessage = msg)
        viewModelScope.launch {
            delay(2600)
            if (_gameUiState.value.toastMessage == msg) {
                _gameUiState.value = _gameUiState.value.copy(toastMessage = null)
            }
        }
    }

    fun resetAllProgress() {
        repository.resetAllProgress()
        showToast("Progress Reset")
    }
}
