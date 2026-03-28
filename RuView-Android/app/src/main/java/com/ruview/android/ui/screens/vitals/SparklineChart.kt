package com.ruview.android.ui.screens.vitals

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ruview.android.ui.theme.CardBackground
import com.ruview.android.ui.theme.CardBorder

@Composable
fun SparklineChart(
    data: List<Float>,
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    Box(
        modifier = modifier
            .background(CardBackground)
            .border(1.dp, CardBorder)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val min = data.min()
            val max = data.max()
            val range = (max - min).coerceAtLeast(1f)
            val w = size.width
            val h = size.height
            val padding = 8f

            fun xAt(idx: Int) = padding + (idx.toFloat() / (data.size - 1).coerceAtLeast(1)) * (w - padding * 2)
            fun yAt(v: Float) = h - padding - ((v - min) / range) * (h - padding * 2)

            // Fill path
            val path = Path()
            path.moveTo(xAt(0), h)
            data.forEachIndexed { i, v ->
                if (i == 0) path.lineTo(xAt(i), yAt(v))
                else path.lineTo(xAt(i), yAt(v))
            }
            path.lineTo(xAt(data.size - 1), h)
            path.close()

            drawPath(path, color = color.copy(alpha = 0.1f))

            // Line
            for (i in 1 until data.size) {
                drawLine(
                    color = color.copy(alpha = 0.8f),
                    start = Offset(xAt(i - 1), yAt(data[i - 1])),
                    end = Offset(xAt(i), yAt(data[i])),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }

            // Dot at latest value
            if (data.isNotEmpty()) {
                drawCircle(
                    color = color,
                    radius = 4f,
                    center = Offset(xAt(data.size - 1), yAt(data.last()))
                )
            }
        }
    }
}
