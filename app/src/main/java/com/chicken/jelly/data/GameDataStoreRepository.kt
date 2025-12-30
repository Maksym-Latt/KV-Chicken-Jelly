package com.chicken.jelly.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "chicken_jelly_store"

private val Context.dataStore by preferencesDataStore(DATA_STORE_NAME)

class GameDataStoreRepository(private val context: Context) : GameRepository {

    private val soundKey = booleanPreferencesKey("sound_enabled")
    private val musicKey = booleanPreferencesKey("music_enabled")
    private val eggsKey = intPreferencesKey("eggs_balance")
    private val wheelKey = intPreferencesKey("wheel_level")
    private val turbineKey = intPreferencesKey("turbine_level")
    private val ownedWheelsKey = stringSetPreferencesKey("owned_wheels")
    private val ownedTurbinesKey = stringSetPreferencesKey("owned_turbines")

    override val soundEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[soundKey] ?: true }

    override val musicEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[musicKey] ?: true }

    override val eggsBalance: Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[eggsKey] ?: 0 }

    override val selectedWheel: Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[wheelKey] ?: 1 }

    override val selectedTurbine: Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[turbineKey] ?: 1 }

    override val ownedWheels: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val set = prefs[ownedWheelsKey] ?: setOf("1")
            set.mapNotNull { it.toIntOrNull() }.toSet()
        }

    override val ownedTurbines: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val set = prefs[ownedTurbinesKey] ?: setOf("1")
            set.mapNotNull { it.toIntOrNull() }.toSet()
        }

    override suspend fun updateSound(enabled: Boolean) {
        updatePreference(soundKey, enabled)
    }

    override suspend fun updateMusic(enabled: Boolean) {
        updatePreference(musicKey, enabled)
    }

    override suspend fun updateEggs(amount: Int) {
        updatePreference(eggsKey, amount)
    }

    override suspend fun selectWheel(level: Int) {
        updatePreference(wheelKey, level)
    }

    override suspend fun selectTurbine(level: Int) {
        updatePreference(turbineKey, level)
    }

    override suspend fun addOwnedWheel(id: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[ownedWheelsKey] ?: setOf("1")
            prefs[ownedWheelsKey] = current + id.toString()
        }
    }

    override suspend fun addOwnedTurbine(id: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[ownedTurbinesKey] ?: setOf("1")
            prefs[ownedTurbinesKey] = current + id.toString()
        }
    }

    private suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }
}
