package com.parsgames.sortpuzzle.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parsgames.sortpuzzle.LocalAppContainer
import com.parsgames.sortpuzzle.R
import com.parsgames.sortpuzzle.core.audio.MidiComposer
import com.parsgames.sortpuzzle.core.model.Levels
import com.parsgames.sortpuzzle.core.util.Persian
import com.parsgames.sortpuzzle.ui.components.BannerAd
import com.parsgames.sortpuzzle.ui.components.BoosterButton
import com.parsgames.sortpuzzle.ui.components.ButtonTone
import com.parsgames.sortpuzzle.ui.components.CoinPill
import com.parsgames.sortpuzzle.ui.components.Confetti
import com.parsgames.sortpuzzle.ui.components.GameButton
import com.parsgames.sortpuzzle.ui.components.GameDialog
import com.parsgames.sortpuzzle.ui.components.GirihBackground
import com.parsgames.sortpuzzle.ui.components.StarRow
import com.parsgames.sortpuzzle.ui.components.TubeView
import com.parsgames.sortpuzzle.ui.nav.findActivity
import com.parsgames.sortpuzzle.ui.theme.LocalGameTheme
import com.parsgames.sortpuzzle.ui.theme.Palette
import kotlin.math.ceil

@Composable
fun GameScreen(
    level: Int,
    onBack: () -> Unit,
    onNextLevel: (Int) -> Unit,
    onShop: () -> Unit
) {
    val container = LocalAppContainer.current
    val activity = findActivity()

    val viewModel: GameViewModel = viewModel(
        key = "level-$level",
        factory = GameViewModel.factory(container, level)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val player by viewModel.player.collectAsStateWithLifecycle()
    val accent = LocalGameTheme.current.accent

    Box(Modifier.fillMaxSize()) {
        GirihBackground(intensity = 0.55f)

        Column(Modifier.fillMaxSize().safeDrawingPadding()) {

            // ------------------------------------------------------ نوار بالا
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.07f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = Palette.SadafDim,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.level_title, Persian.digits(level)),
                        style = MaterialTheme.typography.titleLarge,
                        color = Palette.Sadaf
                    )
                    Text(
                        text = "${Levels.chapterOf(level).title} • آواز ${MidiComposer.modeNameFor(level)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Palette.SadafDim
                    )
                }

                CoinPill(player.coins, onClick = onShop)
            }

            // ---------------------------------------------------- شمارنده‌ها
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Counter(label = stringResource(R.string.moves), value = Persian.number(state.moves))
                Spacer(Modifier.width(26.dp))
                Counter(
                    label = stringResource(R.string.best),
                    value = player.bestMoves[level]?.let { Persian.number(it) } ?: "—"
                )
                if (state.spec.fog) {
                    Spacer(Modifier.width(26.dp))
                    Counter(label = "حالت", value = "مه‌آلود")
                }
            }

            // -------------------------------------------------------- تخته
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val board = state.board
                if (state.loading || board == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = accent, strokeWidth = 3.dp)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            stringResource(R.string.preparing_level),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Palette.SadafDim
                        )
                    }
                } else {
                    val count = board.tubes.size
                    val rows = when {
                        count <= 5 -> 1
                        count <= 10 -> 2
                        else -> 3
                    }
                    val perRow = ceil(count / rows.toFloat()).toInt()
                    val ballSize: Dp = minOf(
                        (maxWidth - 16.dp) / perRow - 12.dp,
                        (maxHeight - 24.dp) / (rows * (board.capacity + 1.35f)),
                        50.dp
                    ).coerceAtLeast(20.dp)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        board.tubes.indices.chunked(perRow).forEach { rowIndices ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                rowIndices.forEach { i ->
                                    TubeView(
                                        tube = board.tubes[i],
                                        capacity = board.capacity,
                                        index = i,
                                        selected = state.selected == i,
                                        highlighted = state.hint?.from == i || state.hint?.to == i,
                                        ballSize = ballSize,
                                        onClick = { viewModel.onTubeClick(i) }
                                    )
                                }
                            }
                        }
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = state.stuck && !state.won,
                    enter = fadeIn(), exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Text(
                        text = stringResource(R.string.no_moves_left),
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.Anaar,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            // ------------------------------------------------------- کمک‌ها
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BoosterButton(
                    icon = Icons.Filled.Undo,
                    label = stringResource(R.string.undo),
                    count = player.undos,
                    onClick = viewModel::undo
                )
                BoosterButton(
                    icon = Icons.Filled.Lightbulb,
                    label = stringResource(R.string.hint),
                    count = player.hints,
                    onClick = viewModel::requestHint,
                    enabled = !state.hintLoading
                )
                BoosterButton(
                    icon = Icons.Filled.AddCircleOutline,
                    label = stringResource(R.string.add_tube),
                    count = player.extraTubes,
                    onClick = viewModel::addExtraTube
                )
                BoosterButton(
                    icon = Icons.Filled.Refresh,
                    label = stringResource(R.string.restart),
                    count = 0,
                    onClick = viewModel::restart
                )
            }

            BannerAd(show = !player.adsRemoved())
        }

        Confetti(play = state.won, modifier = Modifier.fillMaxSize())
    }

    // ------------------------------------------------------------ گفت‌وگوها

    if (state.won) {
        LevelClearedDialog(
            level = level,
            stars = state.starsEarned,
            moves = state.moves,
            coins = state.coinsEarned,
            onNext = {
                val act = activity
                val go = { onNextLevel((level + 1).coerceAtMost(Levels.TOTAL_LEVELS)) }
                if (act != null) container.ads.onLevelFinished(act) { go() } else go()
            },
            onReplay = viewModel::restart,
            onMenu = onBack
        )
    }

    state.askBooster?.let { booster ->
        BoosterDialog(
            booster = booster,
            price = viewModel.priceOf(booster),
            coins = player.coins,
            onWatchAd = {
                val act = activity ?: return@BoosterDialog
                container.ads.showRewarded(act) { earned ->
                    if (earned) viewModel.grantBoosterFromAd(booster) else viewModel.dismissBoosterDialog()
                }
            },
            onBuy = { viewModel.buyBoosterWithCoins(booster) },
            onDismiss = viewModel::dismissBoosterDialog
        )
    }

    state.toast?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(2_600)
            viewModel.consumeToast()
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.Sadaf,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 120.dp, start = 32.dp, end = 32.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.Lajevard.copy(alpha = 0.95f))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun Counter(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Palette.SadafDim)
        Text(value, style = MaterialTheme.typography.titleMedium, color = Palette.Sadaf)
    }
}

