package com.chicken.jelly.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chicken.jelly.R

@Composable
fun PauseOverlay(
    fontFamily: FontFamily,
    onResume: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        BaseOverlay {
            OutlineText(
                text = "Pause",
                fontFamily = fontFamily,
                fontSize = 82,
                outlineThickness = 4.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SwitchColumn(
                    label = "Sound",
                    checked = true,
                    onCheckedChange = {},
                    fontFamily = fontFamily
                )
                SwitchColumn(
                    label = "Music",
                    checked = true,
                    onCheckedChange = {},
                    fontFamily = fontFamily
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            WideButton(
                text = "Resume",
                onClick = onResume,
                textSize = 44
            )

            Spacer(modifier = Modifier.height(14.dp))

            WideButton(
                text = "Exit",
                onClick = onExit,
                red = true,
                textSize = 44
            )
        }
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
        WideButton(text = "Try again", onClick = onRetry)
        Spacer(modifier = Modifier.height(8.dp))
        WideButton(text = "Upgrade", onClick = onUpgrade)
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        BaseOverlay {
            OutlineText(
                text = "Settings",
                fontFamily = fontFamily,
                fontSize = 82,
                outlineThickness = 4.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SwitchColumn(
                    label = "Sound",
                    checked = soundEnabled,
                    onCheckedChange = onSoundToggle,
                    fontFamily = fontFamily
                )
                SwitchColumn(
                    label = "Music",
                    checked = musicEnabled,
                    onCheckedChange = onMusicToggle,
                    fontFamily = fontFamily
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SwitchColumn(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    fontFamily: FontFamily,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlineText(
            text = label,
            fontFamily = fontFamily,
            fontSize = 46,
            outlineThickness = 3.dp
        )
        Spacer(modifier = Modifier.height(6.dp))
        GreenPillSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun GreenPillSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 66.dp,
    borderWidth: Dp = 10.dp,
    innerPadding: Dp = 10.dp,
    borderColor: Color = Color(0xFF31B93D),
    trackColor: Color = Color.White,
    thumbColor: Color = Color(0xFF0F3D13),
) {
    val shape = RoundedCornerShape(height / 2)

    val thumbSize = height - (innerPadding * 2)
    val targetX = if (checked) width - innerPadding - thumbSize else innerPadding
    val thumbX = animateDpAsState(
        targetValue = targetX,
        animationSpec = tween(durationMillis = 180),
        label = "thumbX"
    ).value

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .border(borderWidth, borderColor, shape)
            .background(trackColor, shape)
            .toggleable(
                value = checked,
                role = Role.Switch,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onValueChange = onCheckedChange
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = innerPadding)
                .offset(x = thumbX)
                .size(thumbSize)
                .background(thumbColor, RoundedCornerShape(thumbSize / 2))
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
            .widthIn(min = 280.dp, max = 340.dp)
            .background(
                color = Color(0xff4cae6b),
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                width = 8.dp,
                color = Color(0xFF3C9116),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            content()
        }
    }
}