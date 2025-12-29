package com.chicken.jelly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chicken.jelly.R
import com.chicken.jelly.data.GameRepository
import com.chicken.jelly.model.GameItem
import com.chicken.jelly.model.Upgrade
import com.chicken.jelly.sound.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private const val LANES = 3

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
) : ViewModel() {

    data class UiState(
        val eggs: Int = 0,
        val score: Int = 0,
        val playerLane: Int = 1,
        val items: List<GameItem> = emptyList(),
        val isPaused: Boolean = false,
        val showTutorial: Boolean = true,
        val showResult: Boolean = false,
        val isWin: Boolean = false,
        val soundEnabled: Boolean = true,
        val musicEnabled: Boolean = true,
        val wheelLevel: Int = 1,
        val turbineLevel: Int = 1,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private var loopJob: Job? = null

    val wheels = listOf(
        Upgrade(1, "Roadster", 0, R.drawable.wheel_1),
        Upgrade(2, "Gripper", 30, R.drawable.wheel_2),
        Upgrade(3, "TurboGrip", 60, R.drawable.wheel_3),
        Upgrade(4, "FeatherSpin", 90, R.drawable.wheel_4),
    )

    val turbines = listOf(
        Upgrade(1, "Breeze", 0, R.drawable.turbine_1),
        Upgrade(2, "Draft", 30, R.drawable.turbine_2),
        Upgrade(3, "Gust", 60, R.drawable.turbine_3),
        Upgrade(4, "Cyclone", 90, R.drawable.turbine_4),
    )

    init {
        observeRepository()
    }

    private fun observeRepository() {
        viewModelScope.launch {
            combine(
                repository.eggsBalance,
                repository.soundEnabled,
                repository.musicEnabled,
                repository.selectedWheel,
                repository.selectedTurbine
            ) { eggs, sound, music, wheel, turbine ->
                _uiState.value.copy(
                    eggs = eggs,
                    soundEnabled = sound,
                    musicEnabled = music,
                    wheelLevel = wheel,
                    turbineLevel = turbine
                )
            }.collect { newState -> _uiState.value = newState }
        }
    }

    fun observeSound(soundManager: SoundManager) {
        viewModelScope.launch {
            uiState.collect { state ->
                soundManager.updateSoundEnabled(state.soundEnabled)
                soundManager.updateMusicEnabled(state.musicEnabled)
            }
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { repository.updateSound(enabled) }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch { repository.updateMusic(enabled) }
    }

    fun moveLeft() {
        _uiState.value = _uiState.value.copy(playerLane = maxOf(0, _uiState.value.playerLane - 1))
    }

    fun moveRight() {
        _uiState.value = _uiState.value.copy(playerLane = minOf(LANES - 1, _uiState.value.playerLane + 1))
    }

    fun startRun(soundManager: SoundManager) {
        _uiState.value = _uiState.value.copy(
            score = 0,
            showResult = false,
            isWin = false,
            showTutorial = false,
            items = emptyList(),
            isPaused = false
        )
        loopJob?.cancel()
        loopJob = viewModelScope.launch { loop(soundManager) }
    }

    private suspend fun loop(soundManager: SoundManager) {
        var counter = 0
        while (true) {
            if (_uiState.value.isPaused) {
                delay(200)
                continue
            }
            spawnItem(counter)
            moveItems(soundManager)
            counter++
            delay(300)
        }
    }

    private fun spawnItem(counter: Int) {
        if (counter % 2 != 0) return
        val reward = counter % 6 != 0
        val lane = counter % LANES
        val speed = 0.12f + 0.02f * (_uiState.value.turbineLevel - 1)
        val newItem = GameItem(counter, lane, reward, speed)
        _uiState.value = _uiState.value.copy(items = _uiState.value.items + newItem)
    }

    private fun moveItems(soundManager: SoundManager) {
        val updated = mutableListOf<GameItem>()
        var score = _uiState.value.score
        var eggs = _uiState.value.eggs
        var crashed = false

        _uiState.value.items.forEach { item ->
            val progressed = item.copy(speed = item.speed + 0.05f)
            if (progressed.speed >= 1f) {
                if (progressed.lane == _uiState.value.playerLane) {
                    if (progressed.isReward) {
                        eggs += 3
                        score += 5
                        soundManager.playEffect(R.raw.sfx_egg)
                    } else {
                        crashed = true
                        soundManager.playEffect(R.raw.sfx_crash)
                    }
                }
            } else {
                updated.add(progressed)
            }
        }

        _uiState.value = _uiState.value.copy(items = updated, score = score)
        if (eggs != _uiState.value.eggs) {
            viewModelScope.launch { repository.updateEggs(eggs) }
        }
        if (crashed) {
            showResult(false)
        } else if (score >= 50) {
            showResult(true)
        }
    }

    fun pauseGame() {
        _uiState.value = _uiState.value.copy(isPaused = true)
    }

    fun resumeGame() {
        _uiState.value = _uiState.value.copy(isPaused = false)
    }

    fun showResult(win: Boolean) {
        _uiState.value = _uiState.value.copy(isPaused = true, showResult = true, isWin = win)
        loopJob?.cancel()
    }

    fun selectWheel(upgrade: Upgrade) {
        viewModelScope.launch {
            if (upgrade.price <= _uiState.value.eggs) {
                repository.updateEggs(_uiState.value.eggs - upgrade.price)
                repository.selectWheel(upgrade.id)
            }
        }
    }

    fun selectTurbine(upgrade: Upgrade) {
        viewModelScope.launch {
            if (upgrade.price <= _uiState.value.eggs) {
                repository.updateEggs(_uiState.value.eggs - upgrade.price)
                repository.selectTurbine(upgrade.id)
            }
        }
    }
}
