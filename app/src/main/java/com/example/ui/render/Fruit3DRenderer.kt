package com.example.ui.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.model.FruitItem
import com.example.model.FruitSize
import com.example.model.FruitType
import com.example.model.SpecialFruit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Fruit3DRenderer {

    fun draw3DFruit(
        drawScope: DrawScope,
        fruit: FruitItem,
        center: Offset,
        radius: Float,
        rotationDegrees: Float = 0f,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        sparklePhase: Float = 0f
    ) {
        drawScope.apply {
            rotate(degrees = rotationDegrees, pivot = center) {
                scale(scaleX = scaleX, scaleY = scaleY, pivot = center) {
                    // Size multiplier based on fruit size
                    val sizeMultiplier = when (fruit.type.size) {
                        FruitSize.SMALL -> 0.85f
                        FruitSize.MEDIUM -> 1.0f
                        FruitSize.LARGE -> 1.12f
                    }
                    val effectiveRadius = radius * sizeMultiplier

                    // 1. Soft Bottom Drop Shadow
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x77000000), Color(0x00000000)),
                            center = center.copy(y = center.y + effectiveRadius * 0.75f),
                            radius = effectiveRadius * 0.9f
                        ),
                        topLeft = Offset(center.x - effectiveRadius * 0.8f, center.y + effectiveRadius * 0.45f),
                        size = Size(effectiveRadius * 1.6f, effectiveRadius * 0.6f)
                    )

                    // 2. Main Fruit Body & Anatomy based on FruitType
                    when (fruit.type) {
                        FruitType.APPLE_RED -> drawApple(this, center, effectiveRadius, isRed = true, fruit.special)
                        FruitType.APPLE_GREEN -> drawApple(this, center, effectiveRadius, isRed = false, fruit.special)
                        FruitType.ORANGE -> drawOrange(this, center, effectiveRadius, fruit.special)
                        FruitType.LEMON -> drawLemon(this, center, effectiveRadius, fruit.special)
                        FruitType.BANANA -> drawBanana(this, center, effectiveRadius, fruit.special)
                        FruitType.KIWI -> drawKiwi(this, center, effectiveRadius, fruit.special)
                        FruitType.PEAR -> drawPear(this, center, effectiveRadius, fruit.special)
                        FruitType.PEACH -> drawPeach(this, center, effectiveRadius, fruit.special)
                        FruitType.STRAWBERRY -> drawStrawberry(this, center, effectiveRadius, fruit.special)
                        FruitType.GRAPES -> drawGrapes(this, center, effectiveRadius, fruit.special)
                        FruitType.WATERMELON -> drawWatermelon(this, center, effectiveRadius, fruit.special)
                    }

                    // 3. Special Fruit Overlays
                    when (fruit.special) {
                        SpecialFruit.FROZEN -> drawFrozenOverlay(this, center, effectiveRadius, fruit.iceCracks)
                        SpecialFruit.GOLDEN -> drawGoldenOverlay(this, center, effectiveRadius, sparklePhase)
                        SpecialFruit.ROTTEN -> drawRottenOverlay(this, center, effectiveRadius)
                        SpecialFruit.GIANT -> drawGiantRing(this, center, effectiveRadius)
                        SpecialFruit.NORMAL -> {}
                    }
                }
            }
        }
    }

    fun draw3DFruit(
        drawScope: DrawScope,
        fruit: FruitItem,
        center: Offset,
        radius: Float,
        rotationDegrees: Float = 0f,
        bounceScale: Float = 1f,
        sparklePhase: Float = 0f
    ) {
        draw3DFruit(
            drawScope = drawScope,
            fruit = fruit,
            center = center,
            radius = radius,
            rotationDegrees = rotationDegrees,
            scaleX = bounceScale,
            scaleY = 2f - bounceScale,
            sparklePhase = sparklePhase
        )
    }

    private fun drawApple(drawScope: DrawScope, center: Offset, r: Float, isRed: Boolean, special: SpecialFruit) {
        drawScope.apply {
            val primary = if (special == SpecialFruit.GOLDEN) Color(0xFFFFD700) else if (isRed) Color(0xFFE53935) else Color(0xFF7CB342)
            val shadowColor = if (special == SpecialFruit.GOLDEN) Color(0xFFB8860B) else if (isRed) Color(0xFF8E0000) else Color(0xFF33691E)
            val highlight = if (special == SpecialFruit.GOLDEN) Color(0xFFFFF9C4) else if (isRed) Color(0xFFFF8A80) else Color(0xFFDCEDC8)

            // Stem (Behind body)
            val stemPath = Path().apply {
                moveTo(center.x, center.y - r * 0.7f)
                cubicTo(
                    center.x + r * 0.1f, center.y - r * 1.1f,
                    center.x + r * 0.25f, center.y - r * 1.15f,
                    center.x + r * 0.35f, center.y - r * 1.25f
                )
            }
            drawPath(
                path = stemPath,
                color = Color(0xFF5D4037),
                style = Stroke(width = r * 0.12f, cap = StrokeCap.Round)
            )

            // Leaf
            val leafPath = Path().apply {
                moveTo(center.x + r * 0.1f, center.y - r * 0.95f)
                cubicTo(
                    center.x + r * 0.55f, center.y - r * 1.2f,
                    center.x + r * 0.65f, center.y - r * 0.85f,
                    center.x + r * 0.2f, center.y - r * 0.8f
                )
                close()
            }
            drawPath(path = leafPath, color = Color(0xFF4CAF50))

            // Main 3D Spherical Apple body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(highlight, primary, shadowColor),
                    center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
                    radius = r * 1.3f
                ),
                radius = r * 0.92f,
                center = center
            )

            // Top Indentation Dimple
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(shadowColor.copy(alpha = 0.8f), Color.Transparent),
                    center = Offset(center.x, center.y - r * 0.65f),
                    radius = r * 0.35f
                ),
                topLeft = Offset(center.x - r * 0.25f, center.y - r * 0.8f),
                size = Size(r * 0.5f, r * 0.3f)
            )

            // Glossy Specular Light Highlight
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0f)),
                    center = Offset(center.x - r * 0.38f, center.y - r * 0.38f),
                    radius = r * 0.35f
                ),
                topLeft = Offset(center.x - r * 0.55f, center.y - r * 0.55f),
                size = Size(r * 0.45f, r * 0.32f)
            )
        }
    }

    private fun drawOrange(drawScope: DrawScope, center: Offset, r: Float, special: SpecialFruit) {
        drawScope.apply {
            val primary = if (special == SpecialFruit.GOLDEN) Color(0xFFFFD700) else Color(0xFFFF9800)
            val shadow = if (special == SpecialFruit.GOLDEN) Color(0xFFB8860B) else Color(0xFFE65100)
            val highlight = if (special == SpecialFruit.GOLDEN) Color(0xFFFFF9C4) else Color(0xFFFFE082)

            // 3D Sphere Body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(highlight, primary, shadow),
                    center = Offset(center.x - r * 0.28f, center.y - r * 0.28f),
                    radius = r * 1.25f
                ),
                radius = r * 0.9f,
                center = center
            )

            // Tiny Orange Pores
            val poreColor = Color(0x33FFFFFF)
            val angles = listOf(0.3, 0.9, 1.8, 2.7, 3.8, 4.6, 5.4)
            for (a in angles) {
                val px = center.x + (r * 0.45f * cos(a)).toFloat()
                val py = center.y + (r * 0.45f * sin(a)).toFloat()
                drawCircle(color = poreColor, radius = r * 0.04f, center = Offset(px, py))
            }

            // Top Star Leaf Calyx
            drawCircle(color = Color(0xFF388E3C), radius = r * 0.12f, center = Offset(center.x, center.y - r * 0.72f))

            // Specular Glaze
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.65f), Color.Transparent),
                    center = Offset(center.x - r * 0.35f, center.y - r * 0.35f),
                    radius = r * 0.3f
                ),
                topLeft = Offset(center.x - r * 0.5f, center.y - r * 0.5f),
                size = Size(r * 0.38f, r * 0.28f)
            )
        }
    }

    private fun drawLemon(drawScope: DrawScope, center: Offset, r: Float, special: SpecialFruit) {
        drawScope.apply {
            val primary = if (special == SpecialFruit.GOLDEN) Color(0xFFFFD700) else Color(0xFFFFEB3B)
            val shadow = if (special == SpecialFruit.GOLDEN) Color(0xFFB8860B) else Color(0xFFF57F17)
            val highlight = Color(0xFFFFFDE7)

            // Lemon Oval with tips
            val lemonPath = Path().apply {
                moveTo(center.x - r * 0.95f, center.y)
                cubicTo(
                    center.x - r * 0.6f, center.y - r * 0.75f,
                    center.x + r * 0.6f, center.y - r * 0.75f,
                    center.x + r * 0.95f, center.y
                )
                cubicTo(
                    center.x + r * 0.6f, center.y + r * 0.75f,
                    center.x - r * 0.6f, center.y + r * 0.75f,
                    center.x - r * 0.95f, center.y
                )
                close()
            }

            drawPath(
                path = lemonPath,
                brush = Brush.radialGradient(
                    colors = listOf(highlight, primary, shadow),
                    center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                    radius = r * 1.2f
                )
            )

            // Tip Nubs
            drawCircle(color = Color(0xFF827717), radius = r * 0.08f, center = Offset(center.x - r * 0.92f, center.y))
            drawCircle(color = Color(0xFF827717), radius = r * 0.08f, center = Offset(center.x + r * 0.92f, center.y))

            // Specular Glaze
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(center.x - r * 0.2f, center.y - r * 0.25f),
                    radius = r * 0.28f
                ),
                topLeft = Offset(center.x - r * 0.35f, center.y - r * 0.4f),
                size = Size(r * 0.45f, r * 0.25f)
            )
        }
    }

    private fun drawBanana(drawScope: DrawScope, center: Offset, r: Float, special: SpecialFruit) {
        drawScope.apply {
            val primary = if (special == SpecialFruit.GOLDEN) Color(0xFFFFD700) else Color(0xFFFFEB3B)
            val shadow = if (special == SpecialFruit.GOLDEN) Color(0xFFB8860B) else Color(0xFFF57F17)
            val tipColor = Color(0xFF5D4037)

            // Banana Curved Arc Crescent
            val bananaPath = Path().apply {
                moveTo(center.x - r * 0.85f, center.y + r * 0.35f)
                cubicTo(
                    center.x - r * 0.3f, center.y + r * 0.8f,
                    center.x + r * 0.4f, center.y + r * 0.7f,
                    center.x + r * 0.95f, center.y - r * 0.35f
                )
                cubicTo(
                    center.x + r * 0.35f, center.y + r * 0.25f,
                    center.x - r * 0.25f, center.y + r * 0.35f,
                    center.x - r * 0.85f, center.y + r * 0.35f
                )
                close()
            }

            drawPath(
                path = bananaPath,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFDE7), primary, shadow),
                    center = Offset(center.x, center.y + r * 0.2f),
                    radius = r * 1.1f
                )
            )

            // Banana Upper Ridge Line
            val ridgePath = Path().apply {
                moveTo(center.x - r * 0.8f, center.y + r * 0.36f)
                cubicTo(
                    center.x - r * 0.25f, center.y + r * 0.55f,
                    center.x + r * 0.35f, center.y + r * 0.48f,
                    center.x + r * 0.9f, center.y - r * 0.3f
                )
            }
            drawPath(
                path = ridgePath,
                color = Color(0x44FFFFFF),
                style = Stroke(width = r * 0.08f, cap = StrokeCap.Round)
            )

            // Banana Stem & Tip
            drawCircle(color = tipColor, radius = r * 0.1f, center = Offset(center.x - r * 0.85f, center.y + r * 0.35f))
            drawCircle(color = Color(0xFF689F38), radius = r * 0.09f, center = Offset(center.x + r * 0.95f, center.y - r * 0.35f))
        }
    }

    private fun drawKiwi(drawScope: DrawScope, center: Offset, r: Float, special: SpecialFruit) {
        drawScope.apply {
            // Cut Kiwi Slice / Whole Kiwi 3D
            val skinColor = Color(0xFF795548)
            val pulpColor = if (special == SpecialFruit.GOLDEN) Color(0xFFFFD700) else Color(0xFF8BC34A)
            val coreColor = Color(0xFFFFF9C4)

            // Brown Outer Fuzzy Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(skinColor, Color(0xFF4E342E)),
                    center = center,
                    radius = r * 0.88f
                ),
                radius = r * 0.85f,
                center = center
            )

            // Juicy Green Pulp
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFAED581), pulpColor, Color(0xFF689F38)),
                    center = center,
                    radius = r * 0.75f
                ),
                radius = r * 0.75f,
                center = center
            )

            // Light Cream Core
            drawOval(
                color = coreColor,
                topLeft = Offset(center.x - r * 0.2f, center.y - r * 0.28f),
                size = Size(r * 0.4f, r * 0.56f)
            )

            // Black Seed Ring
            val seedCount = 10
            for (i in 0 until seedCount) {
                val angle = (i * (2 * PI / seedCount)).toDouble()
                val sx = center.x + (r * 0.42f * cos(angle)).toFloat()
                val sy = center.y + (r * 0.48f * sin(angle)).toFloat()
                drawCircle(color = Color(0xFF212121), radius = r * 0.045f, center = Offset(sx, sy))
            }
        }
    }

    private fun drawPear(drawScope: DrawScope, center: Offset, r: Float, special: SpecialFruit) {
        drawScope.apply {
            val primary = if (special == SpecialFruit.GOLDEN) Color(0xFFFFD700) else Color(0xFFC0CA33)
            val shadow = if (special == SpecialFruit.GOLDEN) Color(0xFFB8860B) else Color(0xFF9E9D24)
            val highlight = Color(0xFFF0F4C3)

            // Stem
            val stemPath = Path().apply {
                moveTo(center.x, center.y - r * 0.85f)
                cubicTo(
                    center.x + r * 0.1f, center.y - r * 1.15f,
                    center.x + r * 0.25f, center.y - r * 1.25f,
                    center.x + r * 0.35f, center.y - r * 1.35f
                )
            }
            drawPath(path = stemPath, color = Color(0xFF5D4037), style = Stroke(width = r * 0.1f, cap = StrokeCap.Round))

            // Teardrop Pear Silhouette
            val pearPath = Path().apply {
                moveTo(center.x - r * 0.35f, center.y - r * 0.65f)
                cubicTo(
                    center.x - r * 0.45f, center.y - r * 0.1f,
                    center.x - r * 0.9f, center.y + r * 0.3f,
                    center.x - r * 0.8f, center.y + r * 0.75f
                )
                cubicTo(
                    center.x - r * 0.5f, center.y + r * 0.95f,
                    center.x + r * 0.5f, center.y + r * 0.95f,
                    center.x + r * 0.8f, center.y + r * 0.75f
                )
                cubicTo(
                    center.x + r * 0.9f, center.y + r * 0.3f,
                    center.x + r * 0.45f, center.y - r * 0.1f,
                    center.x + r * 0.35f, center.y - r * 0.65f
                )
                cubicTo(
                    center.x + r * 0.2f, center.y - r * 0.85f,
                    center.x - r * 0.2f, center.y - r * 0.85f,
                    center.x - r * 0.35f, center.y - r * 0.65f
                )
                close()
            }

            drawPath(
                path = pearPath,
                brush = Brush.radialGradient(
                    colors = listOf(highlight, primary, shadow),
                    center = Offset(center.x - r * 0.25f, center.y),
                    radius = r * 1.3f
                )
            )

            // Specular Glaze
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(center.x - r * 0.3f, center.y + r * 0.2f),
                    radius = r * 0.3f
                ),
                topLeft = Offset(center.x - r * 0.45f, center.y + r * 0.05f),
                size = Size(r * 0.35f, r * 0.35f)
            )
        }
    }

    private fun drawPeach(drawScope: DrawScope, center: Offset, r: Float, special: SpecialFruit) {
        drawScope.apply {
            val primary = if (special == SpecialFruit.GOLDEN) Color(0xFFFFD700) else Color(0xFFFF8A65)
            val shadow = if (special == SpecialFruit.GOLDEN) Color(0xFFB8860B) else Color(0xFFD84315)
            val highlight = Color(0xFFFFCCBC)

            // 3D Peach Heart-Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(highlight, primary, shadow),
                    center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                    radius = r * 1.25f
                ),
                radius = r * 0.9f,
                center = center
            )

            // Peach Cleft Line
            val cleftPath = Path().apply {
                moveTo(center.x, center.y - r * 0.8f)
                cubicTo(
                    center.x + r * 0.1f, center.y - r * 0.2f,
                    center.x - r * 0.05f, center.y + r * 0.4f,
                    center.x + r * 0.05f, center.y + r * 0.85f
                )
            }
            drawPath(
                path = cleftPath,
                color = Color(0x55BF360C),
                style = Stroke(width = r * 0.07f, cap = StrokeCap.Round)
            )

            // Top Leaf
            drawCircle(color = Color(0xFF4CAF50), radius = r * 0.14f, center = Offset(center.x + r * 0.25f, center.y - r * 0.85f))
        }
    }

    private fun drawStrawberry(drawScope: DrawScope, center: Offset, r: Float, special: SpecialFruit) {
        drawScope.apply {
            val primary = if (special == SpecialFruit.GOLDEN) Color(0xFFFFD700) else Color(0xFFE91E63)
            val shadow = if (special == SpecialFruit.GOLDEN) Color(0xFFB8860B) else Color(0xFF880E4F)
            val highlight = Color(0xFFFF80AB)

            // Strawberry Cone Heart Path
            val berryPath = Path().apply {
                moveTo(center.x, center.y + r * 0.9f)
                cubicTo(
                    center.x - r * 0.75f, center.y + r * 0.4f,
                    center.x - r * 0.85f, center.y - r * 0.4f,
                    center.x - r * 0.45f, center.y - r * 0.75f
                )
                cubicTo(
                    center.x - r * 0.1f, center.y - r * 0.85f,
                    center.x + r * 0.1f, center.y - r * 0.85f,
                    center.x + r * 0.45f, center.y - r * 0.75f
                )
                cubicTo(
                    center.x + r * 0.85f, center.y - r * 0.4f,
                    center.x + r * 0.75f, center.y + r * 0.4f,
                    center.x, center.y + r * 0.9f
                )
                close()
            }

            drawPath(
                path = berryPath,
                brush = Brush.radialGradient(
                    colors = listOf(highlight, primary, shadow),
                    center = Offset(center.x - r * 0.25f, center.y - r * 0.15f),
                    radius = r * 1.2f
                )
            )

            // Golden Seed Specks
            val seedOffsets = listOf(
                Offset(-0.35f, -0.3f), Offset(0.0f, -0.4f), Offset(0.35f, -0.3f),
                Offset(-0.45f, 0.0f), Offset(-0.15f, -0.05f), Offset(0.2f, 0.0f), Offset(0.48f, 0.05f),
                Offset(-0.3f, 0.3f), Offset(0.05f, 0.3f), Offset(0.32f, 0.35f),
                Offset(-0.1f, 0.6f), Offset(0.12f, 0.65f)
            )
            for (so in seedOffsets) {
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = r * 0.045f,
                    center = Offset(center.x + r * so.x, center.y + r * so.y)
                )
            }

            // Green Calyx Leaves Top
            val leafColor = Color(0xFF43A047)
            for (angle in listOf(-0.6, -0.2, 0.2, 0.6)) {
                val lx = center.x + (r * 0.45f * sin(angle)).toFloat()
                val ly = center.y - r * 0.72f - (r * 0.2f * cos(angle)).toFloat()
                drawOval(
                    color = leafColor,
                    topLeft = Offset(lx - r * 0.1f, ly - r * 0.1f),
                    size = Size(r * 0.2f, r * 0.3f)
                )
            }
        }
    }

    private fun drawGrapes(drawScope: DrawScope, center: Offset, r: Float, special: SpecialFruit) {
        drawScope.apply {
            val primary = if (special == SpecialFruit.GOLDEN) Color(0xFFFFD700) else Color(0xFF7E57C2)
            val shadow = if (special == SpecialFruit.GOLDEN) Color(0xFFB8860B) else Color(0xFF311B92)
            val highlight = Color(0xFFD1C4E9)

            // Cluster of grapes
            val grapeOrbs = listOf(
                Offset(-0.35f, -0.35f) to 0.32f,
                Offset(0.0f, -0.45f) to 0.34f,
                Offset(0.35f, -0.35f) to 0.32f,
                Offset(-0.42f, 0.0f) to 0.33f,
                Offset(0.0f, -0.05f) to 0.35f,
                Offset(0.42f, 0.0f) to 0.33f,
                Offset(-0.25f, 0.35f) to 0.31f,
                Offset(0.25f, 0.35f) to 0.31f,
                Offset(0.0f, 0.65f) to 0.28f
            )

            for ((relPos, orbR) in grapeOrbs) {
                val orbCenter = Offset(center.x + r * relPos.x, center.y + r * relPos.y)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(highlight, primary, shadow),
                        center = Offset(orbCenter.x - r * orbR * 0.35f, orbCenter.y - r * orbR * 0.35f),
                        radius = r * orbR * 1.2f
                    ),
                    radius = r * orbR,
                    center = orbCenter
                )
                // Orb gloss
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = r * orbR * 0.2f,
                    center = Offset(orbCenter.x - r * orbR * 0.35f, orbCenter.y - r * orbR * 0.35f)
                )
            }

            // Top Vine Curl
            drawCircle(color = Color(0xFF689F38), radius = r * 0.1f, center = Offset(center.x, center.y - r * 0.8f))
        }
    }

    private fun drawWatermelon(drawScope: DrawScope, center: Offset, r: Float, special: SpecialFruit) {
        drawScope.apply {
            val rindLight = Color(0xFF7CB342)
            val rindDark = Color(0xFF1B5E20)

            // Outer Base Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFAED581), rindLight, Color(0xFF33691E)),
                    center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                    radius = r * 1.25f
                ),
                radius = r * 0.92f,
                center = center
            )

            // Dark Green Wavy Stripes
            val stripeAngles = listOf(-0.6, -0.2, 0.2, 0.6)
            for (sa in stripeAngles) {
                val stripePath = Path().apply {
                    val startX = center.x + (r * 0.8f * sin(sa)).toFloat()
                    val startY = center.y - (r * 0.85f * cos(sa)).toFloat()
                    val endX = center.x + (r * 0.8f * sin(sa)).toFloat()
                    val endY = center.y + (r * 0.85f * cos(sa)).toFloat()
                    moveTo(startX, startY)
                    cubicTo(
                        startX + r * 0.15f, center.y - r * 0.3f,
                        endX - r * 0.15f, center.y + r * 0.3f,
                        endX, endY
                    )
                }
                drawPath(
                    path = stripePath,
                    color = rindDark,
                    style = Stroke(width = r * 0.18f, cap = StrokeCap.Round)
                )
            }

            // Specular Reflection
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(center.x - r * 0.35f, center.y - r * 0.35f),
                    radius = r * 0.3f
                ),
                topLeft = Offset(center.x - r * 0.5f, center.y - r * 0.5f),
                size = Size(r * 0.4f, r * 0.3f)
            )
        }
    }

    private fun drawFrozenOverlay(drawScope: DrawScope, center: Offset, r: Float, cracks: Int) {
        drawScope.apply {
            // Crystalline Translucent Ice Cube
            val iceColor = Color(0x99B3E5FC)
            val iceRim = Color(0xDDFFFFFF)

            drawRoundRect(
                color = iceColor,
                topLeft = Offset(center.x - r * 0.95f, center.y - r * 0.95f),
                size = Size(r * 1.9f, r * 1.9f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.25f, r * 0.25f)
            )
            drawRoundRect(
                color = iceRim,
                topLeft = Offset(center.x - r * 0.95f, center.y - r * 0.95f),
                size = Size(r * 1.9f, r * 1.9f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.25f, r * 0.25f),
                style = Stroke(width = r * 0.08f)
            )

            // Ice Shard Facet Line
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(center.x - r * 0.8f, center.y - r * 0.8f),
                end = Offset(center.x + r * 0.8f, center.y + r * 0.8f),
                strokeWidth = r * 0.05f
            )

            // Star Sparkle
            drawCircle(color = Color.White, radius = r * 0.1f, center = Offset(center.x - r * 0.5f, center.y - r * 0.5f))
        }
    }

    private fun drawGoldenOverlay(drawScope: DrawScope, center: Offset, r: Float, phase: Float) {
        drawScope.apply {
            // Golden Shimmer Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66FFD700), Color(0x00FFD700)),
                    center = center,
                    radius = r * 1.4f
                ),
                radius = r * 1.35f,
                center = center
            )

            // Rotating 4-point Diamond Sparkles
            val sparkleX = center.x + (r * 0.7f * cos(phase.toDouble())).toFloat()
            val sparkleY = center.y + (r * 0.7f * sin(phase.toDouble())).toFloat()
            val sparkPath = Path().apply {
                moveTo(sparkleX, sparkleY - r * 0.2f)
                lineTo(sparkleX + r * 0.08f, sparkleY)
                lineTo(sparkleX, sparkleY + r * 0.2f)
                lineTo(sparkleX - r * 0.08f, sparkleY)
                close()
            }
            drawPath(path = sparkPath, color = Color.White)
        }
    }

    private fun drawRottenOverlay(drawScope: DrawScope, center: Offset, r: Float) {
        drawScope.apply {
            // Dark spoilage patches
            val darkPatch = Color(0xAA3E2723)
            drawCircle(color = darkPatch, radius = r * 0.28f, center = Offset(center.x + r * 0.35f, center.y + r * 0.25f))
            drawCircle(color = darkPatch, radius = r * 0.22f, center = Offset(center.x - r * 0.3f, center.y + r * 0.35f))
            // Dust/Fly Dot
            drawCircle(color = Color(0xFF212121), radius = r * 0.06f, center = Offset(center.x + r * 0.65f, center.y - r * 0.5f))
        }
    }

    private fun drawGiantRing(drawScope: DrawScope, center: Offset, r: Float) {
        drawScope.apply {
            drawCircle(
                color = Color(0xFFFF5722),
                radius = r * 1.05f,
                center = center,
                style = Stroke(width = r * 0.08f)
            )
        }
    }
}
