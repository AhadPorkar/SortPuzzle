package com.parsgames.sortpuzzle.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.parsgames.sortpuzzle.AppContainer
import com.parsgames.sortpuzzle.core.audio.SoundEngine.Sfx
import com.parsgames.sortpuzzle.core.engine.LevelFactory
import com.parsgames.sortpuzzle.core.engine.Solver
import com.parsgames.sortpuzzle.core.model.Board
import com.parsgames.sortpuzzle.core.model.LevelSpec
import com.parsgames.sortpuzzle.core.model.Levels
import com.parsgames.sortpuzzle.core.model.Move
import com.parsgames.sortpuzzle.monetization.CoinPrices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Booster { HINT, UNDO, TUBE }

@Immutable
data class GameUiState(
    val spec: LevelSpec,
    val board: Board? = null,
    val loading: Boolean = true,
    val selected: Int? = null,
    val moves: Int = 0,
    val won: Boolean = false,
    val starsEarned: Int = 0,
    val coinsEarned: Int = 0,
    val hint: Move? = null,
    val hintLoading: Boolean = false,
    val stuck: Boolean = false,
    val askBooster: Booster? = null,
    val toast: String? = null
)

class GameViewModel(
    private val container: AppContainer,
    val level: Int
) : ViewModel() {

    private val spec = Levels.specFor(level)

    private val _state = MutableStateFlow(GameUiState(spec = spec))
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    val player = container.player

    private val history = ArrayDeque<Board>()

    init {
        container.music.play(level)
        loadBoard()
    }

    private fun loadBoard() {
        _state.value = GameUiState(spec = spec, loading = true)
        history.clear()
        viewModelScope.launch {
            val board = LevelFactory.build(spec)
            _state.value = _state.value.copy(board = board, loading = false)
        }
    }

    // -------------------------------------------------------------- تعامل

    fun onTubeClick(index: Int) {
        val s = _state.value
        val board = s.board ?: return
        if (s.loading || s.won) return

        val selected = s.selected
        when {
            selected == null -> {
                if (board.tubes[index].isEmpty) {
                    container.sound.play(Sfx.BLOCKED)
                } else {
                    container.sound.play(Sfx.LIFT)
                    _state.value = s.copy(selected = index, hint = null)
                }
            }

            selected == index -> {
                container.sound.play(Sfx.TAP)
                _state.value = s.copy(selected = null)
            }

            else -> {
                val result = board.pour(selected, index)
                if (result == null) {
                    // اگر ریختن ممکن نبود اما لوله‌ی مقصد پر است، انتخاب را جابه‌جا می‌کنیم.
                    if (!board.tubes[index].isEmpty) {
                        container.sound.play(Sfx.LIFT)
                        _state.value = s.copy(selected = index)
                    } else {
                        container.sound.play(Sfx.BLOCKED)
                        _state.value = s.copy(selected = null)
                    }
                } else {
                    applyMove(result.first, index)
                }
            }
        }
    }

    private fun applyMove(next: Board, destination: Int) {
        val s = _state.value
        history.addLast(s.board!!)
        if (history.size > MAX_HISTORY) history.removeFirst()

        val completed = next.tubes[destination].isComplete(next.capacity)
        container.sound.play(if (completed) Sfx.TUBE_DONE else Sfx.DROP)

        val solved = next.isSolved
        val newState = s.copy(
            board = if (solved) next.revealAll() else next,
            selected = null,
            hint = null,
            moves = s.moves + 1,
            stuck = !solved && !next.hasAnyMove()
        )
        _state.value = newState

        if (solved) finishLevel(newState.moves)
    }

    private fun finishLevel(moves: Int) {
        val stars = Levels.starsFor(spec, moves)
        val isVip = player.value.isVip()
        val coins = BASE_WIN_COINS + stars * STAR_COINS + if (isVip) VIP_BONUS_COINS else 0

        container.sound.play(Sfx.LEVEL_WIN)
        _state.value = _state.value.copy(won = true, starsEarned = stars, coinsEarned = coins)

        viewModelScope.launch {
            container.repository.recordWin(level, moves, stars)
            container.repository.addCoins(coins)
        }
    }

    // ------------------------------------------------------------- کمک‌ها

    fun undo() {
        val s = _state.value
        if (s.won || history.isEmpty()) return
        if (player.value.undos <= 0) {
            _state.value = s.copy(askBooster = Booster.UNDO)
            return
        }
        val previous = history.removeLast()
        container.sound.play(Sfx.TAP)
        _state.value = s.copy(board = previous, selected = null, hint = null, stuck = false,
            moves = (s.moves - 1).coerceAtLeast(0))
        viewModelScope.launch {
            container.repository.update { it.copy(undos = (it.undos - 1).coerceAtLeast(0)) }
        }
    }

    fun requestHint() {
        val s = _state.value
        val board = s.board ?: return
        if (s.won || s.hintLoading) return
        if (player.value.hints <= 0) {
            _state.value = s.copy(askBooster = Booster.HINT)
            return
        }
        _state.value = s.copy(hintLoading = true)
        viewModelScope.launch {
            val move = withContext(Dispatchers.Default) { Solver.hint(board) }
            if (move == null) {
                _state.value = _state.value.copy(
                    hintLoading = false,
                    toast = "از این‌جا راهی به پاسخ نیست — بهتر است مرحله را دوباره شروع کنی."
                )
            } else {
                container.sound.play(Sfx.TAP)
                _state.value = _state.value.copy(hint = move, hintLoading = false, selected = null)
                container.repository.update { it.copy(hints = (it.hints - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun addExtraTube() {
        val s = _state.value
        val board = s.board ?: return
        if (s.won) return
        if (player.value.extraTubes <= 0) {
            _state.value = s.copy(askBooster = Booster.TUBE)
            return
        }
        container.sound.play(Sfx.DROP)
        _state.value = s.copy(board = board.withExtraTube(), stuck = false, selected = null, hint = null)
        viewModelScope.launch {
            container.repository.update { it.copy(extraTubes = (it.extraTubes - 1).coerceAtLeast(0)) }
        }
    }

    fun restart() {
        container.sound.play(Sfx.TAP)
        loadBoard()
    }

    // --------------------------------------------- تأمینِ کمک‌های تمام‌شده

    fun dismissBoosterDialog() { _state.value = _state.value.copy(askBooster = null) }

    fun buyBoosterWithCoins(booster: Booster) {
        val price = priceOf(booster)
        viewModelScope.launch {
            val ok = container.repository.spendCoins(price)
            _state.value = _state.value.copy(
                askBooster = null,
                toast = if (ok) null else "سکه‌ات کافی نیست"
            )
            if (ok) {
                grant(booster, 1)
                container.sound.play(Sfx.COIN)
            }
        }
    }

    fun grantBoosterFromAd(booster: Booster) {
        viewModelScope.launch {
            grant(booster, if (booster == Booster.UNDO) 3 else 1)
            container.sound.play(Sfx.COIN)
            _state.value = _state.value.copy(askBooster = null)
        }
    }

    private suspend fun grant(booster: Booster, amount: Int) {
        container.repository.update { s ->
            when (booster) {
                Booster.HINT -> s.copy(hints = s.hints + amount)
                Booster.UNDO -> s.copy(undos = s.undos + amount)
                Booster.TUBE -> s.copy(extraTubes = s.extraTubes + amount)
            }
        }
    }

    fun priceOf(booster: Booster): Int = when (booster) {
        Booster.HINT -> CoinPrices.HINT
        Booster.UNDO -> CoinPrices.UNDO
        Booster.TUBE -> CoinPrices.EXTRA_TUBE
    }

    fun consumeToast() { _state.value = _state.value.copy(toast = null) }

    companion object {
        private const val MAX_HISTORY = 60
        private const val BASE_WIN_COINS = 12
        private const val STAR_COINS = 6
        private const val VIP_BONUS_COINS = 10

        fun factory(container: AppContainer, level: Int) = viewModelFactory {
            initializer { GameViewModel(container, level) }
        }
    }
}