@Composable
private fun LevelClearedDialog(
    level: Int,
    stars: Int,
    moves: Int,
    coins: Int,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onMenu: () -> Unit
) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(600)) }

    GameDialog(
        title = stringResource(R.string.level_cleared),
        onDismiss = {},
        dismissOnOutside = false
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            StarRow(stars = (reveal.value * stars).toInt(), size = 40.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                text = "${stringResource(R.string.moves)}: ${Persian.number(moves)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.SadafDim
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.reward_earned, Persian.number(coins)),
                style = MaterialTheme.typography.titleMedium,
                color = Palette.Zaferan
            )
            Spacer(Modifier.height(20.dp))

            GameButton(
                text = stringResource(R.string.next_level),
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                height = 58.dp
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GameButton(
                    text = stringResource(R.string.replay),
                    onClick = onReplay,
                    tone = ButtonTone.Ghost,
                    modifier = Modifier.weight(1f)
                )
                GameButton(
                    text = stringResource(R.string.back_to_menu),
                    onClick = onMenu,
                    tone = ButtonTone.Ghost,
                    modifier = Modifier.weight(1f)
                )
            }
            if (level >= Levels.TOTAL_LEVELS) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "آخرین مرحله‌ی این نسخه را هم حل کردی. دمت گرم!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.Firouzeh,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BoosterDialog(
    booster: Booster,
    price: Int,
    coins: Int,
    onWatchAd: () -> Unit,
    onBuy: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (booster) {
        Booster.HINT -> stringResource(R.string.out_of_hints)
        Booster.UNDO -> stringResource(R.string.out_of_undos)
        Booster.TUBE -> stringResource(R.string.out_of_tubes)
    }

    GameDialog(title = title, onDismiss = onDismiss) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            GameButton(
                text = stringResource(R.string.watch_ad_for),
                onClick = onWatchAd,
                tone = ButtonTone.Gold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            GameButton(
                text = stringResource(R.string.buy_with_coins, Persian.number(price)),
                onClick = onBuy,
                tone = ButtonTone.Primary,
                enabled = coins >= price,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            GameButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss,
                tone = ButtonTone.Ghost,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}