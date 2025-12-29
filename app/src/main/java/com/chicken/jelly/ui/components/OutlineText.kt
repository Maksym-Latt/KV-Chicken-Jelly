package com.chicken.jelly.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
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

@Composable
fun OutlineText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    outline: Color = Color.Black,
    fontSize: Int = 28,
    fontFamily: FontFamily,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Center,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val style = TextStyle(
            color = outline,
            fontSize = fontSize.sp,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            textAlign = textAlign,
        )
        Text(text = text, style = style, modifier = Modifier.offset(1.dp, 1.dp))
        Text(text = text, style = style, modifier = Modifier.offset((-1).dp, (-1).dp))
        Text(
            text = text,
            style = style.copy(color = color),
        )
    }
}
