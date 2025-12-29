package com.chicken.jelly.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.chicken.jelly.R
import com.chicken.jelly.sound.SoundManager
import com.chicken.jelly.ui.components.EggBadge
import com.chicken.jelly.ui.components.OutlineText
import com.chicken.jelly.ui.components.RoundIconButton
import com.chicken.jelly.ui.components.WideButton
import com.chicken.jelly.viewmodel.GameViewModel

@Composable
fun MenuScreen(
    viewModel: GameViewModel,
    onPlay: () -> Unit,
    onGarage: () -> Unit,
    onSettings: () -> Unit,
    soundManager: SoundManager,
) {
    val state = viewModel.uiState.collectAsState().value
    val font = MaterialTheme.typography.bodyLarge.fontFamily ?: FontFamily.Default

    LaunchedEffect(Unit) {
        soundManager.playMenuMusic()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_menu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            OutlineText(text = "Chicken Jelly", fontFamily = font, fontSize = 40)
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(id = R.drawable.player_game),
                contentDescription = null,
                modifier = Modifier.height(220.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            WideButton(text = "Play", onClick = onPlay, fontFamily = font)
            Spacer(modifier = Modifier.height(12.dp))
            WideButton(text = "Garage", onClick = onGarage, fontFamily = font)
        }
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
            RoundIconButton(icon = R.drawable.ic_settings, onClick = onSettings)
        }
        Box(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            EggBadge(value = state.eggs, fontFamily = font)
        }
    }
}
