package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ContainerState
import com.example.ui.render.Container3DRenderer
import com.example.ui.render.Fruit3DRenderer
import kotlin.math.sin

@Composable
fun FruitJarView(
    container: ContainerState,
    isSelected: Boolean,
    isHintHighlighted: Boolean,
    isTargetHint: Boolean = false,
    hiddenTopFruit: Boolean = false,
    impactSquashPulse: Float = 0f,
    onPositionChanged: ((Offset, Float, Float) -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jar_pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val sparklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle"
    )

    // Smooth physics spring lift animation for selected top fruit
    val topFruitLiftOffset by animateFloatAsState(
        targetValue = if (isSelected) -46f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "top_lift"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .width(74.dp)
            .height(180.dp)
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInParent()
                onPositionChanged?.invoke(bounds.topLeft, bounds.width, bounds.height)
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag("container_jar_${container.id}")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Container 3D Background & Glass Fill
            Container3DRenderer.drawContainerBackground(
                drawScope = this,
                container = container,
                width = w,
                height = h,
                isSelected = isSelected,
                isHintHighlighted = isHintHighlighted || isTargetHint,
                pulseAnim = pulseAnim
            )

            // 2. Draw Stacked Fruits
            val totalSlots = container.capacity.coerceAtLeast(4)
            val usableHeight = h - 42f
            val slotHeight = usableHeight / totalSlots
            val fruitRadius = (w - 20f) / 2f

            val fruitsToDraw = if (hiddenTopFruit && container.fruits.isNotEmpty()) {
                container.fruits.dropLast(1)
            } else {
                container.fruits
            }

            fruitsToDraw.forEachIndexed { index, fruit ->
                val isTop = index == fruitsToDraw.size - 1 && !hiddenTopFruit
                val baseCenterY = h - 22f - (index + 0.5f) * slotHeight

                // Hover bobbing when lifted
                val floatBob = if (isTop && isSelected) sin(sparklePhase * 2.5f) * 3f else 0f
                val finalCenterY = if (isTop) baseCenterY + topFruitLiftOffset + floatBob else baseCenterY

                val centerX = w / 2f

                // Reactive squash pulse on lower fruits when a new fruit impacts
                val stackCompressionY = if (impactSquashPulse > 0.01f) {
                    1f - (impactSquashPulse * 0.12f * (1f - index * 0.2f))
                } else 1f
                val stackExpansionX = if (impactSquashPulse > 0.01f) {
                    1f + (impactSquashPulse * 0.14f * (1f - index * 0.2f))
                } else 1f

                Fruit3DRenderer.draw3DFruit(
                    drawScope = this,
                    fruit = fruit,
                    center = Offset(centerX, finalCenterY),
                    radius = fruitRadius,
                    rotationDegrees = if (isTop && isSelected) -7f + sin(sparklePhase * 2f) * 2.5f else 0f,
                    scaleX = stackExpansionX,
                    scaleY = stackCompressionY,
                    sparklePhase = sparklePhase
                )
            }

            // 3. Draw Container 3D Glass Foreground, specular light beams, locks, and cork
            Container3DRenderer.drawContainerForeground(
                drawScope = this,
                container = container,
                width = w,
                height = h,
                isSelected = isSelected,
                isHintHighlighted = isHintHighlighted || isTargetHint,
                pulseAnim = pulseAnim
            )
        }
    }
}
