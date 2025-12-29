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
        val ownedWheels: Set<Int> = setOf(1),
        val ownedTurbines: Set<Int> = setOf(1),
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val currentSpeedMultiplier: Float
        get() {
            val state = _uiState.value
            val wheelMod = wheels.find { it.id == state.wheelLevel }?.speedModifier ?: 1.0f
            val turbineMod = turbines.find { it.id == state.turbineLevel }?.speedModifier ?: 1.0f
            return wheelMod * turbineMod
        }

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
                repository.selectedTurbine,
                repository.ownedWheels,
                repository.ownedTurbines
            ) { args ->
                val eggs = args[0] as Int
                val sound = args[1] as Boolean
                val music = args[2] as Boolean
                val wheel = args[3] as Int
                val turbine = args[4] as Int
                @Suppress("UNCHECKED_CAST") val ownedW = args[5] as Set<Int>
                @Suppress("UNCHECKED_CAST") val ownedT = args[6] as Set<Int>

                _uiState.value.copy(
                    eggs = eggs,
                    soundEnabled = sound,
                    musicEnabled = music,
                    wheelLevel = wheel,
                    turbineLevel = turbine,
                    ownedWheels = ownedW,
                    ownedTurbines = ownedT,
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
                    var resultingState =
                        _uiState.value.copy(
                            eggs = newState.eggs,
                            soundEnabled = newState.soundEnabled,
                            musicEnabled = newState.musicEnabled,
                            wheelLevel = newState.wheelLevel,
                            turbineLevel = newState.turbineLevel,
                            ownedWheels = newState.ownedWheels,
                            ownedTurbines = newState.ownedTurbines
                        )

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
                playerLane = 1,
                currentLevel = 1,
                levelTimeRemaining = GameConfig.LEVEL_DURATION_SECONDS,
                isTransitioning = false
            )
        soundManager.playEffect(R.raw.sfx_engine)
        loopJob?.cancel()
        timerJob?.cancel()
        loopJob = viewModelScope.launch { loop(soundManager) }
        timerJob = viewModelScope.launch { levelTimer(soundManager) }
    }

    fun resetForNewGame() {
        loopJob?.cancel()
        timerJob?.cancel()
        _uiState.value =
            _uiState.value.copy(
                score = 0,
                playerLane = 1,
                items = emptyList(),
                isPaused = false,
                showTutorial = true,
                showResult = false,
                isWin = false,
                currentLevel = 1,
                levelTimeRemaining = GameConfig.LEVEL_DURATION_SECONDS,
                isTransitioning = false
            )
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
                    showResult(true, soundManager)
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
        soundManager.playEffect(R.raw.sfx_engine)

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

        soundManager.playEffect(R.raw.sfx_engine)
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
            delay(16) // ~60 FPS update rate for smooth animation
        }
    }

    private fun spawnItem(counter: Int) {
        // Don't spawn in last 5 seconds of level
        if (_uiState.value.levelTimeRemaining <= GameConfig.STOP_SPAWN_BEFORE_END_SECONDS) return

        // Random spawning - even less frequent for performance and clarity
        val multiplier = currentSpeedMultiplier
        val baseFrequency =
            when (_uiState.value.currentLevel) {
                1 -> 100 // Level 1 (roughly 1.6s at 60fps)
                2 -> 80 // Level 2 (roughly 1.3s at 60fps)
                else -> 65 // Level 3 (roughly 1.0s at 60fps)
            }

        val spawnFrequency = maxOf(5, (baseFrequency / multiplier).toInt())

        if (counter % spawnFrequency != 0) return
        if (Math.random() > 0.6) return // 60% chance to skip even when counter matches

        val reward = Math.random() > 0.3 // 70% chance of reward (egg)
        val lane = (Math.random() * LANES).toInt() // Random lane
        val speed = (0.06f + 0.01f * (_uiState.value.turbineLevel - 1)) * multiplier
        val newItem = GameItem(counter, lane, reward, speed)
        _uiState.value = _uiState.value.copy(items = _uiState.value.items + newItem)
    }

    private fun moveItems(soundManager: SoundManager) {
        val updated = mutableListOf<GameItem>()
        var score = _uiState.value.score
        var eggs = _uiState.value.eggs
        var crashed = false

        _uiState.value.items.forEach { item ->
            val multiplier = currentSpeedMultiplier
            var progressed = item

            if (item.moveDelayMillis > 0) {
                // Item is in "warm-up" phase, only update its delay
                progressed = item.copy(moveDelayMillis = maxOf(0, item.moveDelayMillis - 16))
            } else {
                // Item is active and moving
                progressed = item.copy(speed = item.speed + 0.0024f * multiplier)
            }

            if (!progressed.isCollisionChecked && progressed.speed >= GameConfig.COLLISION_THRESHOLD
            ) {
                // Mark as checked so it doesn't trigger collision logic again for this item
                progressed = progressed.copy(isCollisionChecked = true)

                // Item has reached the player position
                if (progressed.lane == _uiState.value.playerLane) {
                    // It's a HIT
                    progressed = progressed.copy(isHit = true)

                    if (progressed.isReward) {
                        eggs += 3
                        score += 5
                        soundManager.playEffect(R.raw.sfx_egg)
                    } else {
                        crashed = true
                        soundManager.playEffect(R.raw.sfx_crash)
                    }
                }
            }

            // Keep item on screen until far gone (increased to ensure it goes off-screen)
            if (progressed.speed >= 2.5f) {
                // Item is definitely off screen bottom
            } else {
                updated.add(progressed)
            }
        }

        _uiState.value = _uiState.value.copy(items = updated, score = score)
        if (eggs != _uiState.value.eggs) {
            viewModelScope.launch { repository.updateEggs(eggs) }
        }
        if (crashed) {
            showResult(false, soundManager)
        }
    }

    fun pauseGame() {
        _uiState.value = _uiState.value.copy(isPaused = true)
    }

    fun resumeGame() {
        _uiState.value = _uiState.value.copy(isPaused = false)
    }

    fun showResult(win: Boolean, soundManager: SoundManager? = null) {
        if (_uiState.value.showResult) return
        _uiState.value = _uiState.value.copy(isPaused = true, showResult = true, isWin = win)
        loopJob?.cancel()
        timerJob?.cancel()
        soundManager?.playEffect(if (win) R.raw.sfx_win else R.raw.sfx_lose)
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

        // Check ownership
        if (_uiState.value.ownedWheels.contains(targetId)) {
            viewModelScope.launch { repository.selectWheel(targetId) }
            return
        }

        val upgrade = wheels.find { it.id == targetId } ?: return

        if (upgrade.price <= _uiState.value.eggs) {
            viewModelScope.launch {
                repository.updateEggs(_uiState.value.eggs - upgrade.price)
                repository.addOwnedWheel(targetId)
                repository.selectWheel(targetId)
            }
        }
    }

    fun applySelectedTurbine() {
        val targetId = _uiState.value.pendingTurbineId
        val currentId = _uiState.value.turbineLevel
        if (targetId == currentId) return

        // Check ownership
        if (_uiState.value.ownedTurbines.contains(targetId)) {
            viewModelScope.launch { repository.selectTurbine(targetId) }
            return
        }

        val upgrade = turbines.find { it.id == targetId } ?: return

        if (upgrade.price <= _uiState.value.eggs) {
            viewModelScope.launch {
                repository.updateEggs(_uiState.value.eggs - upgrade.price)
                repository.addOwnedTurbine(targetId)
                repository.selectTurbine(targetId)
            }
        }
    }
}
