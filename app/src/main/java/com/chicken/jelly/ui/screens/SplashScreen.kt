package com.chicken.jelly.ui.screens

import android.R.attr.fontFamily
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chicken.jelly.R
import com.chicken.jelly.ui.components.OutlineText
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }

    val transition = rememberInfiniteTransition(label = "loading")

    // ---------- dots animation ----------
    val dotPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotPhase"
    )

    // ---------- slow smooth bobbing ----------
    val bobPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 7000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "bobPhase"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_loading),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(modifier = Modifier.weight(6f))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                val bobOffset =
                    kotlin.math.sin(bobPhase * Math.PI * 2).toFloat() * 6f

                OutlineText(
                    text = buildLoadingText(dotPhase),
                    modifier = Modifier.offset(y = bobOffset.dp),
                    color = Color(0xff5be16d),
                    outline = Color(0xff0e1800),
                    fontSize = 60,
                    outlineThickness = 3.dp,
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun buildLoadingText(phase: Float): String {
    val dots = when (phase.toInt() % 4) {
        0 -> ""
        1 -> "."
        2 -> ".."
        else -> "..."
    }
    return "LOADING$dots"
}