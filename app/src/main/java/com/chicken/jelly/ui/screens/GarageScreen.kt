package com.chicken.jelly.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.chicken.jelly.model.Upgrade
import com.chicken.jelly.ui.components.EggBadge
import com.chicken.jelly.ui.components.OutlineText
import com.chicken.jelly.ui.components.RoundIconButton
import com.chicken.jelly.ui.components.WideButton
import com.chicken.jelly.viewmodel.GameViewModel

@Composable
fun GarageScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val state = viewModel.uiState.collectAsState().value
    val font = MaterialTheme.typography.bodyLarge.fontFamily ?: FontFamily.Default

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_garage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(icon = R.drawable.ic_home, onClick = onBack)
                EggBadge(value = state.eggs, fontFamily = font)
            }
            Image(
                painter = painterResource(id = R.drawable.player_garage),
                contentDescription = null,
                modifier = Modifier.height(200.dp)
            )
            UpgradeSection(
                title = "Wheels",
                upgrades = viewModel.wheels,
                selected = state.wheelLevel,
                onSelect = viewModel::selectWheel,
                font = font
            )
            UpgradeSection(
                title = "Turbine",
                upgrades = viewModel.turbines,
                selected = state.turbineLevel,
                onSelect = viewModel::selectTurbine,
                font = font
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun UpgradeSection(
    title: String,
    upgrades: List<Upgrade>,
    selected: Int,
    onSelect: (Upgrade) -> Unit,
    font: FontFamily,
) {
    OutlineText(text = title, fontFamily = font, fontSize = 24)
    Spacer(modifier = Modifier.height(8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(upgrades) { upgrade ->
            UpgradeCard(
                upgrade = upgrade,
                selected = upgrade.id == selected,
                font = font,
                onSelect = { onSelect(upgrade) }
            )
        }
    }
}

@Composable
private fun UpgradeCard(upgrade: Upgrade, selected: Boolean, font: FontFamily, onSelect: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = upgrade.iconRes),
            contentDescription = upgrade.name,
            modifier = Modifier.height(82.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlineText(text = "${upgrade.price}🥚", fontFamily = font, fontSize = 18)
        Spacer(modifier = Modifier.height(4.dp))
        WideButton(
            text = if (selected) "Equipped" else "Equip",
            onClick = onSelect,
            fontFamily = font,
            red = false,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
