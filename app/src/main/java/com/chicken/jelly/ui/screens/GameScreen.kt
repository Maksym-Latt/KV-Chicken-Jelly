package com.chicken.jelly.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LocalLifecycleOwner
import com.chicken.jelly.R
import com.chicken.jelly.config.GameConfig
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
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        soundManager.playGameMusic()
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        viewModel.pauseGame()
                        soundManager.pauseForLifecycle()
                    }

                    Lifecycle.Event.ON_RESUME -> soundManager.resumeAfterLifecycle()
                    else -> Unit
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler {
        if (state.showResult) {
            onExit()
        } else {
            viewModel.pauseGame()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val backgroundRes =
            when (state.currentLevel) {
                1 -> R.drawable.bg_game_1
                2 -> R.drawable.bg_game_2
                else -> R.drawable.bg_game_3
            }

        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EggBadge(value = state.eggs)
                // Timer display
                OutlineText(
                    text = "${state.levelTimeRemaining}s",
                    fontFamily = font,
                    fontSize = 32,
                    color = if (state.levelTimeRemaining <= 5) Color.Red else Color.White,
                    outlineThickness = 3.dp
                )

                RoundIconButton(icon = R.drawable.ic_pause, onClick = viewModel::pauseGame)
            }
            Box(modifier = Modifier.weight(1f)) {
                LaneView(state.playerLane)
                ItemLayer(state)
                PlayerCar(state = state)
                if (state.showTutorial && !state.isPaused) {
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
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Left",
                    modifier =
                        Modifier
                            .padding(10.dp)
                            .size(100.dp)
                            .clickableNoRipple {
                                viewModel.moveLeft()
                            }
                )
                Image(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Right",
                    modifier =
                        Modifier
                            .padding(10.dp)
                            .size(100.dp)
                            .graphicsLayer { scaleX = -1f }
                            .clickableNoRipple { viewModel.moveRight() }
                )
            }
        }
        if (state.isTransitioning) {
            LevelTransitionOverlay(level = state.currentLevel, font = font)
        }
        if (state.isPaused && !state.showResult) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                PauseOverlay(
                    fontFamily = font,
                    soundEnabled = state.soundEnabled,
                    musicEnabled = state.musicEnabled,
                    onSoundToggle = viewModel::toggleSound,
                    onMusicToggle = viewModel::toggleMusic,
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
        repeat(GameConfig.LANE_COUNT) { index ->
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxSize()) {
                if (index == playerLane) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = GameConfig.laneIndicatorBottomPadding)
                                .background(Color(0x33FFFFFF))
                                .height(GameConfig.laneIndicatorHeight)
                                .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.ItemLayer(state: GameViewModel.UiState) {
    state.items.forEach { item ->
        // Skip rendering if item was hit (collected eggs or crashed obstacles)
        if (item.isHit) return@forEach

        // Calculate perspective-based position and scale
        val progress = item.speed
        val horizontalOffset = GameConfig.getLaneOffset(item.lane, progress)
        val verticalOffset = GameConfig.getVerticalOffset(progress)
        val itemSize = GameConfig.getItemSize(progress)

        // Use graphicsLayer for better performance
        val scale = itemSize.value / 64f // Normalize to base size

        Image(
            painter =
                painterResource(
                    id = if (item.isReward) R.drawable.egg else R.drawable.ic_stack
                ),
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = horizontalOffset, y = verticalOffset)
                    .size(64.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun LevelTransitionOverlay(level: Int, font: FontFamily) {
    val scale by
    animateFloatAsState(
        targetValue = 0.5f,
        animationSpec = tween(durationMillis = 2000),
        label = "transitionScale"
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        contentAlignment = Alignment.Center
    ) {
        OutlineText(
            text = "Level $level",
            fontFamily = font,
            fontSize = 48,
            color = Color.White,
            outlineThickness = 4.dp
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
            modifier = Modifier.padding(16.dp)
        ) {
            OutlineText(
                text = "Tap Start to drift!",
                fontFamily = font,
                fontSize = 28,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            WideButton(text = "Start", onClick = onStart)
        }
    }
}

@Composable
private fun BoxScope.PlayerCar(state: GameViewModel.UiState) {
    val playerLane = state.playerLane
    val isTransitioning = state.isTransitioning

    val targetOffset =
        if (playerLane in 0 until GameConfig.LANE_COUNT) {
            GameConfig.playerCarLaneOffsets[playerLane]
        } else {
            0.dp
        }

    // Smooth animation for lane changes
    val horizontalOffset by
    animateDpAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 200),
        label = "playerCarOffset"
    )

    // Shrink car during transitions
    val carScale by
    animateFloatAsState(
        targetValue = if (isTransitioning) 0.7f else 1.0f,
        animationSpec = tween(1000),
        label = "carScale"
    )

    // Drive forward (upwards) during transitions
    val carVerticalOffset by
    animateDpAsState(
        targetValue =
            if (isTransitioning) (-160).dp
            else (-GameConfig.playerCarBottomPadding),
        animationSpec = tween(2500),
        label = "carVerticalOffset"
    )

    Image(
        painter = painterResource(id = R.drawable.player_game),
        contentDescription = null,
        modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .offset(x = horizontalOffset, y = carVerticalOffset)
                .height(GameConfig.playerCarHeight)
                .graphicsLayer {
                    scaleX = carScale
                    scaleY = carScale
                },
        contentScale = ContentScale.Crop
    )
}

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
