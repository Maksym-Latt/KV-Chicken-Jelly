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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
    private var lastGeneratedIsReward: Boolean? = null
    private var consecutiveTypeCount: Int = 0

    val wheels =
        listOf(
            Upgrade(1, "Roadster", 0, R.drawable.wheel_1, 1.2f),
            Upgrade(2, "Gripper", 50, R.drawable.wheel_2, 1.3f),
            Upgrade(3, "TurboGrip", 120, R.drawable.wheel_3, 1.5f),
            Upgrade(4, "FeatherSpin", 200, R.drawable.wheel_4, 1.7f),
        )

    val turbines =
        listOf(
            Upgrade(1, "Breeze", 0, R.drawable.turbine_1, 1.1f),
            Upgrade(2, "Draft", 60, R.drawable.turbine_2, 1.5f),
            Upgrade(3, "Gust", 150, R.drawable.turbine_3, 1.7f),
            Upgrade(4, "Cyclone", 250, R.drawable.turbine_4, 2f),
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
            uiState.map { it.soundEnabled }.distinctUntilChanged().collect { enabled ->
                soundManager.updateSoundEnabled(enabled)
            }
        }
        viewModelScope.launch {
            uiState.map { it.musicEnabled }.distinctUntilChanged().collect { enabled ->
                soundManager.updateMusicEnabled(enabled)
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
        lastGeneratedIsReward = null
        consecutiveTypeCount = 0
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
        lastGeneratedIsReward = null
        consecutiveTypeCount = 0
    }

    private suspend fun levelTimer(soundManager: SoundManager) {
        while (true) {
            if (_uiState.value.isPaused || _uiState.value.isTransitioning) {
                delay(100)
                continue
            }

            delay(1000)
            val newTime = _uiState.value.levelTimeRemaining - 1
            _uiState.value = _uiState.value.copy(levelTimeRemaining = newTime)

            if (newTime <= 0) {
                if (_uiState.value.currentLevel >= 3) {
                    showResult(true, soundManager)
                    return
                } else {
                    startLevelTransition(soundManager)
                }
            }
        }
    }

    private suspend fun startLevelTransition(soundManager: SoundManager) {
        _uiState.value = _uiState.value.copy(isTransitioning = true, items = emptyList())

        soundManager.playEffect(R.raw.sfx_engine)

        delay(2000)

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
            delay(16)
        }
    }

    private fun spawnItem(counter: Int) {
        if (_uiState.value.levelTimeRemaining <= GameConfig.STOP_SPAWN_BEFORE_END_SECONDS) return

        val multiplier = currentSpeedMultiplier
        val baseFrequency =
            when (_uiState.value.currentLevel) {
                1 -> 100
                2 -> 80
                else -> 65
            }

        val spawnFrequency = maxOf(5, (baseFrequency / multiplier).toInt())

        if (counter % spawnFrequency != 0) return
        if (Math.random() > 0.6) return

        val lane = (Math.random() * LANES).toInt()

        val reward =
            when {
                lastGeneratedIsReward == null -> Math.random() > 0.5
                consecutiveTypeCount >= 2 -> !lastGeneratedIsReward!!
                else -> {
                    if (Math.random() > 0.3) !lastGeneratedIsReward!!
                    else lastGeneratedIsReward!!
                }
            }

        if (reward == lastGeneratedIsReward) {
            consecutiveTypeCount++
        } else {
            lastGeneratedIsReward = reward
            consecutiveTypeCount = 1
        }

        val startingProgress = 0.0f
        val newItem = GameItem(counter, lane, reward, startingProgress)
        _uiState.value = _uiState.value.copy(items = _uiState.value.items + newItem)
    }

    private fun moveItems(soundManager: SoundManager) {
        val updated = mutableListOf<GameItem>()
        val playerLane = _uiState.value.playerLane
        var score = _uiState.value.score
        var eggs = _uiState.value.eggs
        var crashed = false

        _uiState.value.items.forEach { item ->
            val multiplier = currentSpeedMultiplier
            var progressed =
                if (item.moveDelayMillis > 0) {
                    item.copy(moveDelayMillis = maxOf(0, item.moveDelayMillis - 16))
                } else {
                    item.copy(speed = item.speed + 0.0024f * multiplier)
                }

            if (!progressed.isHit && progressed.speed >= 0.7f && progressed.speed <= 1.15f) {
                if (progressed.lane == playerLane) {
                    // HIT!
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

            if (progressed.speed >= 2.5f) {
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
