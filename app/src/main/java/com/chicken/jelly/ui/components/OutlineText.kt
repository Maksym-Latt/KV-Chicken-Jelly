package com.chicken.jelly.ui.components

import android.R.attr.maxLines
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun OutlineText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    outline: Color = Color.Black,
    outlineThickness: Dp = 1.dp,
    fontSize: Int = 28,
    letterSpacing: TextUnit = 2.sp,
    fontFamily: FontFamily = MaterialTheme.typography.bodyLarge.fontFamily ?: FontFamily.Default,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Center,
) {
    val density = LocalDensity.current
    val strokePx = with(density) { outlineThickness.toPx() }

    val baseStyle = TextStyle(
        fontSize = fontSize.sp,
        letterSpacing = letterSpacing,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        textAlign = textAlign,
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = baseStyle.copy(
                color = outline,
                drawStyle = Stroke(width = strokePx)
            ),
        )

        Text(
            text = text,
            style = baseStyle.copy(color = color),
        )
    }
}