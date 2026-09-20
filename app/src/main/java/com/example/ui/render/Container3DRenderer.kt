package com.example.ui.render

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.ContainerState
import com.example.model.ContainerStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Container3DRenderer {

    fun drawContainerBackground(
        drawScope: DrawScope,
        container: ContainerState,
        width: Float,
        height: Float,
        isSelected: Boolean = false,
        isHintHighlighted: Boolean = false,
        pulseAnim: Float = 1f
    ) {
        drawScope.apply {
            val left = 6f
            val top = 16f
            val w = width - 12f
            val h = height - 24f
            val cornerRadius = w * 0.28f

            // 1. Highlight / Selected Glow
            if (isSelected || isHintHighlighted || container.isSorted) {
                val glowColor = when {
                    container.isSorted -> Color(0xFFFFD700)
                    isHintHighlighted -> Color(0xFF00E676)
                    isSelected -> Color(0xFF40C4FF)
                    else -> Color.Transparent
                }
                val glowRadius = 14f * pulseAnim
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.6f), Color.Transparent),
                        center = Offset(width / 2f, height / 2f),
                        radius = (w + h) / 2f
                    ),
                    topLeft = Offset(left - glowRadius, top - glowRadius),
                    size = Size(w + glowRadius * 2f, h + glowRadius * 2f),
                    cornerRadius = CornerRadius(cornerRadius + 6f)
                )
            }

            // 2. Container Back Wall & Shadow
            when (container.style) {
                ContainerStyle.GLASS_JAR, ContainerStyle.GOLD_CRYSTAL -> {
                    // Glass inner tint
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x33FFFFFF),
                                Color(0x15FFFFFF),
                                Color(0x28FFFFFF)
                            ),
                            startY = top,
                            endY = top + h
                        ),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                }
                ContainerStyle.FRUIT_BASKET -> {
                    // Wicker background
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF8D6E63), Color(0xFF5D4037)),
                            startY = top,
                            endY = top + h
                        ),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                }
                ContainerStyle.FRUIT_BOWL -> {
                    // Ceramic bowl interior
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC)),
                            startY = top,
                            endY = top + h
                        ),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                }
                ContainerStyle.WOODEN_CRATE -> {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFBCAAA4), Color(0xFF6D4C41)),
                            startY = top,
                            endY = top + h
                        ),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
                ContainerStyle.ICE_BOX -> {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x6680D8FF), Color(0x440091EA)),
                            startY = top,
                            endY = top + h
                        ),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                }
                ContainerStyle.NEON_TUBE -> {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x3300E5FF), Color(0x1A7C4DFF), Color(0x44D500F9)),
                            startY = top,
                            endY = top + h
                        ),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                }
                ContainerStyle.BAMBOO_JAR -> {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF81C784), Color(0xFF388E3C), Color(0xFF1B5E20)),
                            startY = top,
                            endY = top + h
                        ),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                }
                ContainerStyle.POTION_FLASK -> {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x44E040FB), Color(0x227C4DFF), Color(0x55651FFF)),
                            startY = top,
                            endY = top + h
                        ),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                }
                ContainerStyle.DIAMOND_VASE -> {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x55E0F7FA), Color(0x3380DEEA), Color(0x5500E5FF)),
                            startY = top,
                            endY = top + h
                        ),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                }
            }
        }
    }

    fun drawContainerForeground(
        drawScope: DrawScope,
        container: ContainerState,
        width: Float,
        height: Float,
        isSelected: Boolean = false,
        isHintHighlighted: Boolean = false,
        pulseAnim: Float = 1f
    ) {
        drawScope.apply {
            val left = 6f
            val top = 16f
            val w = width - 12f
            val h = height - 24f
            val cornerRadius = w * 0.28f

            when (container.style) {
                ContainerStyle.GLASS_JAR -> drawGlassForeground(this, left, top, w, h, cornerRadius, isGold = false)
                ContainerStyle.GOLD_CRYSTAL -> drawGlassForeground(this, left, top, w, h, cornerRadius, isGold = true)
                ContainerStyle.FRUIT_BASKET -> drawBasketForeground(this, left, top, w, h, cornerRadius)
                ContainerStyle.FRUIT_BOWL -> drawBowlForeground(this, left, top, w, h, cornerRadius)
                ContainerStyle.WOODEN_CRATE -> drawCrateForeground(this, left, top, w, h)
                ContainerStyle.ICE_BOX -> drawIceBoxForeground(this, left, top, w, h, cornerRadius)
                ContainerStyle.NEON_TUBE -> drawNeonTubeForeground(this, left, top, w, h, cornerRadius)
                ContainerStyle.BAMBOO_JAR -> drawBambooForeground(this, left, top, w, h)
                ContainerStyle.POTION_FLASK -> drawPotionForeground(this, left, top, w, h, cornerRadius)
                ContainerStyle.DIAMOND_VASE -> drawDiamondForeground(this, left, top, w, h, cornerRadius)
            }

            // Sorted Celebration Badge / Cork
            if (container.isSorted) {
                // Top Golden Star Cap
                val capY = top - 4f
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFEE58), Color(0xFFFFB300)),
                        center = Offset(width / 2f, capY),
                        radius = w * 0.45f
                    ),
                    topLeft = Offset(left + w * 0.1f, capY - 6f),
                    size = Size(w * 0.8f, 16f)
                )

                // Sparkling Crown Checkmark Badge
                val badgeY = top + h * 0.5f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF4CAF50), Color(0xFF1B5E20)),
                        center = Offset(width / 2f, badgeY),
                        radius = 24f
                    ),
                    radius = 20f,
                    center = Offset(width / 2f, badgeY)
                )
                // Checkmark
                val checkPath = Path().apply {
                    moveTo(width / 2f - 9f, badgeY)
                    lineTo(width / 2f - 3f, badgeY + 6f)
                    lineTo(width / 2f + 9f, badgeY - 6f)
                }
                drawPath(
                    path = checkPath,
                    color = Color.White,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }

            // Locked Container Overlay
            if (container.isLocked) {
                drawRoundRect(
                    color = Color(0x99000000),
                    topLeft = Offset(left, top),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(cornerRadius)
                )
                // Big Lock Icon Center
                val lockCenter = Offset(width / 2f, height / 2f)
                // Lock body
                drawRoundRect(
                    color = Color(0xFFFFC107),
                    topLeft = Offset(lockCenter.x - 16f, lockCenter.y - 4f),
                    size = Size(32f, 26f),
                    cornerRadius = CornerRadius(6f)
                )
                // Shackle
                drawArc(
                    color = Color(0xFFE0E0E0),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(lockCenter.x - 12f, lockCenter.y - 20f),
                    size = Size(24f, 24f),
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
            }
        }
    }

    private fun drawGlassForeground(
        drawScope: DrawScope,
        left: Float,
        top: Float,
        w: Float,
        h: Float,
        cornerRadius: Float,
        isGold: Boolean
    ) {
        drawScope.apply {
            val strokeColor = if (isGold) Color(0xFFFFD700) else Color(0x99E0F7FA)
            val strokeWidth = if (isGold) 4.5f else 3.5f

            // Outer Glass Tube Border
            drawRoundRect(
                color = strokeColor,
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidth)
            )

            // Top Lip Ellipse
            val lipColor = if (isGold) Color(0xFFFFD700) else Color(0xCCFFFFFF)
            drawOval(
                color = lipColor,
                topLeft = Offset(left + 2f, top - 3f),
                size = Size(w - 4f, 10f),
                style = Stroke(width = strokeWidth)
            )

            // Glass Vertical Specular Reflection (Left side curved shine)
            val shinePath = Path().apply {
                moveTo(left + w * 0.16f, top + 12f)
                lineTo(left + w * 0.16f, top + h - 18f)
            }
            drawPath(
                path = shinePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.7f)
                    ),
                    startY = top,
                    endY = top + h
                ),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            // Glass Right Subtle Rim Reflection
            val rightShine = Path().apply {
                moveTo(left + w * 0.88f, top + 16f)
                lineTo(left + w * 0.88f, top + h - 22f)
            }
            drawPath(
                path = rightShine,
                color = Color.White.copy(alpha = 0.35f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // Bottom Glass Thickness Lens
            drawArc(
                color = Color.White.copy(alpha = 0.5f),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(left + w * 0.1f, top + h - 16f),
                size = Size(w * 0.8f, 12f),
                style = Stroke(width = 3f)
            )
        }
    }

    private fun drawBasketForeground(drawScope: DrawScope, left: Float, top: Float, w: Float, h: Float, cornerRadius: Float) {
        drawScope.apply {
            // Wicker lattice lines
            val latticeColor = Color(0x443E2723)
            val strokeW = 2.5f
            for (i in 1..4) {
                val y = top + (h / 5f) * i
                drawLine(color = latticeColor, start = Offset(left + 4f, y), end = Offset(left + w - 4f, y), strokeWidth = strokeW)
            }
            // Rim border
            drawRoundRect(
                color = Color(0xFFD7CCC8),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = 4f)
            )
        }
    }

    private fun drawBowlForeground(drawScope: DrawScope, left: Float, top: Float, w: Float, h: Float, cornerRadius: Float) {
        drawScope.apply {
            drawRoundRect(
                color = Color(0xFFB0BEC5),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = 4f)
            )
        }
    }

    private fun drawCrateForeground(drawScope: DrawScope, left: Float, top: Float, w: Float, h: Float) {
        drawScope.apply {
            val plankColor = Color(0xFF4E342E)
            for (i in 1..3) {
                val y = top + (h / 4f) * i
                drawLine(color = plankColor, start = Offset(left, y), end = Offset(left + w, y), strokeWidth = 3f)
            }
            drawRoundRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(8f),
                style = Stroke(width = 4.5f)
            )
        }
    }

    private fun drawIceBoxForeground(drawScope: DrawScope, left: Float, top: Float, w: Float, h: Float, cornerRadius: Float) {
        drawScope.apply {
            drawRoundRect(
                color = Color(0xFFE1F5FE),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = 4f)
            )
            // Frost sparkle
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(left + 8f, top + 10f),
                end = Offset(left + w * 0.4f, top + 10f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }

    private fun drawNeonTubeForeground(drawScope: DrawScope, left: Float, top: Float, w: Float, h: Float, cornerRadius: Float) {
        drawScope.apply {
            // Glowing cyan/magenta neon rim
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFFE040FB)),
                    startY = top,
                    endY = top + h
                ),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = 4f)
            )
            // Circuit lines
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                start = Offset(left + 6f, top + 14f),
                end = Offset(left + 6f, top + h - 14f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFE040FB).copy(alpha = 0.8f),
                start = Offset(left + w - 6f, top + 14f),
                end = Offset(left + w - 6f, top + h - 14f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }
    }

    private fun drawBambooForeground(drawScope: DrawScope, left: Float, top: Float, w: Float, h: Float) {
        drawScope.apply {
            val bambooDark = Color(0xFF2E7D32)
            val bambooLight = Color(0xFF81C784)
            // Bamboo segment nodes
            for (i in 1..3) {
                val y = top + (h / 4f) * i
                drawRoundRect(
                    color = bambooDark,
                    topLeft = Offset(left - 2f, y - 4f),
                    size = Size(w + 4f, 8f),
                    cornerRadius = CornerRadius(4f)
                )
                drawLine(
                    color = bambooLight,
                    start = Offset(left + 2f, y),
                    end = Offset(left + w - 2f, y),
                    strokeWidth = 2f
                )
            }
            drawRoundRect(
                color = bambooDark,
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(14f),
                style = Stroke(width = 4f)
            )
        }
    }

    private fun drawPotionForeground(drawScope: DrawScope, left: Float, top: Float, w: Float, h: Float, cornerRadius: Float) {
        drawScope.apply {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFEA80FC), Color(0xFF7C4DFF)),
                    startY = top,
                    endY = top + h
                ),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = 3.5f)
            )
            // Magical bubble accents
            drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 3.5f, center = Offset(left + w * 0.3f, top + h * 0.4f))
            drawCircle(color = Color.White.copy(alpha = 0.7f), radius = 2.5f, center = Offset(left + w * 0.7f, top + h * 0.65f))
            drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 4.5f, center = Offset(left + w * 0.45f, top + h * 0.8f))
        }
    }

    private fun drawDiamondForeground(drawScope: DrawScope, left: Float, top: Float, w: Float, h: Float, cornerRadius: Float) {
        drawScope.apply {
            // Faceted crystal border with diamond sparkle
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0xFF80D8FF)),
                    startY = top,
                    endY = top + h
                ),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = 4.5f)
            )
            // Gem Facet shine
            val facet = Path().apply {
                moveTo(left + w * 0.5f, top + 6f)
                lineTo(left + w * 0.85f, top + h * 0.3f)
                lineTo(left + w * 0.5f, top + h * 0.9f)
                lineTo(left + w * 0.15f, top + h * 0.3f)
                close()
            }
            drawPath(path = facet, color = Color.White.copy(alpha = 0.18f))
        }
    }
}
