package com.parsgames.sortpuzzle.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.parsgames.sortpuzzle.core.model.Levels
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "player")

/**
 * تنها منبعِ حقیقت برای وضعیت بازیکن. همه‌ی تغییرها اتمی‌اند و بلافاصله
 * روی دیسک می‌نشینند تا بستنِ ناگهانی بازی چیزی را از بین نبرد.
 */
class PlayerRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val state: Flow<PlayerState> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { prefs ->
            prefs[KEY_STATE]?.let { raw ->
                runCatching { json.decodeFromString<PlayerState>(raw) }.getOrDefault(PlayerState())
            } ?: PlayerState()
        }

    suspend fun update(transform: (PlayerState) -> PlayerState) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_STATE]
                ?.let { runCatching { json.decodeFromString<PlayerState>(it) }.getOrNull() }
                ?: PlayerState()
            prefs[KEY_STATE] = json.encodeToString(transform(current))
        }
    }

    // ------------------------------------------------------ عملیاتِ پرتکرار

    suspend fun addCoins(amount: Int) = update { it.copy(coins = (it.coins + amount).coerceAtLeast(0)) }

    suspend fun spendCoins(amount: Int): Boolean {
        var ok = false
        update { s ->
            if (s.coins >= amount) { ok = true; s.copy(coins = s.coins - amount) } else s
        }
        return ok
    }

    suspend fun recordWin(level: Int, moves: Int, stars: Int) = update { s ->
        val prevStars = s.stars[level] ?: 0
        val prevBest = s.bestMoves[level] ?: Int.MAX_VALUE
        s.copy(
            stars = s.stars + (level to maxOf(prevStars, stars)),
            bestMoves = s.bestMoves + (level to minOf(prevBest, moves)),
            highestUnlocked = maxOf(s.highestUnlocked, (level + 1).coerceAtMost(Levels.TOTAL_LEVELS)),
            lastPlayed = (level + 1).coerceAtMost(Levels.TOTAL_LEVELS),
            totalWins = s.totalWins + 1
        )
    }

    suspend fun resetProgress() = update {
        PlayerState(
            noAds = it.noAds,
            vipUntilMillis = it.vipUntilMillis,
            musicOn = it.musicOn,
            sfxOn = it.sfxOn,
            hapticsOn = it.hapticsOn,
            themeId = it.themeId,
            unlockedThemes = it.unlockedThemes
        )
    }

    private companion object {
        val KEY_STATE = stringPreferencesKey("player_state_v1")
    }
}
