package com.chicken.jelly.data

import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val soundEnabled: Flow<Boolean>
    val musicEnabled: Flow<Boolean>
    val eggsBalance: Flow<Int>
    val selectedWheel: Flow<Int>
    val selectedTurbine: Flow<Int>

    val ownedWheels: Flow<Set<Int>>
    val ownedTurbines: Flow<Set<Int>>

    suspend fun updateSound(enabled: Boolean)
    suspend fun updateMusic(enabled: Boolean)
    suspend fun updateEggs(amount: Int)
    suspend fun selectWheel(level: Int)
    suspend fun selectTurbine(level: Int)
    suspend fun addOwnedWheel(id: Int)
    suspend fun addOwnedTurbine(id: Int)
}
