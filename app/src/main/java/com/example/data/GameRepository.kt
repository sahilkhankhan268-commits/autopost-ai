package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ContainerStyle
import com.example.model.FruitSkinPack
import com.example.model.PowerUpType
import com.example.model.ThemeBackground
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val coinReward: Int,
    val gemReward: Int,
    val target: Int,
    val current: Int,
    val isClaimed: Boolean
) {
    val isUnlocked: Boolean get() = current >= target
}

data class UserGameData(
    val coins: Int = 200,
    val gems: Int = 15,
    val currentLevel: Int = 1,
    val highestLevelUnlocked: Int = 1,
    val levelStars: Map<Int, Int> = emptyMap(),
    val powerUps: Map<PowerUpType, Int> = mapOf(
        PowerUpType.UNDO to 3,
        PowerUpType.HAMMER to 2,
        PowerUpType.SHUFFLE to 2,
        PowerUpType.BOMB to 1,
        PowerUpType.HINT to 3
    ),
    val unlockedStyles: Set<ContainerStyle> = setOf(ContainerStyle.GLASS_JAR),
    val selectedStyle: ContainerStyle = ContainerStyle.GLASS_JAR,
    val unlockedBackgrounds: Set<ThemeBackground> = setOf(ThemeBackground.NEON_NIGHT),
    val selectedBackground: ThemeBackground = ThemeBackground.NEON_NIGHT,
    val unlockedFruitPacks: Set<FruitSkinPack> = setOf(FruitSkinPack.CLASSIC_FRUITS),
    val selectedFruitPack: FruitSkinPack = FruitSkinPack.CLASSIC_FRUITS,
    val dailyStreak: Int = 1,
    val lastDailyClaimEpochDay: Long = 0L,
    val lastLuckySpinEpochDay: Long = 0L,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val totalSortsCount: Int = 0,
    val totalLevelsWon: Int = 0,
    val levelsWonWithoutUndo: Int = 0,
    val claimedAchievements: Set<String> = emptySet(),
    val claimedMilestones: Set<Int> = emptySet()
)

class GameRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fruit_sort_3d_prefs", Context.MODE_PRIVATE)

    private val _userData = MutableStateFlow(loadUserData())
    val userData: StateFlow<UserGameData> = _userData.asStateFlow()

    private fun loadUserData(): UserGameData {
        val coins = prefs.getInt("coins", 250)
        val gems = prefs.getInt("gems", 20)
        val currentLevel = prefs.getInt("current_level", 1)
        val highestUnlocked = prefs.getInt("highest_level_unlocked", 1)

        // Load stars
        val starEntries = prefs.getStringSet("level_stars_set", emptySet()) ?: emptySet()
        val starsMap = mutableMapOf<Int, Int>()
        for (entry in starEntries) {
            val parts = entry.split(":")
            if (parts.size == 2) {
                val lvl = parts[0].toIntOrNull()
                val stars = parts[1].toIntOrNull()
                if (lvl != null && stars != null) {
                    starsMap[lvl] = stars
                }
            }
        }

        // Power-ups
        val powerUps = mutableMapOf<PowerUpType, Int>()
        powerUps[PowerUpType.UNDO] = prefs.getInt("power_undo", 3)
        powerUps[PowerUpType.HAMMER] = prefs.getInt("power_hammer", 2)
        powerUps[PowerUpType.SHUFFLE] = prefs.getInt("power_shuffle", 2)
        powerUps[PowerUpType.BOMB] = prefs.getInt("power_bomb", 1)
        powerUps[PowerUpType.HINT] = prefs.getInt("power_hint", 3)

        // Container styles
        val styleNames = prefs.getStringSet("unlocked_styles", setOf(ContainerStyle.GLASS_JAR.name)) ?: setOf(ContainerStyle.GLASS_JAR.name)
        val unlockedStyles = styleNames.mapNotNull {
            try { ContainerStyle.valueOf(it) } catch (_: Exception) { null }
        }.toSet().ifEmpty { setOf(ContainerStyle.GLASS_JAR) }

        val selectedStyleName = prefs.getString("selected_style", ContainerStyle.GLASS_JAR.name) ?: ContainerStyle.GLASS_JAR.name
        val selectedStyle = try { ContainerStyle.valueOf(selectedStyleName) } catch (_: Exception) { ContainerStyle.GLASS_JAR }

        // Backgrounds
        val bgNames = prefs.getStringSet("unlocked_backgrounds", setOf(ThemeBackground.NEON_NIGHT.name)) ?: setOf(ThemeBackground.NEON_NIGHT.name)
        val unlockedBackgrounds = bgNames.mapNotNull {
            try { ThemeBackground.valueOf(it) } catch (_: Exception) { null }
        }.toSet().ifEmpty { setOf(ThemeBackground.NEON_NIGHT) }

        val selectedBgName = prefs.getString("selected_background", ThemeBackground.NEON_NIGHT.name) ?: ThemeBackground.NEON_NIGHT.name
        val selectedBackground = try { ThemeBackground.valueOf(selectedBgName) } catch (_: Exception) { ThemeBackground.NEON_NIGHT }

        // Fruit packs
        val fruitPackNames = prefs.getStringSet("unlocked_fruit_packs", setOf(FruitSkinPack.CLASSIC_FRUITS.name)) ?: setOf(FruitSkinPack.CLASSIC_FRUITS.name)
        val unlockedFruitPacks = fruitPackNames.mapNotNull {
            try { FruitSkinPack.valueOf(it) } catch (_: Exception) { null }
        }.toSet().ifEmpty { setOf(FruitSkinPack.CLASSIC_FRUITS) }

        val selectedFruitPackName = prefs.getString("selected_fruit_pack", FruitSkinPack.CLASSIC_FRUITS.name) ?: FruitSkinPack.CLASSIC_FRUITS.name
        val selectedFruitPack = try { FruitSkinPack.valueOf(selectedFruitPackName) } catch (_: Exception) { FruitSkinPack.CLASSIC_FRUITS }

        val dailyStreak = prefs.getInt("daily_streak", 1)
        val lastDailyClaimEpochDay = prefs.getLong("last_daily_claim", 0L)
        val lastLuckySpinEpochDay = prefs.getLong("last_lucky_spin", 0L)
        val soundEnabled = prefs.getBoolean("sound_enabled", true)
        val musicEnabled = prefs.getBoolean("music_enabled", true)
        val hapticEnabled = prefs.getBoolean("haptic_enabled", true)

        val totalSorts = prefs.getInt("total_sorts", 0)
        val totalLevelsWon = prefs.getInt("total_levels_won", 0)
        val withoutUndo = prefs.getInt("levels_won_without_undo", 0)
        val claimedAchievements = prefs.getStringSet("claimed_achievements", emptySet()) ?: emptySet()

        val milestoneEntries = prefs.getStringSet("claimed_milestones_set", emptySet()) ?: emptySet()
        val claimedMilestones = milestoneEntries.mapNotNull { it.toIntOrNull() }.toSet()

        return UserGameData(
            coins = coins,
            gems = gems,
            currentLevel = currentLevel,
            highestLevelUnlocked = highestUnlocked,
            levelStars = starsMap,
            powerUps = powerUps,
            unlockedStyles = unlockedStyles,
            selectedStyle = selectedStyle,
            unlockedBackgrounds = unlockedBackgrounds,
            selectedBackground = selectedBackground,
            unlockedFruitPacks = unlockedFruitPacks,
            selectedFruitPack = selectedFruitPack,
            dailyStreak = dailyStreak,
            lastDailyClaimEpochDay = lastDailyClaimEpochDay,
            lastLuckySpinEpochDay = lastLuckySpinEpochDay,
            soundEnabled = soundEnabled,
            musicEnabled = musicEnabled,
            hapticEnabled = hapticEnabled,
            totalSortsCount = totalSorts,
            totalLevelsWon = totalLevelsWon,
            levelsWonWithoutUndo = withoutUndo,
            claimedAchievements = claimedAchievements,
            claimedMilestones = claimedMilestones
        )
    }

    private fun persist(data: UserGameData) {
        _userData.value = data
        val editor = prefs.edit()
        editor.putInt("coins", data.coins)
        editor.putInt("gems", data.gems)
        editor.putInt("current_level", data.currentLevel)
        editor.putInt("highest_level_unlocked", data.highestLevelUnlocked)

        val starSet = data.levelStars.map { "${it.key}:${it.value}" }.toSet()
        editor.putStringSet("level_stars_set", starSet)

        data.powerUps.forEach { (type, count) ->
            editor.putInt("power_${type.name.lowercase()}", count)
        }

        editor.putStringSet("unlocked_styles", data.unlockedStyles.map { it.name }.toSet())
        editor.putString("selected_style", data.selectedStyle.name)

        editor.putStringSet("unlocked_backgrounds", data.unlockedBackgrounds.map { it.name }.toSet())
        editor.putString("selected_background", data.selectedBackground.name)

        editor.putStringSet("unlocked_fruit_packs", data.unlockedFruitPacks.map { it.name }.toSet())
        editor.putString("selected_fruit_pack", data.selectedFruitPack.name)

        editor.putInt("daily_streak", data.dailyStreak)
        editor.putLong("last_daily_claim", data.lastDailyClaimEpochDay)
        editor.putLong("last_lucky_spin", data.lastLuckySpinEpochDay)
        editor.putBoolean("sound_enabled", data.soundEnabled)
        editor.putBoolean("music_enabled", data.musicEnabled)
        editor.putBoolean("haptic_enabled", data.hapticEnabled)

        editor.putInt("total_sorts", data.totalSortsCount)
        editor.putInt("total_levels_won", data.totalLevelsWon)
        editor.putInt("levels_won_without_undo", data.levelsWonWithoutUndo)
        editor.putStringSet("claimed_achievements", data.claimedAchievements)
        editor.putStringSet("claimed_milestones_set", data.claimedMilestones.map { it.toString() }.toSet())

        editor.apply()
    }

    fun addCoins(amount: Int) {
        val current = _userData.value
        persist(current.copy(coins = current.coins + amount))
    }

    fun spendCoins(amount: Int): Boolean {
        val current = _userData.value
        if (current.coins >= amount) {
            persist(current.copy(coins = current.coins - amount))
            return true
        }
        return false
    }

    fun addGems(amount: Int) {
        val current = _userData.value
        persist(current.copy(gems = current.gems + amount))
    }

    fun spendGems(amount: Int): Boolean {
        val current = _userData.value
        if (current.gems >= amount) {
            persist(current.copy(gems = current.gems - amount))
            return true
        }
        return false
    }

    fun onLevelComplete(level: Int, starsEarned: Int, coinsEarned: Int, gemsEarned: Int, usedUndo: Boolean) {
        val current = _userData.value
        val newStars = current.levelStars.toMutableMap()
        val oldStars = newStars[level] ?: 0
        if (starsEarned > oldStars) {
            newStars[level] = starsEarned
        }

        val nextLevel = level + 1
        val newHighest = maxOf(current.highestLevelUnlocked, nextLevel)
        val newWithoutUndo = if (!usedUndo) current.levelsWonWithoutUndo + 1 else current.levelsWonWithoutUndo

        persist(
            current.copy(
                coins = current.coins + coinsEarned,
                gems = current.gems + gemsEarned,
                currentLevel = nextLevel,
                highestLevelUnlocked = newHighest,
                levelStars = newStars,
                totalLevelsWon = current.totalLevelsWon + 1,
                levelsWonWithoutUndo = newWithoutUndo
            )
        )
    }

    fun recordSortComplete() {
        val current = _userData.value
        persist(current.copy(totalSortsCount = current.totalSortsCount + 1))
    }

    fun setCurrentLevel(level: Int) {
        val current = _userData.value
        persist(current.copy(currentLevel = level))
    }

    fun usePowerUp(type: PowerUpType): Boolean {
        val current = _userData.value
        val count = current.powerUps[type] ?: 0
        if (count > 0) {
            val updated = current.powerUps.toMutableMap()
            updated[type] = count - 1
            persist(current.copy(powerUps = updated))
            return true
        }
        return false
    }

    fun buyPowerUp(type: PowerUpType, amount: Int = 1): Boolean {
        val current = _userData.value
        val totalCoins = type.coinCost * amount
        val totalGems = type.gemCost * amount

        if (totalCoins > 0 && current.coins < totalCoins) return false
        if (totalGems > 0 && current.gems < totalGems) return false

        val updatedPowerUps = current.powerUps.toMutableMap()
        updatedPowerUps[type] = (updatedPowerUps[type] ?: 0) + amount

        persist(
            current.copy(
                coins = current.coins - totalCoins,
                gems = current.gems - totalGems,
                powerUps = updatedPowerUps
            )
        )
        return true
    }

    fun unlockStyle(style: ContainerStyle): Boolean {
        val current = _userData.value
        if (current.unlockedStyles.contains(style)) return true
        if (current.coins < style.costCoins) return false

        persist(
            current.copy(
                coins = current.coins - style.costCoins,
                unlockedStyles = current.unlockedStyles + style,
                selectedStyle = style
            )
        )
        return true
    }

    fun selectStyle(style: ContainerStyle) {
        val current = _userData.value
        if (current.unlockedStyles.contains(style)) {
            persist(current.copy(selectedStyle = style))
        }
    }

    fun unlockBackground(theme: ThemeBackground): Boolean {
        val current = _userData.value
        if (current.unlockedBackgrounds.contains(theme)) return true
        if (theme.costCoins > 0 && current.coins < theme.costCoins) return false
        if (theme.gemCost > 0 && current.gems < theme.gemCost) return false

        persist(
            current.copy(
                coins = current.coins - theme.costCoins,
                gems = current.gems - theme.gemCost,
                unlockedBackgrounds = current.unlockedBackgrounds + theme,
                selectedBackground = theme
            )
        )
        return true
    }

    fun selectBackground(theme: ThemeBackground) {
        val current = _userData.value
        if (current.unlockedBackgrounds.contains(theme)) {
            persist(current.copy(selectedBackground = theme))
        }
    }

    fun unlockFruitPack(pack: FruitSkinPack): Boolean {
        val current = _userData.value
        if (current.unlockedFruitPacks.contains(pack)) return true
        if (pack.costCoins > 0 && current.coins < pack.costCoins) return false
        if (pack.gemCost > 0 && current.gems < pack.gemCost) return false

        persist(
            current.copy(
                coins = current.coins - pack.costCoins,
                gems = current.gems - pack.gemCost,
                unlockedFruitPacks = current.unlockedFruitPacks + pack,
                selectedFruitPack = pack
            )
        )
        return true
    }

    fun selectFruitPack(pack: FruitSkinPack) {
        val current = _userData.value
        if (current.unlockedFruitPacks.contains(pack)) {
            persist(current.copy(selectedFruitPack = pack))
        }
    }

    fun claimMilestoneReward(milestoneLevel: Int, coinReward: Int, gemReward: Int): Boolean {
        val current = _userData.value
        if (current.highestLevelUnlocked < milestoneLevel) return false
        if (current.claimedMilestones.contains(milestoneLevel)) return false

        persist(
            current.copy(
                coins = current.coins + coinReward,
                gems = current.gems + gemReward,
                claimedMilestones = current.claimedMilestones + milestoneLevel
            )
        )
        return true
    }

    fun recordLuckySpin(todayEpochDay: Long, coinReward: Int, gemReward: Int) {
        val current = _userData.value
        persist(
            current.copy(
                coins = current.coins + coinReward,
                gems = current.gems + gemReward,
                lastLuckySpinEpochDay = todayEpochDay
            )
        )
    }

    fun updateSettings(sound: Boolean, music: Boolean, haptic: Boolean) {
        val current = _userData.value
        persist(current.copy(soundEnabled = sound, musicEnabled = music, hapticEnabled = haptic))
    }

    fun claimDailyReward(todayEpochDay: Long, dayInStreak: Int, coins: Int, gems: Int): Boolean {
        val current = _userData.value
        if (current.lastDailyClaimEpochDay >= todayEpochDay) return false

        val isConsecutive = (todayEpochDay - current.lastDailyClaimEpochDay) == 1L
        val newStreak = if (isConsecutive) (current.dailyStreak % 7) + 1 else 1

        persist(
            current.copy(
                coins = current.coins + coins,
                gems = current.gems + gems,
                dailyStreak = newStreak,
                lastDailyClaimEpochDay = todayEpochDay
            )
        )
        return true
    }

    fun claimAchievement(achievementId: String, coinReward: Int, gemReward: Int): Boolean {
        val current = _userData.value
        if (current.claimedAchievements.contains(achievementId)) return false

        persist(
            current.copy(
                coins = current.coins + coinReward,
                gems = current.gems + gemReward,
                claimedAchievements = current.claimedAchievements + achievementId
            )
        )
        return true
    }

    fun getAchievementsList(data: UserGameData): List<AchievementItem> {
        return listOf(
            AchievementItem(
                id = "first_sort",
                title = "First Sort",
                description = "Complete your first sorted fruit container",
                iconEmoji = "🍎",
                coinReward = 50,
                gemReward = 2,
                target = 1,
                current = data.totalSortsCount,
                isClaimed = data.claimedAchievements.contains("first_sort")
            ),
            AchievementItem(
                id = "levels_10",
                title = "10 Levels Complete",
                description = "Master your sorting skills on 10 levels",
                iconEmoji = "🎯",
                coinReward = 150,
                gemReward = 5,
                target = 10,
                current = data.totalLevelsWon,
                isClaimed = data.claimedAchievements.contains("levels_10")
            ),
            AchievementItem(
                id = "levels_50",
                title = "50 Levels Complete",
                description = "Conquer 50 challenging fruit puzzles",
                iconEmoji = "🏆",
                coinReward = 350,
                gemReward = 15,
                target = 50,
                current = data.totalLevelsWon,
                isClaimed = data.claimedAchievements.contains("levels_50")
            ),
            AchievementItem(
                id = "levels_100",
                title = "100 Levels Complete",
                description = "Achieve Century Master status",
                iconEmoji = "👑",
                coinReward = 800,
                gemReward = 30,
                target = 100,
                current = data.totalLevelsWon,
                isClaimed = data.claimedAchievements.contains("levels_100")
            ),
            AchievementItem(
                id = "levels_250",
                title = "250 Levels Legend",
                description = "Overcome 250 tricky sorting stages",
                iconEmoji = "⚡",
                coinReward = 1500,
                gemReward = 50,
                target = 250,
                current = data.totalLevelsWon,
                isClaimed = data.claimedAchievements.contains("levels_250")
            ),
            AchievementItem(
                id = "levels_500",
                title = "500 Levels Grandmaster",
                description = "Reach the halfway summit of 500 levels",
                iconEmoji = "🌟",
                coinReward = 3000,
                gemReward = 100,
                target = 500,
                current = data.totalLevelsWon,
                isClaimed = data.claimedAchievements.contains("levels_500")
            ),
            AchievementItem(
                id = "levels_1000",
                title = "1000 Levels Immortal",
                description = "Conquer the entire 1000 Level Empire",
                iconEmoji = "🌌",
                coinReward = 10000,
                gemReward = 300,
                target = 1000,
                current = data.totalLevelsWon,
                isClaimed = data.claimedAchievements.contains("levels_1000")
            ),
            AchievementItem(
                id = "streak_7",
                title = "7 Day Streak",
                description = "Play and claim daily rewards 7 days in a row",
                iconEmoji = "🔥",
                coinReward = 250,
                gemReward = 10,
                target = 7,
                current = data.dailyStreak,
                isClaimed = data.claimedAchievements.contains("streak_7")
            ),
            AchievementItem(
                id = "gems_50",
                title = "Collect 50 Gems",
                description = "Amass a sparkling collection of 50 Gems",
                iconEmoji = "💎",
                coinReward = 200,
                gemReward = 10,
                target = 50,
                current = data.gems,
                isClaimed = data.claimedAchievements.contains("gems_50")
            ),
            AchievementItem(
                id = "solve_no_undo",
                title = "Solve Without Undo",
                description = "Win 15 levels without using the Undo power-up",
                iconEmoji = "🧠",
                coinReward = 300,
                gemReward = 8,
                target = 15,
                current = data.levelsWonWithoutUndo,
                isClaimed = data.claimedAchievements.contains("solve_no_undo")
            ),
            AchievementItem(
                id = "perfect_stars",
                title = "Star Collector",
                description = "Earn 3 stars on at least 20 levels",
                iconEmoji = "✨",
                coinReward = 400,
                gemReward = 12,
                target = 20,
                current = data.levelStars.values.count { it == 3 },
                isClaimed = data.claimedAchievements.contains("perfect_stars")
            )
        )
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
        _userData.value = loadUserData()
    }
}
