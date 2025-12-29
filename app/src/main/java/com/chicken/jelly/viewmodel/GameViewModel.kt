package com.chicken.jelly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chicken.jelly.R
import com.chicken.jelly.config.GameConfig
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

private val LANES = GameConfig.LANE_COUNT

@HiltViewModel
class GameViewModel
@Inject
constructor(
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
            val pendingWheelId: Int = 1,
            val pendingTurbineId: Int = 1,
            val currentLevel: Int = 1,
            val levelTimeRemaining: Int = GameConfig.LEVEL_DURATION_SECONDS,
            val isTransitioning: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private var loopJob: Job? = null
    private var timerJob: Job? = null

    val wheels =
            listOf(
                    Upgrade(1, "Roadster", 0, R.drawable.wheel_1, 1.2f),
                    Upgrade(2, "Gripper", 30, R.drawable.wheel_2, 1.4f),
                    Upgrade(3, "TurboGrip", 60, R.drawable.wheel_3, 1.6f),
                    Upgrade(4, "FeatherSpin", 90, R.drawable.wheel_4, 1.8f),
            )

    val turbines =
            listOf(
                    Upgrade(1, "Breeze", 0, R.drawable.turbine_1, 1.2f),
                    Upgrade(2, "Draft", 30, R.drawable.turbine_2, 1.4f),
                    Upgrade(3, "Gust", 60, R.drawable.turbine_3, 1.6f),
                    Upgrade(4, "Cyclone", 90, R.drawable.turbine_4, 1.8f),
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
                // Only update pending if we haven't manipulated them locally yet?
                // Or always sync them if the source of truth changes?
                // For simplicity, let's sync pending to active if the active one changes externally
                // (or on first load), but usually we want to keep local pending state if user is
                // editing.
                // However, since repo is single source of truth for persistent state:
                _uiState.value.copy(
                        eggs = eggs,
                        soundEnabled = sound,
                        musicEnabled = music,
                        wheelLevel = wheel,
                        turbineLevel = turbine,
                        // We only sync pending to actual on data arrival IF we assume a fresh start
                        // or if we want to reset. Ideally we check if they are set.
                        // Let's just update them. If user is in middle of selection and db updates,
                        // it might jump.
                        // But DB updates usually come from 'apply'.
                        pendingWheelId =
                                if (_uiState.value.pendingWheelId == _uiState.value.wheelLevel)
                                        wheel
                                else _uiState.value.pendingWheelId,
                        pendingTurbineId =
                                if (_uiState.value.pendingTurbineId == _uiState.value.turbineLevel)
                                        turbine
                                else _uiState.value.pendingTurbineId
                )
            }
                    .collect { newState ->
                        // Ensure if it's the very first load (defaults), we sync pending to loaded
                        // values
                        // This logic above is a bit tricky with the 'if'.
                        // Simplified: Let's trust the VM state for pending, unless we just started.
                        // Actually, let's just update the persistent fields.
                        // We will handle initialization properly in a separate block or accept that
                        // clean start -> pending=1 (default) might be wrong if saved=2.

                        // Better approach for sync:
                        var resultingState =
                                _uiState.value.copy(
                                        eggs = newState.eggs,
                                        soundEnabled = newState.soundEnabled,
                                        musicEnabled = newState.musicEnabled,
                                        wheelLevel = newState.wheelLevel,
                                        turbineLevel = newState.turbineLevel
                                )

                        // If this is the FIRST real update (e.g. going from default 1 to saved 3),
                        // we might want to sync pending.
                        // A simple heuristic: if pendingXXX match the OLD activeXXX, update them to
                        // NEW activeXXX.
                        if (_uiState.value.pendingWheelId == _uiState.value.wheelLevel) {
                            resultingState =
                                    resultingState.copy(pendingWheelId = newState.wheelLevel)
                        }
                        if (_uiState.value.pendingTurbineId == _uiState.value.turbineLevel) {
                            resultingState =
                                    resultingState.copy(pendingTurbineId = newState.turbineLevel)
                        }

                        _uiState.value = resultingState
                    }
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
        _uiState.value =
                _uiState.value.copy(playerLane = minOf(LANES - 1, _uiState.value.playerLane + 1))
    }

    fun startRun(soundManager: SoundManager) {
        _uiState.value =
                _uiState.value.copy(
                        score = 0,
                        showResult = false,
                        isWin = false,
                        showTutorial = false,
                        items = emptyList(),
                        isPaused = false,
                        currentLevel = 1,
                        levelTimeRemaining = GameConfig.LEVEL_DURATION_SECONDS,
                        isTransitioning = false
                )
        loopJob?.cancel()
        timerJob?.cancel()
        loopJob = viewModelScope.launch { loop(soundManager) }
        timerJob = viewModelScope.launch { levelTimer(soundManager) }
    }

    private suspend fun levelTimer(soundManager: SoundManager) {
        while (true) {
            if (_uiState.value.isPaused || _uiState.value.isTransitioning) {
                delay(100)
                continue
            }

            delay(1000) // 1 second tick
            val newTime = _uiState.value.levelTimeRemaining - 1
            _uiState.value = _uiState.value.copy(levelTimeRemaining = newTime)

            if (newTime <= 0) {
                // Level complete
                if (_uiState.value.currentLevel >= 3) {
                    // Game won!
                    showResult(true)
                    return
                } else {
                    // Transition to next level
                    startLevelTransition(soundManager)
                }
            }
        }
    }

    private suspend fun startLevelTransition(soundManager: SoundManager) {
        _uiState.value = _uiState.value.copy(isTransitioning = true, items = emptyList())

        // Play transition sound
        soundManager.playEffect(R.raw.sfx_win)

        // Wait for transition animation
        delay(2000)

        // Move to next level
        val nextLevel = _uiState.value.currentLevel + 1
        _uiState.value =
                _uiState.value.copy(
                        currentLevel = nextLevel,
                        levelTimeRemaining = GameConfig.LEVEL_DURATION_SECONDS,
                        isTransitioning = false
                )
    }

    private suspend fun loop(soundManager: SoundManager) {
        var counter = 0
        while (true) {
            if (_uiState.value.isPaused || _uiState.value.isTransitioning) {
                delay(200)
                continue
            }
            spawnItem(counter)
            moveItems(soundManager)
            counter++
            delay(50) // Faster update rate for smoother animation
        }
    }

    private fun spawnItem(counter: Int) {
        // Don't spawn in last 5 seconds of level
        if (_uiState.value.levelTimeRemaining <= GameConfig.STOP_SPAWN_BEFORE_END_SECONDS) return

        // Random spawning - only spawn sometimes
        val spawnFrequency =
                when (_uiState.value.currentLevel) {
                    1 -> 4 // Easy: spawn every 4th tick
                    2 -> 3 // Medium: spawn every 3rd tick
                    else -> 2 // Hard: spawn every 2nd tick
                }

        if (counter % spawnFrequency != 0) return
        if (Math.random() > 0.6) return // 60% chance to skip even when counter matches

        val reward = Math.random() > 0.3 // 70% chance of reward (egg)
        val lane = (Math.random() * LANES).toInt() // Random lane
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
            val progressed = item.copy(speed = item.speed + 0.015f) // Slower, smoother movement
            if (progressed.speed >= GameConfig.COLLISION_THRESHOLD) {
                // Item has reached the player position
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
                // Keep item on screen a bit longer before removing
                if (progressed.speed >= 1.2f) {
                    // Item is off screen, don't add to updated list
                } else {
                    updated.add(progressed)
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

    // --- Selection Logic ---

    fun selectWheelById(id: Int) {
        _uiState.value = _uiState.value.copy(pendingWheelId = id)
    }

    fun selectTurbineById(id: Int) {
        _uiState.value = _uiState.value.copy(pendingTurbineId = id)
    }

    fun applySelectedWheel() {
        val targetId = _uiState.value.pendingWheelId
        val currentId = _uiState.value.wheelLevel
        if (targetId == currentId) return

        val upgrade = wheels.find { it.id == targetId } ?: return

        // Check if already purchased?
        // NOTE: The model doesn't seem to track "purchased" status separate from selection.
        // Assuming strictly linear upgrades or re-purchase?
        // User asked to "fix" it. Assuming simple Logic:
        // If you select it, you verify price. If you already have it (lower level?)
        // The original logic was: if price <= eggs, buy&select.
        // It seems upgrades are non-linear or just individual items.
        // Let's assume if it is NOT the current one, we pay.
        // Ideally we should track "Owned" items.
        // But based on available code, price is deducted on selection.
        // I will stick to price check.

        if (upgrade.price <= _uiState.value.eggs) {
            viewModelScope.launch {
                repository.updateEggs(_uiState.value.eggs - upgrade.price)
                repository.selectWheel(targetId)
                // Pending stays as is, acting as the new current.
            }
        }
    }

    fun applySelectedTurbine() {
        val targetId = _uiState.value.pendingTurbineId
        val currentId = _uiState.value.turbineLevel
        if (targetId == currentId) return

        val upgrade = turbines.find { it.id == targetId } ?: return

        if (upgrade.price <= _uiState.value.eggs) {
            viewModelScope.launch {
                repository.updateEggs(_uiState.value.eggs - upgrade.price)
                repository.selectTurbine(targetId)
            }
        }
    }
}
