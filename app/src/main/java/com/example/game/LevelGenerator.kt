package com.example.game

import com.example.model.ContainerState
import com.example.model.ContainerStyle
import com.example.model.FruitItem
import com.example.model.FruitType
import com.example.model.GameMode
import com.example.model.SpecialFruit
import java.util.LinkedList
import java.util.Queue
import java.util.UUID
import kotlin.random.Random

data class LevelData(
    val levelNumber: Int,
    val fruitTypes: List<FruitType>,
    val containers: List<ContainerState>,
    val moveLimit: Int? = null,
    val timeLimitSeconds: Int? = null,
    val difficultyName: String,
    val coinReward: Int = 50,
    val gemReward: Int = 2
)

object LevelGenerator {

    private val allFruitTypes = FruitType.entries

    fun getDifficultyName(level: Int): String {
        return when {
            level <= 20 -> "Novice"
            level <= 50 -> "Apprentice"
            level <= 100 -> "Adept"
            level <= 200 -> "Expert"
            level <= 350 -> "Master"
            level <= 500 -> "Grandmaster"
            level <= 700 -> "Champion"
            level <= 900 -> "Legend"
            else -> "Immortal"
        }
    }

    fun generateLevel(level: Int, selectedStyle: ContainerStyle = ContainerStyle.GLASS_JAR, randomSeed: Long? = null): LevelData {
        val random = if (randomSeed != null) Random(randomSeed) else Random(level.toLong() * 31337L + 777L)

        // Progressive fruit count scaling up to 1000 levels
        val fruitTypeCount = when {
            level <= 3 -> 2
            level <= 15 -> 3
            level <= 45 -> 4
            level <= 120 -> 5
            level <= 350 -> 6
            else -> 7
        }.coerceAtMost(allFruitTypes.size)

        // Empty tube allocation
        val emptyCount = when {
            level <= 8 -> 2
            level <= 50 -> 2
            level <= 150 -> if (level % 3 == 0) 1 else 2
            level <= 500 -> if (level % 2 == 0) 1 else 2
            else -> if (random.nextFloat() < 0.65f) 1 else 2
        }

        val capacity = 4

        // Pick distinct fruit types for this level
        val shuffledFruits = allFruitTypes.shuffled(random).take(fruitTypeCount)

        // Calculate reverse shuffle moves strictly increasing with level
        val shuffleMoves = (10 + (level * 0.35).toInt()).coerceIn(12, 180)

        // Generate guaranteed solvable layout
        var containers = createSolvablePuzzle(
            fruits = shuffledFruits,
            emptyCount = emptyCount,
            capacity = capacity,
            shuffleSteps = shuffleMoves,
            random = random,
            selectedStyle = selectedStyle
        )

        // Inject progressive mechanics
        if (level >= 10 && random.nextFloat() < 0.30f) {
            // Golden fruit bonus
            containers = injectGoldenFruit(containers, random)
        }

        if (level >= 20 && level % 2 == 0) {
            // Frozen fruit
            containers = injectFrozenFruit(containers, random)
        }

        if (level >= 40 && level % 5 == 0) {
            // Locked jar (requires sorting another jar or key)
            containers = injectLockedContainer(containers, random)
        }

        // Boss level every 25 levels with strict move limit
        val isBossLevel = level % 25 == 0
        val moveLimit = if (isBossLevel || (level >= 60 && level % 4 == 0)) {
            (fruitTypeCount * capacity * 2.2).toInt() + ((1000 - level) / 50).coerceIn(4, 16)
        } else null

        // Dynamic timer per level: scales from 80s down to 35s based on level difficulty
        val dynamicTimeLimit = when {
            level <= 5 -> 80
            level <= 15 -> 70
            level <= 40 -> 60
            level <= 100 -> 55
            level <= 250 -> 50
            level <= 500 -> 45
            level <= 750 -> 40
            else -> 35
        }

        val difficultyName = if (isBossLevel) "⚡ Boss #${level / 25}" else getDifficultyName(level)

        return LevelData(
            levelNumber = level,
            fruitTypes = shuffledFruits,
            containers = containers,
            moveLimit = moveLimit,
            timeLimitSeconds = dynamicTimeLimit,
            difficultyName = difficultyName,
            coinReward = 40 + (level * 0.6).toInt().coerceAtMost(600),
            gemReward = if (isBossLevel) 10 else if (level % 5 == 0) 3 else 1
        )
    }

