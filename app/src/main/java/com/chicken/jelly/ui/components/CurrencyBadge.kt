package com.chicken.jelly.ui.components

import android.R.attr.fontFamily
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
fun EggBadge(
    value: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.btn_short_green_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.height(60.dp)
        )

        Row(
            modifier = Modifier
        ) {
            Image(
                painter = painterResource(id = R.drawable.egg),
                contentDescription = null,
                modifier = Modifier.height(45.dp),
                contentScale = ContentScale.Crop
            )
            OutlineText(
                text = value.toString(),
                fontSize = 38,
                color = Color.White,
                outline = Color.Black
            )
        }
    }
}