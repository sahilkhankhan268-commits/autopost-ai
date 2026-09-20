package com.example.model

import androidx.compose.ui.graphics.Color

enum class FruitSize {
    SMALL,
    MEDIUM,
    LARGE
}

enum class FruitType(
    val displayName: String,
    val size: FruitSize,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
) {
    APPLE_RED(
        displayName = "Red Apple",
        size = FruitSize.MEDIUM,
        primaryColor = Color(0xFFE53935),
        secondaryColor = Color(0xFFB71C1C),
        accentColor = Color(0xFFFF8A80)
    ),
    APPLE_GREEN(
        displayName = "Green Apple",
        size = FruitSize.MEDIUM,
        primaryColor = Color(0xFF7CB342),
        secondaryColor = Color(0xFF558B2F),
        accentColor = Color(0xFFAED581)
    ),
    ORANGE(
        displayName = "Orange",
        size = FruitSize.MEDIUM,
        primaryColor = Color(0xFFFF9800),
        secondaryColor = Color(0xFFE65100),
        accentColor = Color(0xFFFFE082)
    ),
    LEMON(
        displayName = "Lemon",
        size = FruitSize.MEDIUM,
        primaryColor = Color(0xFFFFEB3B),
        secondaryColor = Color(0xFFFBC02D),
        accentColor = Color(0xFFFFF9C4)
    ),
    BANANA(
        displayName = "Banana",
        size = FruitSize.LARGE,
        primaryColor = Color(0xFFFFD54F),
        secondaryColor = Color(0xFFFFA000),
        accentColor = Color(0xFFFFF59D)
    ),
    KIWI(
        displayName = "Kiwi",
        size = FruitSize.SMALL,
        primaryColor = Color(0xFF8D6E63),
        secondaryColor = Color(0xFF5D4037),
        accentColor = Color(0xFF8BC34A)
    ),
    PEAR(
        displayName = "Pear",
        size = FruitSize.MEDIUM,
        primaryColor = Color(0xFFC0CA33),
        secondaryColor = Color(0xFF9E9D24),
        accentColor = Color(0xFFE6EE9C)
    ),
    PEACH(
        displayName = "Peach",
        size = FruitSize.MEDIUM,
        primaryColor = Color(0xFFFF8A65),
        secondaryColor = Color(0xFFE64A19),
        accentColor = Color(0xFFFFCCBC)
    ),
    STRAWBERRY(
        displayName = "Strawberry",
        size = FruitSize.SMALL,
        primaryColor = Color(0xFFE91E63),
        secondaryColor = Color(0xFFAD1457),
        accentColor = Color(0xFFF8BBD0)
    ),
    GRAPES(
        displayName = "Grapes",
        size = FruitSize.LARGE,
        primaryColor = Color(0xFF7E57C2),
        secondaryColor = Color(0xFF4527A0),
        accentColor = Color(0xFFD1C4E9)
    ),
    WATERMELON(
        displayName = "Watermelon",
        size = FruitSize.LARGE,
        primaryColor = Color(0xFF43A047),
        secondaryColor = Color(0xFF1B5E20),
        accentColor = Color(0xFF81C784)
    )
}

enum class SpecialFruit {
    NORMAL,
    FROZEN,
    GOLDEN,
    ROTTEN,
    GIANT
}

data class FruitItem(
    val id: String,
    val type: FruitType,
    val special: SpecialFruit = SpecialFruit.NORMAL,
    val isFrozen: Boolean = special == SpecialFruit.FROZEN,
    val iceCracks: Int = 0
)

