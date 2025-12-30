package com.chicken.jelly.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
fun GarageScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    soundManager: SoundManager,
) {
    val state = viewModel.uiState.collectAsState().value
    val font = MaterialTheme.typography.bodyLarge.fontFamily ?: FontFamily.Default

    LaunchedEffect(Unit) { soundManager.playMenuMusic() }

    var tab by rememberSaveable { mutableIntStateOf(0) }

    val items =
        remember(tab, state, viewModel) {
            when (tab) {
                0 ->
                    viewModel.wheels.map {
                        GarageItemUi(
                            id = it.id,
                            iconRes = it.iconRes,
                            isSelected = it.id == state.pendingWheelId,
                            level = it.id,
                            speedModifier = it.speedModifier,
                            price = it.price
                        )
                    }

                else ->
                    viewModel.turbines.map {
                        GarageItemUi(
                            id = it.id,
                            iconRes = it.iconRes,
                            isSelected = it.id == state.pendingTurbineId,
                            level = it.id,
                            speedModifier = it.speedModifier,
                            price = it.price
                        )
                    }
            }
        }

    val isEquipped =
        when (tab) {
            0 -> state.pendingWheelId == state.wheelLevel
            else -> state.pendingTurbineId == state.turbineLevel
        }

    val isOwned =
        when (tab) {
            0 -> state.ownedWheels.contains(state.pendingWheelId)
            else -> state.ownedTurbines.contains(state.pendingTurbineId)
        }

    val selectedItemPrice =
        when (tab) {
            0 -> viewModel.wheels.find { it.id == state.pendingWheelId }?.price ?: 0
            else -> viewModel.turbines.find { it.id == state.pendingTurbineId }?.price ?: 0
        }

    val canAfford = state.eggs >= selectedItemPrice

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_garage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.player_garage),
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.82f)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-26).dp),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(icon = R.drawable.ic_home, onClick = onBack, modifier = Modifier)

                Spacer(modifier = Modifier.weight(1f))

                EggBadge(value = state.eggs, modifier = Modifier.wrapContentWidth())
            }

            Spacer(modifier = Modifier.weight(0.08f))

            GarageTabs(selectedIndex = tab, onSelect = { tab = it }, font = font)

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                GarageGrid(
                    items = items,
                    onSelect = { id ->
                        when (tab) {
                            0 -> viewModel.selectWheelById(id)
                            else -> viewModel.selectTurbineById(id)
                        }
                    },
                    modifier = Modifier.size(170.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                val selectedItem = items.find { it.isSelected }

                Column(
                    modifier = Modifier.widthIn(min = 220.dp, max = 320.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedItem != null) {
                        OutlineText(
                            text = "Level: ${selectedItem.level}",
                            fontFamily = font,
                            fontSize = 20,
                            color = Color.White,
                            outlineThickness = 2.dp
                        )
                        OutlineText(
                            text = "Speed: ${selectedItem.speedModifier}x",
                            fontFamily = font,
                            fontSize = 20,
                            color = Color(0xff5be16d),
                            outlineThickness = 2.dp
                        )
                        OutlineText(
                            text = "Price: ${selectedItem.price}",
                            fontFamily = font,
                            fontSize = 20,
                            color = Color.White,
                            outlineThickness = 2.dp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        WideButton(
                            text =
                                if (isEquipped) "Equipped"
                                else if (isOwned) "Equip"
                                else if (canAfford) "Buy" else "Locked",
                            onClick = {
                                if (!isEquipped && (isOwned || canAfford)) {
                                    when (tab) {
                                        0 -> viewModel.applySelectedWheel()
                                        else -> viewModel.applySelectedTurbine()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            textSize = 34
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GarageTabs(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    font: FontFamily,
) {
    val labels = remember { listOf("Wheels", "Turbine") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.85f), RectangleShape)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEachIndexed { index, title ->
                val active = index == selectedIndex

                Box(
                    modifier =
                        Modifier
                            .clickable { onSelect(index) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlineText(
                        text = title,
                        fontFamily = font,
                        fontSize = 22,
                        outlineThickness = 3.dp,
                        color = if (active) Color(0xff5be16d) else Color.White,
                        outline = Color(0xff0e1800),
                    )
                }
            }
        }

        val target =
            when (selectedIndex) {
                0 -> (-60).dp
                1 -> 60.dp
                else -> 0.dp
            }
        val x by animateDpAsState(targetValue = target, label = "tabIndicator")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp), contentAlignment = Alignment.Center
        ) {
            Box(
                modifier =
                    Modifier
                        .offset(x = x)
                        .width(80.dp)
                        .height(3.dp)
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            RoundedCornerShape(6.dp)
                        )
            )
        }
    }
}

@Composable
private fun GarageGrid(
    items: List<GarageItemUi>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        items(items.take(4)) { item ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                        .clip(shape)
                        .background(Color.White.copy(alpha = 0.92f))
                        .then(
                            if (item.isSelected) {
                                Modifier.border(3.dp, Color(0xFFFFD700), shape)
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onSelect(item.id) }
                        .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                if (item.isSelected) {
                    Box(
                        modifier =
                            Modifier
                                .matchParentSize()
                                .clip(shape)
                                .background(Color(0xFFFFD700).copy(alpha = 0.12f))
                    )
                }
            }
        }
    }
}

private data class GarageItemUi(
    val id: Int,
    val iconRes: Int,
    val isSelected: Boolean,
    val level: Int,
    val speedModifier: Float,
    val price: Int,
)