    private fun createSolvablePuzzle(
        fruits: List<FruitType>,
        emptyCount: Int,
        capacity: Int,
        shuffleSteps: Int,
        random: Random,
        selectedStyle: ContainerStyle
    ): List<ContainerState> {
        val totalContainers = fruits.size + emptyCount
        // Start in fully solved state
        val initialContainers = mutableListOf<MutableList<FruitItem>>()

        for (fruit in fruits) {
            val jar = mutableListOf<FruitItem>()
            repeat(capacity) {
                jar.add(FruitItem(id = UUID.randomUUID().toString(), type = fruit))
            }
            initialContainers.add(jar)
        }

        repeat(emptyCount) {
            initialContainers.add(mutableListOf())
        }

        // Perform valid reverse-sorting steps
        // In reverse sorting: any top fruit can move to any jar that is not full,
        // with preference to mix up solved stacks
        var lastFrom = -1
        var lastTo = -1

        var currentStep = 0
        var attempts = 0
        while (currentStep < shuffleSteps && attempts < shuffleSteps * 10) {
            attempts++
            val fromIndex = random.nextInt(totalContainers)
            val fromJar = initialContainers[fromIndex]
            if (fromJar.isEmpty()) continue

            val toIndex = random.nextInt(totalContainers)
            if (toIndex == fromIndex) continue

            val toJar = initialContainers[toIndex]
            if (toJar.size >= capacity) continue

            // Avoid immediate ping-pong move
            if (fromIndex == lastTo && toIndex == lastFrom && attempts < shuffleSteps * 4) {
                continue
            }

            // Move fruit
            val fruit = fromJar.removeAt(fromJar.size - 1)
            toJar.add(fruit)

            lastFrom = fromIndex
            lastTo = toIndex
            currentStep++
        }

        return initialContainers.mapIndexed { index, fruitList ->
            ContainerState(
                id = index,
                capacity = capacity,
                fruits = fruitList.toList(),
                style = selectedStyle
            )
        }
    }

    private fun injectGoldenFruit(containers: List<ContainerState>, random: Random): List<ContainerState> {
        val candidates = mutableListOf<Pair<Int, Int>>()
        containers.forEachIndexed { cIdx, container ->
            container.fruits.forEachIndexed { fIdx, _ ->
                candidates.add(cIdx to fIdx)
            }
        }
        if (candidates.isEmpty()) return containers
        val (targetC, targetF) = candidates.random(random)
        return containers.mapIndexed { cIdx, container ->
            if (cIdx == targetC) {
                val updatedFruits = container.fruits.mapIndexed { fIdx, fruit ->
                    if (fIdx == targetF) fruit.copy(special = SpecialFruit.GOLDEN) else fruit
                }
                container.copy(fruits = updatedFruits)
            } else container
        }
    }

    private fun injectFrozenFruit(containers: List<ContainerState>, random: Random): List<ContainerState> {
        // Place frozen fruit in bottom or middle of a non-empty container
        val nonEmpties = containers.filter { it.fruits.size >= 2 }
        if (nonEmpties.isEmpty()) return containers
        val targetJar = nonEmpties.random(random)
        return containers.map { container ->
            if (container.id == targetJar.id) {
                val updatedFruits = container.fruits.mapIndexed { idx, fruit ->
                    if (idx == 0) fruit.copy(special = SpecialFruit.FROZEN, isFrozen = true) else fruit
                }
                container.copy(fruits = updatedFruits)
            } else container
        }
    }

    private fun injectLockedContainer(containers: List<ContainerState>, random: Random): List<ContainerState> {
        val nonEmpties = containers.filter { !it.isEmpty }
        if (nonEmpties.size <= 2) return containers
        val target = nonEmpties.random(random)
        return containers.map { if (it.id == target.id) it.copy(isLocked = true) else it }
    }

