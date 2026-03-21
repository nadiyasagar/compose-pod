package com.example.composepod.demo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment

fun Modifier.glassEffect(
    cornerRadius: Float = 24f,
    blurRadius: Float = 20f,
    backgroundColor: Color = Color.White.copy(alpha = 0.05f),
    strokeColor: Color = Color.White.copy(alpha = 0.2f)
) = composed {
    this
        .clip(RoundedCornerShape(cornerRadius))
        .background(backgroundColor)
        .border(
            width = 1.dp,
            color = strokeColor,
            shape = RoundedCornerShape(cornerRadius)
        )
}

@Composable
fun VibrantBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F2B)) // Dark sleek background from reference
    ) {
        // Glowing Orb 1 (Top right - vibrant pinkish orange)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.45f)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .blur(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE56A77).copy(alpha = 0.8f),
                            Color(0xFFE56A77).copy(alpha = 0.0f)
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Glowing Orb 2 (Center left - soft purple/blue)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.4f)
                .align(Alignment.CenterStart)
                .offset(x = (-80).dp, y = 50.dp)
                .blur(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF9172B5).copy(alpha = 0.5f),
                            Color(0xFF9172B5).copy(alpha = 0.0f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        content()
    }
}