enum class ContainerStyle(
    val displayName: String,
    val description: String,
    val costCoins: Int,
    val isPremium: Boolean = false,
    val iconEmoji: String = "🏺"
) {
    GLASS_JAR("Glass Jar", "Classic crystal transparent container", 0, false, "🏺"),
    FRUIT_BASKET("Fruit Basket", "Hand-woven wicker garden basket", 200, false, "🧺"),
    FRUIT_BOWL("Fruit Bowl", "Modern ceramic smoothie bowl", 350, false, "🥣"),
    WOODEN_CRATE("Wooden Crate", "Rustic farm harvest crate", 500, false, "📦"),
    ICE_BOX("Ice Box", "Frosted polar cryo cooler", 750, false, "🧊"),
    GOLD_CRYSTAL("Gold Crystal", "Royal glass with 24k gold rim", 1000, true, "👑"),
    NEON_TUBE("Neon Cyber Tube", "Futuristic pulsing cyberpunk tube", 1200, false, "⚡"),
    BAMBOO_JAR("Zen Bamboo", "Natural handcrafted Japanese bamboo", 1400, false, "🎋"),
    POTION_FLASK("Magic Flask", "Alchemist mystical bubble flask", 1600, false, "🧪"),
    DIAMOND_VASE("Diamond Luxe", "Ultra-brilliant sparkling gemstone vase", 2000, true, "💎")
}

enum class ThemeBackground(
    val displayName: String,
    val description: String,
    val topColor: Color,
    val midColor: Color,
    val botColor: Color,
    val accentGlow: Color,
    val costCoins: Int,
    val gemCost: Int = 0,
    val iconEmoji: String
) {
    NEON_NIGHT(
        displayName = "Neon Midnight",
        description = "Deep dark indigo with sleek neon night glow",
        topColor = Color(0xFF0D0824),
        midColor = Color(0xFF160E36),
        botColor = Color(0xFF090417),
        accentGlow = Color(0xFF7C4DFF),
        costCoins = 0,
        gemCost = 0,
        iconEmoji = "🌌"
    ),
    TROPICAL_OASIS(
        displayName = "Tropical Sunset",
        description = "Warm Hawaiian golden hour with ocean breeze",
        topColor = Color(0xFF2A0845),
        midColor = Color(0xFF642B73),
        botColor = Color(0xFFC6426E),
        accentGlow = Color(0xFFFFB300),
        costCoins = 300,
        gemCost = 0,
        iconEmoji = "🌴"
    ),
    COSMIC_GALAXY(
        displayName = "Cosmic Galaxy",
        description = "Deep nebula with glowing stardust constellations",
        topColor = Color(0xFF020111),
        midColor = Color(0xFF191654),
        botColor = Color(0xFF43C6AC),
        accentGlow = Color(0xFF00E5FF),
        costCoins = 600,
        gemCost = 0,
        iconEmoji = "🪐"
    ),
    ENCHANTED_FOREST(
        displayName = "Emerald Forest",
        description = "Mystical enchanted grove filled with magical fireflies",
        topColor = Color(0xFF06291C),
        midColor = Color(0xFF0F4C3A),
        botColor = Color(0xFF041912),
        accentGlow = Color(0xFF00E676),
        costCoins = 850,
        gemCost = 0,
        iconEmoji = "🌲"
    ),
    CYBERPUNK_2099(
        displayName = "Cyberpunk 2099",
        description = "Electric laser grid with high-voltage synthwave vibes",
        topColor = Color(0xFF12032B),
        midColor = Color(0xFF2E0854),
        botColor = Color(0xFF03001E),
        accentGlow = Color(0xFFFF007F),
        costCoins = 1200,
        gemCost = 0,
        iconEmoji = "🌆"
    ),
    VOLCANO_FORGE(
        displayName = "Volcano Magma",
        description = "Fiery obsidian caverns with glowing lava embers",
        topColor = Color(0xFF2E0909),
        midColor = Color(0xFF5A1414),
        botColor = Color(0xFF1A0404),
        accentGlow = Color(0xFFFF3D00),
        costCoins = 1500,
        gemCost = 0,
        iconEmoji = "🌋"
    ),
    PASTEL_CANDY(
        displayName = "Candy Wonderland",
        description = "Sweet dreamy cotton candy clouds and sugar sparkles",
        topColor = Color(0xFF38153A),
        midColor = Color(0xFF5C2960),
        botColor = Color(0xFF230D25),
        accentGlow = Color(0xFFFF80AB),
        costCoins = 1800,
        gemCost = 0,
        iconEmoji = "🍭"
    ),
    ROYAL_PALACE(
        displayName = "Imperial Gold",
        description = "Opulent palace hall with polished obsidian and 24K gold",
        topColor = Color(0xFF1A1504),
        midColor = Color(0xFF382C05),
        botColor = Color(0xFF0E0B02),
        accentGlow = Color(0xFFFFD700),
        costCoins = 2500,
        gemCost = 25,
        iconEmoji = "👑"
    )
}

