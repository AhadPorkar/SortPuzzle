package com.parsgames.sortpuzzle

import android.app.Application
import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.parsgames.sortpuzzle.core.audio.MusicPlayer
import com.parsgames.sortpuzzle.core.audio.SoundEngine
import com.parsgames.sortpuzzle.core.data.PlayerRepository
import com.parsgames.sortpuzzle.core.data.PlayerState
import com.parsgames.sortpuzzle.monetization.AdsManager
import com.parsgames.sortpuzzle.monetization.BillingManager
import com.parsgames.sortpuzzle.monetization.Grant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * ظرفِ وابستگی‌ها.
 *
 * بازی به‌عمد از یک ظرفِ دستی به‌جای کتابخانه‌ی تزریق وابستگی استفاده می‌کند:
 * تعداد سرویس‌ها کم و طول عمرشان یکسان است، پس افزودن پردازشگرِ نشانه‌گذاری
 * فقط زمانِ ساخت را بالا می‌برد بی‌آنکه چیزی ساده‌تر شود.
 */
class AppContainer(context: Context) {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val repository = PlayerRepository(context)
    val sound = SoundEngine()
    val music = MusicPlayer(context, scope)
    val ads = AdsManager(context, scope)

    val billing = BillingManager(context, scope) { grant -> applyGrant(grant) }

    val player: StateFlow<PlayerState> = repository.state
        .onEach { s ->
            music.enabled = s.musicOn
            sound.enabled = s.sfxOn
            ads.adsRemoved = s.adsRemoved()
        }
        .stateIn(scope, SharingStarted.Eagerly, PlayerState())

    fun start() {
        ads.initialize()
        billing.start()
        scope.launch { grantVipDailyIfDue() }
    }

    /** تحویلِ کالای خریداری‌شده. تنها نقطه‌ای که خرید به دارایی تبدیل می‌شود. */
    suspend fun applyGrant(grant: Grant) {
        when (grant) {
            is Grant.RemoveAds -> repository.update { it.copy(noAds = true) }

            is Grant.Vip -> repository.update { s ->
                val base = maxOf(s.vipUntilMillis, System.currentTimeMillis())
                s.copy(vipUntilMillis = base + TimeUnit.DAYS.toMillis(grant.days.toLong()))
            }

            is Grant.Pack -> repository.update { s ->
                s.copy(
                    coins = s.coins + grant.coins,
                    hints = s.hints + grant.hints,
                    undos = s.undos + grant.undos,
                    extraTubes = s.extraTubes + grant.tubes,
                    noAds = s.noAds || grant.removeAds
                )
            }
        }
        sound.play(SoundEngine.Sfx.COIN)
    }

    /** هدیه‌ی روزانه‌ی مشترکان ویژه. */
    private suspend fun grantVipDailyIfDue() {
        repository.update { s ->
            val now = System.currentTimeMillis()
            if (!s.isVip(now)) return@update s
            if (now - s.vipDailyGrantMillis < TimeUnit.DAYS.toMillis(1)) return@update s
            s.copy(
                coins = s.coins + VIP_DAILY_COINS,
                hints = s.hints + VIP_DAILY_HINTS,
                vipDailyGrantMillis = now
            )
        }
    }

    fun release() {
        music.release()
        billing.release()
    }

    companion object {
        const val VIP_DAILY_COINS = 100
        const val VIP_DAILY_HINTS = 3
    }
}

class SortPuzzleApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.start()
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer در درختِ Compose قرار نگرفته است")
}
