package com.parsgames.sortpuzzle.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.parsgames.sortpuzzle.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * مدیریتِ تبلیغ‌ها.
 *
 * قاعده‌ی طراحیِ ما ساده است: **ویدیوی جایزه‌دار همیشه اختیاری است** و تبلیغِ
 * میان‌برنامه‌ای هرگز بلافاصله پس از باخت یا در میانه‌ی مرحله نمایش داده
 * نمی‌شود. سقفِ نمایش هم رعایت می‌شود تا تجربه‌ی بازی خراب نشود.
 */
class AdsManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var initialized = false

    private var rewarded: RewardedAd? = null
    private var interstitial: InterstitialAd? = null

    private var loadingRewarded = false
    private var loadingInterstitial = false

    private var levelsSinceInterstitial = 0
    private var lastInterstitialAt = 0L

    private val _rewardedReady = MutableStateFlow(false)
    val rewardedReady: StateFlow<Boolean> = _rewardedReady.asStateFlow()

    /** وقتی بازیکن تبلیغات را حذف کرده باشد، تبلیغِ اجباری کاملاً غیرفعال می‌شود. */
    var adsRemoved: Boolean = false

    fun initialize() {
        if (initialized) return
        initialized = true
        scope.launch(Dispatchers.IO) {
            MobileAds.initialize(context) {
                loadRewarded()
                if (!adsRemoved) loadInterstitial()
            }
        }
    }

    private fun request() = AdRequest.Builder().build()

    // -------------------------------------------------------- ویدیوی جایزه‌دار

    fun loadRewarded() {
        if (loadingRewarded || rewarded != null) return
        loadingRewarded = true
        RewardedAd.load(context, BuildConfig.AD_UNIT_REWARDED, request(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                    loadingRewarded = false
                    _rewardedReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewarded = null
                    loadingRewarded = false
                    _rewardedReady.value = false
                    Log.w(TAG, "ویدیوی جایزه‌دار بارگذاری نشد: ${error.message}")
                }
            })
    }

    /**
     * @param onResult با `true` صدا زده می‌شود اگر بازیکن ویدیو را تا پایان دیده
     *        باشد. اگر تبلیغی آماده نباشد، `false` برمی‌گردد تا رابط کاربری
     *        بتواند پیام مناسب نشان دهد.
     */
    fun showRewarded(activity: Activity, onResult: (Boolean) -> Unit) {
        val ad = rewarded
        if (ad == null) {
            loadRewarded()
            onResult(false)
            return
        }
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null
                _rewardedReady.value = false
                loadRewarded()
                onResult(earned)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewarded = null
                _rewardedReady.value = false
                loadRewarded()
                onResult(false)
            }
        }
        ad.show(activity) { earned = true }
    }

    // ---------------------------------------------------------- میان‌برنامه‌ای

    fun loadInterstitial() {
        if (adsRemoved || loadingInterstitial || interstitial != null) return
        loadingInterstitial = true
        InterstitialAd.load(context, BuildConfig.AD_UNIT_INTERSTITIAL, request(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    loadingInterstitial = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    loadingInterstitial = false
                    Log.w(TAG, "تبلیغ میان‌برنامه‌ای بارگذاری نشد: ${error.message}")
                }
            })
    }

    /** پس از بردنِ مرحله صدا زده می‌شود؛ خودش تصمیم می‌گیرد نمایش بدهد یا نه. */
    fun onLevelFinished(activity: Activity, onClosed: () -> Unit) {
        if (adsRemoved) { onClosed(); return }
        levelsSinceInterstitial++

        val now = System.currentTimeMillis()
        val enoughLevels = levelsSinceInterstitial >= LEVELS_BETWEEN_ADS
        val enoughTime = now - lastInterstitialAt >= MIN_GAP_MS
        val ad = interstitial

        if (!enoughLevels || !enoughTime || ad == null) {
            loadInterstitial()
            onClosed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                levelsSinceInterstitial = 0
                lastInterstitialAt = System.currentTimeMillis()
                loadInterstitial()
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitial = null
                loadInterstitial()
                onClosed()
            }
        }
        ad.show(activity)
    }

    private companion object {
        const val TAG = "AdsManager"
        const val LEVELS_BETWEEN_ADS = 3
        const val MIN_GAP_MS = 100_000L
    }
}
