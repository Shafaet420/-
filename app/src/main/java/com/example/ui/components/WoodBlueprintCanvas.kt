package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.WoodMode
import com.example.ui.theme.HeroBrassAccent
import com.example.ui.theme.HeroCreamText

/**
 * Lightweight custom Canvas blueprint showing an isometric Round Log (with girth band & length guide)
 * or Sawn Timber Plank (with L × W × T guides) inside the Hero Result Card.
 */
@Composable
fun WoodBlueprintCanvas(
    mode: WoodMode,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        when (mode) {
            WoodMode.ROUND_LOG -> drawRoundLogDiagram(w, h)
            WoodMode.SAWN_TIMBER -> drawSawnTimberDiagram(w, h)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundLogDiagram(
    w: Float,
    h: Float
) {
    val creamLine = HeroCreamText.copy(alpha = 0.88f)
    val creamFill = HeroCreamText.copy(alpha = 0.14f)
    val ringColor = HeroCreamText.copy(alpha = 0.45f)
    val brassGuide = HeroBrassAccent

    val rx = w * 0.11f
    val ry = h * 0.27f
    val leftCx = w * 0.24f
    val rightCx = w * 0.80f
    val cy = h * 0.54f

    // Log trunk body silhouette
    val bodyPath = Path().apply {
        moveTo(leftCx, cy - ry)
        lineTo(rightCx, cy - ry)
        // Right rounded end
        cubicTo(
            rightCx + rx * 1.15f, cy - ry,
            rightCx + rx * 1.15f, cy + ry,
            rightCx, cy + ry
        )
        lineTo(leftCx, cy + ry)
        close()
    }
    drawPath(path = bodyPath, color = creamFill)
    drawPath(
        path = bodyPath,
        color = creamLine,
        style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Subtle bark grain lines along trunk
    val grainOffsets = listOf(-0.48f, -0.10f, 0.36f)
    grainOffsets.forEachIndexed { idx, factor ->
        val gy = cy + ry * factor
        val startX = leftCx + rx * (0.9f + idx * 0.2f)
        val endX = rightCx - rx * (0.3f + (idx % 2) * 0.4f)
        drawLine(
            color = HeroCreamText.copy(alpha = 0.22f),
            start = Offset(startX, gy),
            end = Offset(endX, gy),
            strokeWidth = 1.6f,
            cap = StrokeCap.Round
        )
    }

    // Left cross-section oval (cut end showing growth rings)
    drawOval(
        color = HeroCreamText.copy(alpha = 0.22f),
        topLeft = Offset(leftCx - rx, cy - ry),
        size = Size(rx * 2f, ry * 2f)
    )
    drawOval(
        color = creamLine,
        topLeft = Offset(leftCx - rx, cy - ry),
        size = Size(rx * 2f, ry * 2f),
        style = Stroke(width = 2.5f)
    )

    // Concentric growth rings inside cross-section
    listOf(0.65f, 0.34f).forEach { scale ->
        drawOval(
            color = ringColor,
            topLeft = Offset(leftCx - rx * scale, cy - ry * scale),
            size = Size(rx * 2f * scale, ry * 2f * scale),
            style = Stroke(width = 1.6f)
        )
    }
    drawCircle(
        color = brassGuide,
        radius = rx * 0.14f,
        center = Offset(leftCx, cy)
    )

    // Brass girth tape wrapping around middle of the log
    val midX = (leftCx + rightCx) * 0.52f
    val girthPath = Path().apply {
        moveTo(midX, cy - ry - 3f)
        cubicTo(
            midX + rx * 1.15f, cy - ry,
            midX + rx * 1.15f, cy + ry,
            midX, cy + ry + 3f
        )
    }
    drawPath(
        path = girthPath,
        color = brassGuide,
        style = Stroke(width = 4.2f, cap = StrokeCap.Round)
    )
    // Back dashed arc of girth tape
    val girthBackPath = Path().apply {
        moveTo(midX, cy - ry - 3f)
        cubicTo(
            midX - rx * 0.85f, cy - ry,
            midX - rx * 0.85f, cy + ry,
            midX, cy + ry + 3f
        )
    }
    drawPath(
        path = girthBackPath,
        color = brassGuide.copy(alpha = 0.5f),
        style = Stroke(
            width = 2.2f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
    )

    // Top horizontal length dimension line
    val dimY = cy - ry - h * 0.16f
    drawLine(
        color = brassGuide,
        start = Offset(leftCx, dimY),
        end = Offset(rightCx, dimY),
        strokeWidth = 2.2f,
        cap = StrokeCap.Round
    )
    // End ticks for length
    drawLine(
        color = brassGuide,
        start = Offset(leftCx, dimY - 5f),
        end = Offset(leftCx, dimY + 5f),
        strokeWidth = 2.4f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = brassGuide,
        start = Offset(rightCx, dimY - 5f),
        end = Offset(rightCx, dimY + 5f),
        strokeWidth = 2.4f,
        cap = StrokeCap.Round
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSawnTimberDiagram(
    w: Float,
    h: Float
) {
    val creamLine = HeroCreamText.copy(alpha = 0.90f)
    val topFill = HeroCreamText.copy(alpha = 0.24f)
    val frontFill = HeroCreamText.copy(alpha = 0.12f)
    val sideFill = HeroCreamText.copy(alpha = 0.30f)
    val brassGuide = HeroBrassAccent

    // Isometric plank coordinates
    val pTopLeft = Offset(w * 0.14f, h * 0.44f)
    val pTopRight = Offset(w * 0.68f, h * 0.24f)
    val pBackRight = Offset(w * 0.88f, h * 0.36f)
    val pMidFront = Offset(w * 0.34f, h * 0.56f)

    val thicknessDy = h * 0.20f
    val pBotLeft = Offset(pTopLeft.x, pTopLeft.y + thicknessDy)
    val pBotFront = Offset(pMidFront.x, pMidFront.y + thicknessDy)
    val pBotRight = Offset(pBackRight.x, pBackRight.y + thicknessDy)

    // Top face
    val topFace = Path().apply {
        moveTo(pTopLeft.x, pTopLeft.y)
        lineTo(pTopRight.x, pTopRight.y)
        lineTo(pBackRight.x, pBackRight.y)
        lineTo(pMidFront.x, pMidFront.y)
        close()
    }
    drawPath(topFace, color = topFill)

    // Left-front end face (Width × Thickness)
    val endFace = Path().apply {
        moveTo(pTopLeft.x, pTopLeft.y)
        lineTo(pMidFront.x, pMidFront.y)
        lineTo(pBotFront.x, pBotFront.y)
        lineTo(pBotLeft.x, pBotLeft.y)
        close()
    }
    drawPath(endFace, color = sideFill)

    // Long right-front face (Length × Thickness)
    val sideFace = Path().apply {
        moveTo(pMidFront.x, pMidFront.y)
        lineTo(pBackRight.x, pBackRight.y)
        lineTo(pBotRight.x, pBotRight.y)
        lineTo(pBotFront.x, pBotFront.y)
        close()
    }
    drawPath(sideFace, color = frontFill)

    // Clean edges
    val stroke = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawPath(topFace, color = creamLine, style = stroke)
    drawPath(endFace, color = creamLine, style = stroke)
    drawPath(sideFace, color = creamLine, style = stroke)

    // Subtle grain line on top face
    drawLine(
        color = Color.White.copy(alpha = 0.26f),
        start = Offset(
            (pTopLeft.x + pMidFront.x) / 2f,
            (pTopLeft.y + pMidFront.y) / 2f
        ),
        end = Offset(
            (pTopRight.x + pBackRight.x) / 2f,
            (pTopRight.y + pBackRight.y) / 2f
        ),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round
    )

    // Brass Length guide along top edge
    drawLine(
        color = brassGuide,
        start = Offset(pTopLeft.x - 4f, pTopLeft.y - 11f),
        end = Offset(pTopRight.x - 4f, pTopRight.y - 11f),
        strokeWidth = 2.4f,
        cap = StrokeCap.Round
    )

    // Brass Thickness guide on front vertical corner
    drawLine(
        color = brassGuide,
        start = Offset(pMidFront.x, pMidFront.y),
        end = Offset(pBotFront.x, pBotFront.y),
        strokeWidth = 3.4f,
        cap = StrokeCap.Round
    )

    // Brass Width guide along front-left bottom edge
    drawLine(
        color = brassGuide.copy(alpha = 0.85f),
        start = Offset(pBotLeft.x - 3f, pBotLeft.y + 8f),
        end = Offset(pBotFront.x - 3f, pBotFront.y + 8f),
        strokeWidth = 2.4f,
        cap = StrokeCap.Round
    )
}
