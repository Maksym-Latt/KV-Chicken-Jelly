package com.chicken.jelly.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.chicken.jelly.R
import com.chicken.jelly.sound.SoundManager
import com.chicken.jelly.ui.components.EggBadge
import com.chicken.jelly.ui.components.OutlineText
import com.chicken.jelly.ui.components.PauseOverlay
import com.chicken.jelly.ui.components.ResultOverlay
import com.chicken.jelly.ui.components.RoundIconButton
import com.chicken.jelly.ui.components.WideButton
import com.chicken.jelly.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel, onExit: () -> Unit, soundManager: SoundManager) {
    val state by viewModel.uiState.collectAsState()
    val font = MaterialTheme.typography.bodyLarge.fontFamily ?: FontFamily.Default

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_game),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(icon = R.drawable.ic_home, onClick = onExit)
                EggBadge(value = state.eggs, fontFamily = font)
                RoundIconButton(icon = R.drawable.ic_pause, onClick = viewModel::pauseGame)
            }
            Box(modifier = Modifier.weight(1f)) {
                LaneView(state.playerLane)
                ItemLayer(state)
                Image(
                    painter = painterResource(id = R.drawable.player_game),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(200.dp)
                )
                if (state.showTutorial) {
                    TutorialOverlay(font = font, onStart = { viewModel.startRun(soundManager) })
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow),
                    contentDescription = "Left",
                    modifier = Modifier
                        .size(72.dp)
                        .clickableNoRipple { viewModel.moveLeft() }
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow),
                    contentDescription = "Right",
                    modifier = Modifier
                        .size(72.dp)
                        .clickableNoRipple { viewModel.moveRight() }
                )
            }
        }
        if (state.isPaused && !state.showResult && !state.showTutorial) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                PauseOverlay(
                    fontFamily = font,
                    onResume = viewModel::resumeGame,
                    onExit = onExit
                )
            }
        }
        if (state.showResult) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                ResultOverlay(
                    fontFamily = font,
                    eggs = state.eggs,
                    win = state.isWin,
                    onRetry = { viewModel.startRun(soundManager) },
                    onUpgrade = onExit
                )
            }
        }
    }
}

@Composable
private fun LaneView(playerLane: Int) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                if (index == playerLane) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 140.dp)
                            .background(Color(0x33FFFFFF))
                            .height(6.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemLayer(state: GameViewModel.UiState) {
    state.items.forEach { item ->
        val laneOffset = when (item.lane) {
            0 -> -80.dp
            1 -> 0.dp
            else -> 80.dp
        }
        val vertical = (item.speed * 320).dp
        Image(
            painter = painterResource(id = if (item.isReward) R.drawable.egg else R.drawable.ic_garage),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = vertical)
                .offset(x = laneOffset)
                .size(64.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun TutorialOverlay(font: FontFamily, onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(16.dp)
        ) {
            OutlineText(
                text = "Tap Start to drift!",
                fontFamily = font,
                fontSize = 28,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            WideButton(text = "Start", onClick = onStart, fontFamily = font)
        }
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        clickable(indication = null, interactionSource = MutableInteractionSource()) { onClick() }
    )
