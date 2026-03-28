package com.ruview.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ruview.android.data.models.COCO_SKELETON_CONNECTIONS
import com.ruview.android.data.models.Keypoint
import com.ruview.android.data.models.Person
import com.ruview.android.ui.theme.Background
import com.ruview.android.ui.theme.BoneLine
import com.ruview.android.ui.theme.KeepointHead
import com.ruview.android.ui.theme.KeypointLower
import com.ruview.android.ui.theme.KeypointUpper

@Composable
fun SkeletonCanvas(
    persons: List<Person>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        persons.forEach { person ->
            drawSkeleton(person.keypoints, person.confidence)
        }
    }
}

private fun DrawScope.drawSkeleton(keypoints: List<Keypoint>, confidence: Float) {
    if (keypoints.size < 17) return

    val alpha = (confidence * 0.85f + 0.15f).coerceIn(0f, 1f)
    val w = size.width
    val h = size.height

    fun kp(idx: Int): Offset? {
        val kp = keypoints.getOrNull(idx) ?: return null
        return if (kp.confidence > 0.3f) Offset(kp.x * w, kp.y * h) else null
    }

    // Draw bones
    COCO_SKELETON_CONNECTIONS.forEach { (a, b) ->
        val from = kp(a) ?: return@forEach
        val to = kp(b) ?: return@forEach
        drawLine(
            color = BoneLine.copy(alpha = alpha * 0.75f),
            start = from,
            end = to,
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }

    // Draw keypoints with color by body part
    keypoints.forEachIndexed { idx, kp ->
        if (kp.confidence > 0.3f) {
            val pos = Offset(kp.x * w, kp.y * h)
            val color = keypointColor(idx)
            val radius = if (idx in 0..4) 7f else 5f

            // Glow effect
            drawCircle(
                color = color.copy(alpha = alpha * 0.25f),
                radius = radius * 2.5f,
                center = pos
            )
            // Dot
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = pos
            )
        }
    }
}

private fun keypointColor(idx: Int): Color = when (idx) {
    in 0..4  -> KeepointHead    // Head keypoints
    in 5..10 -> KeypointUpper   // Upper body
    else     -> KeypointLower   // Lower body
}
