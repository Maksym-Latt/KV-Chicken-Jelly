package com.chicken.jelly.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.chicken.jelly.R

@Composable
fun EggBadge(value: Int, fontFamily: FontFamily = FontFamily.Default) {
    Box(
        modifier = Modifier
            .background(Color(0xFF62D33E), RoundedCornerShape(32.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.egg),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
            OutlineText(
                text = value.toString(),
                fontFamily = fontFamily,
                fontSize = 20,
                color = Color.White,
                outline = Color.Black
            )
        }
    }
}
