package com.chicken.jelly.ui.components

import android.R.attr.fontFamily
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chicken.jelly.R

@Composable
fun WideButton(
    text: String? = null,
    icon: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    red: Boolean = false,
    textSize: Int = 46,
) {
    val background = if (red) R.drawable.btn_red_bg else R.drawable.btn_green_bg

    Box(
        modifier = modifier.clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = background),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            if (icon != null) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null
                )
            }

            if (icon != null && text != null) {
                Spacer(modifier = Modifier.width(12.dp))
            }

            if (text != null) {
                OutlineText(
                    text = text,
                    color = Color.White,
                    outline = Color.Black,
                    fontWeight = FontWeight.Bold,
                    outlineThickness = 2.dp,
                    fontSize = textSize
                )
            }
        }
    }
}


@Composable
fun RoundIconButton(
    icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.btn_round_bg),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
    }
}


