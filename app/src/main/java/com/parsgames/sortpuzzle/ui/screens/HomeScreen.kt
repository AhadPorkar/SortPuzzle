package com.parsgames.sortpuzzle.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsgames.sortpuzzle.LocalAppContainer
import com.parsgames.sortpuzzle.R
import com.parsgames.sortpuzzle.core.data.PlayerState
import com.parsgames.sortpuzzle.core.util.Persian
import com.parsgames.sortpuzzle.ui.components.BannerAd
import com.parsgames.sortpuzzle.ui.components.ButtonTone
import com.parsgames.sortpuzzle.ui.components.CoinPill
import com.parsgames.sortpuzzle.ui.components.GameButton
import com.parsgames.sortpuzzle.ui.components.GameDialog
import com.parsgames.sortpuzzle.ui.components.GirihBackground
import com.parsgames.sortpuzzle.ui.theme.Palette
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    player: PlayerState,
    onPlay: (Int) -> Unit,
    onLevels: () -> Unit,
    onShop: () -> Unit,
    onSettings: () -> Unit
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var showDaily by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition(label = "logo")
    val float by transition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(3_400), RepeatMode.Reverse),
        label = "float"
    )

    val dailyReady = DailyReward.isReady(player)

    Box(Modifier.fillMaxSize()) {
        GirihBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ------- نوار بالا
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CoinPill(player.coins, onClick = onShop)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (player.isVip()) {
                        Icon(
                            Icons.Filled.WorkspacePremium,
                            contentDescription = stringResource(R.string.vip_title),
                            tint = Palette.Zaferan,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.07f))
                            .clickable(onClick = onSettings),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = Palette.SadafDim,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(1.dp).weight(0.6f))

            // ------- عنوان
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { translationY = float }
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp),
                    color = Palette.Sadaf
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "چیدمانِ رنگ‌ها",
                    style = MaterialTheme.typography.titleMedium,
                    color = Palette.Firouzeh
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "${Persian.number(player.totalStars)} ستاره از ${Persian.number(player.totalWins)} برد",
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.SadafDim
                )
            }

            Spacer(Modifier.height(1.dp).weight(0.8f))

            // ------- دکمه‌ها
            GameButton(
                text = stringResource(R.string.continue_level, Persian.number(player.lastPlayed)),
                onClick = { onPlay(player.lastPlayed) },
                icon = Icons.Filled.PlayArrow,
                modifier = Modifier.fillMaxWidth(),
                height = 64.dp
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameButton(
                    text = stringResource(R.string.levels),
                    onClick = onLevels,
                    tone = ButtonTone.Ghost,
                    icon = Icons.Filled.ViewModule,
                    modifier = Modifier.weight(1f)
                )
                GameButton(
                    text = stringResource(R.string.shop),
                    onClick = onShop,
                    tone = ButtonTone.Ghost,
                    icon = Icons.Filled.ShoppingBag,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))

            GameButton(
                text = stringResource(R.string.daily_gift),
                onClick = { showDaily = true },
                tone = if (dailyReady) ButtonTone.Gold else ButtonTone.Ghost,
                icon = Icons.Filled.CardGiftcard,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(1.dp).weight(0.4f))

            BannerAd(show = !player.adsRemoved())
        }
    }

    if (showDaily) {
        DailyRewardDialog(
            player = player,
            onDismiss = { showDaily = false },
            onClaim = { doubled ->
                scope.launch {
                    DailyReward.claim(container, doubled)
                    showDaily = false
                }
            }
        )
    }
}

// --------------------------------------------------------------- هدیه‌ی روزانه

object DailyReward {

    fun isReady(player: PlayerState, now: Long = System.currentTimeMillis()): Boolean =
        !isSameDay(player.lastDailyClaimMillis, now)

    fun streakAfterClaim(player: PlayerState, now: Long = System.currentTimeMillis()): Int {
        val gap = now - player.lastDailyClaimMillis
        val continued = gap < TimeUnit.DAYS.toMillis(2)
        return if (continued) player.dailyStreak + 1 else 1
    }

    fun amountFor(streak: Int): Int = 50 + (streak - 1).coerceIn(0, 6) * 25

    suspend fun claim(container: com.parsgames.sortpuzzle.AppContainer, doubled: Boolean) {
        container.repository.update { s ->
            if (!isReady(s)) return@update s
            val streak = streakAfterClaim(s)
            val amount = amountFor(streak) * if (doubled) 2 else 1
            s.copy(
                coins = s.coins + amount,
                dailyStreak = streak,
                lastDailyClaimMillis = System.currentTimeMillis(),
                hints = if (streak % 7 == 0) s.hints + 2 else s.hints
            )
        }
        container.sound.play(com.parsgames.sortpuzzle.core.audio.SoundEngine.Sfx.COIN)
    }

    private fun isSameDay(a: Long, b: Long): Boolean {
        if (a == 0L) return false
        val ca = Calendar.getInstance().apply { timeInMillis = a }
        val cb = Calendar.getInstance().apply { timeInMillis = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }
}

@Composable
private fun DailyRewardDialog(
    player: PlayerState,
    onDismiss: () -> Unit,
    onClaim: (doubled: Boolean) -> Unit
) {
    val container = LocalAppContainer.current
    val activity = com.parsgames.sortpuzzle.ui.nav.findActivity()
    val ready = DailyReward.isReady(player)
    val streak = DailyReward.streakAfterClaim(player)
    val amount = DailyReward.amountFor(streak)

    GameDialog(title = stringResource(R.string.daily_title), onDismiss = onDismiss) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (ready) "${Persian.number(amount)} سکه" else stringResource(R.string.daily_claimed),
                style = MaterialTheme.typography.headlineLarge,
                color = if (ready) Palette.Zaferan else Palette.SadafDim,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.daily_streak, Persian.number(if (ready) streak else player.dailyStreak)),
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.SadafDim
            )
            Spacer(Modifier.height(18.dp))

            if (ready) {
                GameButton(
                    text = stringResource(R.string.daily_claim),
                    onClick = { onClaim(false) },
                    tone = ButtonTone.Gold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                GameButton(
                    text = stringResource(R.string.daily_double),
                    onClick = {
                        val act = activity ?: return@GameButton
                        container.ads.showRewarded(act) { earned -> onClaim(earned) }
                    },
                    tone = ButtonTone.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                GameButton(
                    text = stringResource(R.string.close),
                    onClick = onDismiss,
                    tone = ButtonTone.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