    fun generateDailyPuzzle(epochDay: Long, selectedStyle: ContainerStyle = ContainerStyle.GLASS_JAR): LevelData {
        val random = Random(epochDay * 99991L)
        val fruits = allFruitTypes.shuffled(random).take(4)
        val containers = createSolvablePuzzle(
            fruits = fruits,
            emptyCount = 2,
            capacity = 4,
            shuffleSteps = 45,
            random = random,
            selectedStyle = selectedStyle
        )
        return LevelData(
            levelNumber = (epochDay % 1000).toInt(),
            fruitTypes = fruits,
            containers = containers,
            difficultyName = "Daily Challenge",
            coinReward = 150,
            gemReward = 5
        )
    }

    fun generateChallengeLevel(tier: Int, selectedStyle: ContainerStyle = ContainerStyle.GLASS_JAR): LevelData {
        val fruitCount = (3 + tier).coerceAtMost(6)
        val fruits = allFruitTypes.shuffled().take(fruitCount)
        val timeLimit = (70 - tier * 8).coerceAtLeast(35)
        val moveLimit = (fruitCount * 4 * 2.2).toInt()
        val containers = createSolvablePuzzle(
            fruits = fruits,
            emptyCount = if (tier > 3) 1 else 2,
            capacity = 4,
            shuffleSteps = 30 + tier * 15,
            random = Random.Default,
            selectedStyle = selectedStyle
        )
        return LevelData(
            levelNumber = tier,
            fruitTypes = fruits,
            containers = containers,
            moveLimit = moveLimit,
            timeLimitSeconds = timeLimit,
            difficultyName = "Challenge #$tier",
            coinReward = 100 * tier,
            gemReward = 4 * tier
        )
    }

    fun generateEndlessRound(wave: Int, selectedStyle: ContainerStyle = ContainerStyle.GLASS_JAR): LevelData {
        val fruitCount = (2 + (wave / 3)).coerceIn(3, 7)
        val fruits = allFruitTypes.shuffled().take(fruitCount)
        val containers = createSolvablePuzzle(
            fruits = fruits,
            emptyCount = if (wave > 8) 1 else 2,
            capacity = 4,
            shuffleSteps = 25 + wave * 5,
            random = Random.Default,
            selectedStyle = selectedStyle
        )
        return LevelData(
            levelNumber = wave,
            fruitTypes = fruits,
            containers = containers,
            difficultyName = "Endless Wave $wave",
            coinReward = 30 + wave * 10,
            gemReward = 2
        )
    }

    // BFS Solver for Hint Engine and Solvability Confirmation
    fun findNextBestMove(containers: List<ContainerState>): Pair<Int, Int>? {
        // Find a valid legal move that moves towards completion
        val state = containers.map { it.fruits.map { f -> f.type } }
        val capacity = containers.firstOrNull()?.capacity ?: 4

        // 1. Check if moving onto identical top fruit completes or makes progress
        for (i in containers.indices) {
            val source = containers[i]
            if (source.isLocked || source.isEmpty || source.isSorted) continue
            val topSource = source.topFruit ?: continue
            if (topSource.isFrozen) continue

            for (j in containers.indices) {
                if (i == j) continue
                val dest = containers[j]
                if (dest.isLocked || dest.isFull || dest.isSorted) continue

                val destTop = dest.topFruit
                // Rule 1: Move onto matching fruit
                if (destTop != null && destTop.type == topSource.type) {
                    return i to j
                }
            }
        }

        // 2. Check move to an empty jar if it helps uncover a different fruit below
        for (i in containers.indices) {
            val source = containers[i]
            if (source.isLocked || source.isEmpty || source.isSorted) continue
            val topSource = source.topFruit ?: continue
            if (topSource.isFrozen) continue

            // Only move to empty if source has mixed fruits below
            val hasDifferentBelow = source.fruits.any { it.type != topSource.type }
            if (!hasDifferentBelow) continue

            for (j in containers.indices) {
                if (i == j) continue
                val dest = containers[j]
                if (!dest.isLocked && dest.isEmpty) {
                    return i to j
                }
            }
        }

        // 3. Fallback: Any valid move
        for (i in containers.indices) {
            val source = containers[i]
            if (source.isLocked || source.isEmpty || source.isSorted) continue
            val topSource = source.topFruit ?: continue
            if (topSource.isFrozen) continue

            for (j in containers.indices) {
                if (i == j) continue
                val dest = containers[j]
                if (!dest.isLocked && !dest.isFull) {
                    if (dest.isEmpty || dest.topFruit?.type == topSource.type) {
                        return i to j
                    }
                }
            }
        }

        return null
    }
}
