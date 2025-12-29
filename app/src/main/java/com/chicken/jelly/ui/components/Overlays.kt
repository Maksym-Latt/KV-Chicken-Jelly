package com.chicken.jelly.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
fun PauseOverlay(
    fontFamily: FontFamily,
    onResume: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseOverlay(modifier = modifier) {
        OutlineText(text = "Pause", fontFamily = fontFamily, fontSize = 32)
        Spacer(modifier = Modifier.height(16.dp))
        WideButton(text = "Resume", onClick = onResume, fontFamily = fontFamily)
        Spacer(modifier = Modifier.height(8.dp))
        WideButton(text = "Exit", onClick = onExit, fontFamily = fontFamily, red = true)
    }
}

@Composable
fun ResultOverlay(
    fontFamily: FontFamily,
    eggs: Int,
    win: Boolean,
    onRetry: () -> Unit,
    onUpgrade: () -> Unit,
) {
    BaseOverlay {
        Image(
            painter = painterResource(id = if (win) R.drawable.player_game else R.drawable.player_garage),
            contentDescription = null,
            modifier = Modifier.height(180.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlineText(
            text = if (win) "Egg-cellent!" else "Yolk's on you!",
            fontFamily = fontFamily,
            fontSize = 30,
            color = Color(0xFF7EF35A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        EggBadge(value = eggs)
        Spacer(modifier = Modifier.height(16.dp))
        WideButton(text = "Try again", onClick = onRetry, fontFamily = fontFamily)
        Spacer(modifier = Modifier.height(8.dp))
        WideButton(text = "Upgrade", onClick = onUpgrade, fontFamily = fontFamily)
    }
}

@Composable
fun SettingsOverlay(
    fontFamily: FontFamily,
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    onMusicToggle: (Boolean) -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
) {
    BaseOverlay {
        OutlineText(text = "Settings", fontFamily = fontFamily, fontSize = 32)
        Spacer(modifier = Modifier.height(16.dp))
        SwitchRow(
            label = "Sound",
            checked = soundEnabled,
            onCheckedChange = onSoundToggle,
            fontFamily = fontFamily
        )
        SwitchRow(
            label = "Music",
            checked = musicEnabled,
            onCheckedChange = onMusicToggle,
            fontFamily = fontFamily
        )
        Spacer(modifier = Modifier.height(12.dp))
        WideButton(text = "Privacy", onClick = onPrivacy, fontFamily = fontFamily)
        Spacer(modifier = Modifier.height(8.dp))
        WideButton(text = "Terms", onClick = onTerms, fontFamily = fontFamily)
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    fontFamily: FontFamily,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlineText(text = label, fontFamily = fontFamily, fontSize = 22)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF6CE650)
            )
        )
    }
}

@Composable
private fun BaseOverlay(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xDD54B63F), RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            content()
        }
    }
}
