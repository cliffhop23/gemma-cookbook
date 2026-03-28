package com.ruview.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruview.android.ui.theme.CardBorder
import com.ruview.android.ui.theme.TextSecondary

@Composable
fun GaugeArc(
    value: Float,
    minValue: Float,
    maxValue: Float,
    label: String,
    unit: String,
    accentColor: Color,
    size: Dp = 160.dp,
    modifier: Modifier = Modifier
) {
    val progress = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    val sweepAngle = progress * 240f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size)
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val stroke = 12.dp.toPx()
                val inset = stroke / 2f

                // Background arc
                drawArc(
                    color = CardBorder,
                    startAngle = 150f,
                    sweepAngle = 240f,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(
                        this.size.width - stroke,
                        this.size.height - stroke
                    )
                )

                // Value arc
                if (sweepAngle > 0f) {
                    drawArc(
                        color = accentColor,
                        startAngle = 150f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                        topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                        size = androidx.compose.ui.geometry.Size(
                            this.size.width - stroke,
                            this.size.height - stroke
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.0f", value),
                    color = accentColor,
                    fontSize = (size.value * 0.22f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = unit,
                    color = TextSecondary,
                    fontSize = (size.value * 0.1f).sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