enum class FruitSkinPack(
    val displayName: String,
    val description: String,
    val sampleEmojis: String,
    val costCoins: Int,
    val gemCost: Int = 0
) {
    CLASSIC_FRUITS(
        displayName = "Fresh Orchard",
        description = "Handpicked vibrant 3D fruits with rich details",
        sampleEmojis = "🍎 🍊 🍌 🍓 🍉",
        costCoins = 0
    ),
    TROPICAL_BERRIES(
        displayName = "Berry & Citrus Feast",
        description = "Juicy blueberries, tart cherries & fresh citrus",
        sampleEmojis = "🫐 🍒 🍋 🥝 🍇",
        costCoins = 450
    ),
    CRYSTAL_GEMS(
        displayName = "Precious Crystal Gems",
        description = "Gleaming ruby, sapphire, emerald & topaz crystals",
        sampleEmojis = "💎 💠 🔮 💍 🌟",
        costCoins = 900
    ),
    SWEET_DESSERTS(
        displayName = "Sweet Bakery Treats",
        description = "Delicious donuts, cupcakes, macarons & candies",
        sampleEmojis = "🍩 🧁 🍪 🍰 🍧",
        costCoins = 1400
    ),
    CUTE_EMOJIS(
        displayName = "Cute Joy Emojis",
        description = "Charming lucky stars, fire, clouds & magic icons",
        sampleEmojis = "⭐ 🔥 🌈 🍀 💖",
        costCoins = 2000,
        gemCost = 20
    )
}

data class ContainerState(
    val id: Int,
    val capacity: Int = 4,
    val fruits: List<FruitItem> = emptyList(),
    val isLocked: Boolean = false,
    val style: ContainerStyle = ContainerStyle.GLASS_JAR
) {
    val isFull: Boolean get() = fruits.size >= capacity
    val isEmpty: Boolean get() = fruits.isEmpty()
    val topFruit: FruitItem? get() = fruits.lastOrNull()
    val isSorted: Boolean
        get() = fruits.isNotEmpty() && fruits.size == capacity &&
                fruits.all { it.type == fruits.first().type && it.special != SpecialFruit.ROTTEN }
    val isHomogeneous: Boolean
        get() = fruits.isNotEmpty() &&
                fruits.all { it.type == fruits.first().type && it.special != SpecialFruit.ROTTEN }
}

enum class GameMode {
    CLASSIC,
    CHALLENGE,
    DAILY_PUZZLE,
    ENDLESS
}

data class MoveRecord(
    val fromContainerId: Int,
    val toContainerId: Int,
    val fruit: FruitItem
)

enum class PowerUpType(
    val displayName: String,
    val description: String,
    val coinCost: Int,
    val gemCost: Int,
    val iconEmoji: String
) {
    UNDO("Undo", "Revert your previous fruit move", 30, 0, "↩️"),
    HAMMER("Hammer", "Smash and pulverize any difficult fruit", 40, 0, "🔨"),
    SHUFFLE("Shuffle", "Rearrange unsolved fruits safely", 50, 0, "🔄"),
    BOMB("Bomb", "Explode any obstacle or clear a jar", 0, 8, "💣"),
    HINT("Hint", "Highlight the next optimal move", 20, 0, "💡")
}
