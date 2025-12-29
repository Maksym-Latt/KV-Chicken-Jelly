package com.chicken.jelly.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.chicken.jelly.R
import com.chicken.jelly.ui.components.RoundIconButton
import com.chicken.jelly.ui.components.SettingsOverlay
import com.chicken.jelly.viewmodel.GameViewModel

@Composable
fun SettingsScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val state = viewModel.uiState.collectAsState().value
    val font = MaterialTheme.typography.bodyLarge.fontFamily ?: FontFamily.Default

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_menu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        RoundIconButton(
            icon = R.drawable.ic_home,
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .align (Alignment.TopStart))
        Box(modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 16.dp)) {
            SettingsOverlay(
                fontFamily = font,
                soundEnabled = state.soundEnabled,
                musicEnabled = state.musicEnabled,
                onSoundToggle = viewModel::toggleSound,
                onMusicToggle = viewModel::toggleMusic,
                onPrivacy = {},
                onTerms = {}
            )
        }
    }
}
