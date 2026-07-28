package com.parsgames.sortpuzzle.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * لایه‌ی خرید درون‌برنامه‌ای روی Google Play Billing نسخه‌ی ۸.
 *
 * وظیفه‌ی این کلاس فقط گفت‌وگو با Play است؛ تحویلِ کالا از راه [onGrant] به
 * لایه‌ی داده سپرده می‌شود تا منطقِ اقتصاد بازی یک‌جا بماند.
 *
 * نکته‌ی امنیتی: برای بازی‌های تجاری، تأیید نهاییِ رسیدها را روی سرور خودتان
 * با Google Play Developer API انجام دهید. این پیاده‌سازی، اعتبارسنجی سمت
 * دستگاه را انجام می‌دهد که برای شروع کافی است.
 */
class BillingManager(
    context: Context,
    private val scope: CoroutineScope,
    private val onGrant: suspend (Grant) -> Unit
) : PurchasesUpdatedListener {

    private val _products = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val products: StateFlow<Map<String, ProductDetails>> = _products.asStateFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private var retryDelayMs = 1_000L

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    fun start() {
        if (client.isReady) return
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _connected.value = true
                    retryDelayMs = 1_000L
                    queryProducts()
                    restorePurchases()
                } else {
                    _connected.value = false
                    Log.w(TAG, "اتصال به Play برقرار نشد: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _connected.value = false
                scope.launch {
                    delay(retryDelayMs)
                    retryDelayMs = (retryDelayMs * 2).coerceAtMost(60_000L)
                    start()
                }
            }
        })
    }

    // ------------------------------------------------------------ کالاها

    private fun queryProducts() {
        fun query(ids: List<String>, type: String) {
            if (ids.isEmpty()) return
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ids.map {
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(it)
                            .setProductType(type)
                            .build()
                    }
                ).build()

            client.queryProductDetailsAsync(params) { result, productDetailsResult ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "دریافت کالاها ناموفق بود: ${result.debugMessage}")
                    return@queryProductDetailsAsync
                }
                val fetched = productDetailsResult.productDetailsList
                _products.value = _products.value + fetched.associateBy { it.productId }
            }
        }
        query(Products.oneTimeProducts, BillingClient.ProductType.INAPP)
        query(Products.subscriptions, BillingClient.ProductType.SUBS)
    }

    /**
     * قیمتِ محلی‌شده برای نمایش؛ اگر Play در دسترس نباشد، خالی برمی‌گردد.
     *
     * [catalog] را از `products` بگیرید تا رابط کاربری با رسیدنِ قیمت‌ها
     * دوباره ترسیم شود.
     */
    fun priceOf(
        productId: String,
        basePlanId: String? = null,
        catalog: Map<String, ProductDetails> = _products.value
    ): String? {
        val details = catalog[productId] ?: return null
        details.oneTimePurchaseOfferDetails?.let { return it.formattedPrice }
        val offers = details.subscriptionOfferDetails ?: return null
        val offer = basePlanId?.let { id -> offers.firstOrNull { it.basePlanId == id } } ?: offers.first()
        return offer.pricingPhases.pricingPhaseList.firstOrNull()?.formattedPrice
    }

    // ------------------------------------------------------------- خرید

    fun purchase(activity: Activity, productId: String, basePlanId: String? = null) {
        val details = _products.value[productId] ?: run {
            _lastError.value = "این کالا در حال حاضر در دسترس نیست."
            return
        }

        val builder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        // اشتراک‌ها همیشه به توکنِ پیشنهاد نیاز دارند؛ کالاهای یک‌بارمصرف نه.
        val offerToken: String? = details.subscriptionOfferDetails
            ?.let { offers -> basePlanId?.let { id -> offers.firstOrNull { it.basePlanId == id } } ?: offers.firstOrNull() }
            ?.offerToken

        if (offerToken != null) builder.setOfferToken(offerToken)

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(builder.build()))
            .build()

        client.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                purchases?.forEach { handlePurchase(it) }

            BillingClient.BillingResponseCode.USER_CANCELED -> Unit   // بی‌صدا

            else -> _lastError.value = "خرید کامل نشد. دوباره تلاش کنید."
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        scope.launch {
            purchase.products.forEach { productId ->
                onGrant(Products.grantsFor(productId))
            }

            val consumable = purchase.products.any { Products.isConsumable(it) }
            if (consumable) {
                val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                client.consumeAsync(params) { r, _ ->
                    if (r.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.w(TAG, "مصرف کالا ناموفق: ${r.debugMessage}")
                    }
                }
            } else if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                client.acknowledgePurchase(params) { r ->
                    if (r.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.w(TAG, "تأیید خرید ناموفق: ${r.debugMessage}")
                    }
                }
            }
        }
    }

    /** بازیابیِ خریدهای ماندگار و اشتراک‌های فعال. */
    fun restorePurchases() {
        listOf(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS).forEach { type ->
            val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
            client.queryPurchasesAsync(params) { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases.forEach { handlePurchase(it) }
                }
            }
        }
    }

    fun clearError() { _lastError.value = null }

    fun release() {
        runCatching { client.endConnection() }
    }

    private companion object { const val TAG = "BillingManager" }
}
