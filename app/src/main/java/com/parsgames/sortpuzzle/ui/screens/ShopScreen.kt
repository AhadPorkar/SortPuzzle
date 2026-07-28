package com.parsgames.sortpuzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.parsgames.sortpuzzle.LocalAppContainer
import com.parsgames.sortpuzzle.R
import com.parsgames.sortpuzzle.core.data.PlayerState
import com.parsgames.sortpuzzle.core.util.Persian
import com.parsgames.sortpuzzle.monetization.CoinPrices
import com.parsgames.sortpuzzle.monetization.Grant
import com.parsgames.sortpuzzle.monetization.Products
import com.parsgames.sortpuzzle.ui.components.ButtonTone
import com.parsgames.sortpuzzle.ui.components.CoinPill
import com.parsgames.sortpuzzle.ui.components.Divider
import com.parsgames.sortpuzzle.ui.components.GameButton
import com.parsgames.sortpuzzle.ui.components.GirihBackground
import com.parsgames.sortpuzzle.ui.components.GlassCard
import com.parsgames.sortpuzzle.ui.nav.findActivity
import com.parsgames.sortpuzzle.ui.theme.LocalGameTheme
import com.parsgames.sortpuzzle.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private val BUNDLE_WINDOW = TimeUnit.HOURS.toMillis(24)

@Composable
fun ShopScreen(
    player: PlayerState,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val activity = findActivity()
    val scope = rememberCoroutineScope()
    val accent = LocalGameTheme.current.accent

    val rewardedReady by container.ads.rewardedReady.collectAsState()
    val catalog by container.billing.products.collectAsState()
    var notice by remember { mutableStateOf<String?>(null) }

    // پنجره‌ی باندل با نخستین ورود به فروشگاه باز می‌شود.
    LaunchedEffect(Unit) {
        if (player.bundleStartMillis == 0L) {
            container.repository.update { it.copy(bundleStartMillis = System.currentTimeMillis()) }
        }
    }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) { now = System.currentTimeMillis(); delay(1_000) }
    }
    val bundleRemaining = (player.bundleStartMillis + BUNDLE_WINDOW - now).coerceAtLeast(0L)

    Box(Modifier.fillMaxSize()) {
        GirihBackground(intensity = 0.7f)

        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.07f))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = Palette.SadafDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.shop_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Palette.Sadaf
                    )
                }
                CoinPill(player.coins)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 18.dp, end = 18.dp, bottom = 36.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ---------------------------------------------- سکه‌ی رایگان
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column {
                            SectionTitle(stringResource(R.string.free_coins), Icons.Filled.PlayCircleOutline, accent)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.free_coins_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.SadafDim
                            )
                            Spacer(Modifier.height(14.dp))
                            GameButton(
                                text = if (rewardedReady) stringResource(R.string.watch_ad_for) else stringResource(R.string.loading),
                                onClick = {
                                    val act = activity ?: return@GameButton
                                    container.ads.showRewarded(act) { earned ->
                                        if (earned) {
                                            scope.launch { container.repository.addCoins(50) }
                                        } else {
                                            notice = "ویدیو آماده نبود. کمی بعد دوباره امتحان کن."
                                        }
                                    }
                                },
                                tone = ButtonTone.Gold,
                                enabled = rewardedReady,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ------------------------------------------- باندل زمان‌دار
                if (bundleRemaining > 0L) {
                    item {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "باندل آغاز راه",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Palette.Sadaf
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Palette.Anaar.copy(alpha = 0.85f))
                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.save_percent, Persian.digits(60)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Palette.Sadaf
                                        )
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                BundleContents(Products.grantsFor(Products.BUNDLE_STARTER))
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    stringResource(R.string.bundle_timer, Persian.countdown(bundleRemaining)),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Palette.Zaferan
                                )
                                Spacer(Modifier.height(12.dp))
                                GameButton(
                                    text = container.billing.priceOf(Products.BUNDLE_STARTER, catalog = catalog) ?: "خرید باندل",
                                    onClick = {
                                        activity?.let { container.billing.purchase(it, Products.BUNDLE_STARTER) }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // ------------------------------------------------- کمک‌ها
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column {
                            SectionTitle(stringResource(R.string.tab_boosters), Icons.Filled.Lightbulb, accent)
                            Spacer(Modifier.height(12.dp))
                            BoosterRow(
                                icon = Icons.Filled.Lightbulb,
                                title = stringResource(R.string.hint),
                                owned = player.hints,
                                price = CoinPrices.HINT,
                                coins = player.coins
                            ) {
                                scope.launch {
                                    if (container.repository.spendCoins(CoinPrices.HINT)) {
                                        container.repository.update { it.copy(hints = it.hints + 1) }
                                    } else notice = "سکه‌ات کافی نیست"
                                }
                            }
                            Divider(Modifier.padding(vertical = 10.dp))
                            BoosterRow(
                                icon = Icons.Filled.Undo,
                                title = stringResource(R.string.undo),
                                owned = player.undos,
                                price = CoinPrices.UNDO,
                                coins = player.coins
                            ) {
                                scope.launch {
                                    if (container.repository.spendCoins(CoinPrices.UNDO)) {
                                        container.repository.update { it.copy(undos = it.undos + 3) }
                                    } else notice = "سکه‌ات کافی نیست"
                                }
                            }
                            Divider(Modifier.padding(vertical = 10.dp))
                            BoosterRow(
                                icon = Icons.Filled.AddCircleOutline,
                                title = stringResource(R.string.add_tube),
                                owned = player.extraTubes,
                                price = CoinPrices.EXTRA_TUBE,
                                coins = player.coins
                            ) {
                                scope.launch {
                                    if (container.repository.spendCoins(CoinPrices.EXTRA_TUBE)) {
                                        container.repository.update { it.copy(extraTubes = it.extraTubes + 1) }
                                    } else notice = "سکه‌ات کافی نیست"
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------- بسته‌های سکه
                item { SectionHeader(stringResource(R.string.tab_coins)) }
                items(
                    items = listOf(
                        Triple(Products.COINS_S, 500, "بسته‌ی کوچک"),
                        Triple(Products.COINS_M, 1_500, "بسته‌ی متوسط"),
                        Triple(Products.COINS_L, 4_000, "بسته‌ی بزرگ")
                    ),
                    key = { it.first }
                ) { (id, amount, title) ->
                    CoinPackRow(
                        title = title,
                        amount = amount,
                        price = container.billing.priceOf(id, catalog = catalog),
                        onBuy = { activity?.let { container.billing.purchase(it, id) } }
                    )
                }

                // ------------------------------------------------ حذف تبلیغ
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column {
                            SectionTitle(stringResource(R.string.remove_ads), Icons.Filled.Check, accent)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.remove_ads_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.SadafDim
                            )
                            Spacer(Modifier.height(14.dp))
                            GameButton(
                                text = if (player.noAds) stringResource(R.string.owned)
                                else container.billing.priceOf(Products.REMOVE_ADS, catalog = catalog) ?: stringResource(R.string.remove_ads),
                                onClick = {
                                    activity?.let { container.billing.purchase(it, Products.REMOVE_ADS) }
                                },
                                enabled = !player.noAds,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ---------------------------------------------------- VIP
                item {
                    VipCard(
                        player = player,
                        monthlyPrice = container.billing.priceOf(Products.VIP_SUB, Products.VIP_PLAN_MONTHLY, catalog),
                        yearlyPrice = container.billing.priceOf(Products.VIP_SUB, Products.VIP_PLAN_YEARLY, catalog),
                        onSubscribe = { plan ->
                            activity?.let { container.billing.purchase(it, Products.VIP_SUB, plan) }
                        }
                    )
                }

                item {
                    GameButton(
                        text = stringResource(R.string.restore_purchases),
                        onClick = { container.billing.restorePurchases() },
                        tone = ButtonTone.Ghost,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        notice?.let { message ->
            LaunchedEffect(message) { delay(2_400); notice = null }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.Sadaf,
                    modifier = Modifier
                        .padding(bottom = 44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Palette.Lajevard.copy(alpha = 0.95f))
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = Palette.Sadaf,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = Palette.Sadaf)
    }
}

@Composable
private fun BundleContents(grant: Grant) {
    val pack = grant as? Grant.Pack ?: return
    val parts = buildList {
        if (pack.coins > 0) add("${Persian.number(pack.coins)} سکه")
        if (pack.hints > 0) add("${Persian.digits(pack.hints)} راهنما")
        if (pack.undos > 0) add("${Persian.digits(pack.undos)} برگرداندن")
        if (pack.tubes > 0) add("${Persian.digits(pack.tubes)} لوله‌ی کمکی")
        if (pack.removeAds) add("حذف تبلیغات")
    }
    Text(
        text = parts.joinToString(" • "),
        style = MaterialTheme.typography.bodyMedium,
        color = Palette.Mina
    )
}

@Composable
private fun BoosterRow(
    icon: ImageVector,
    title: String,
    owned: Int,
    price: Int,
    coins: Int,
    onBuy: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color.White.copy(alpha = 0.07f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Palette.Firouzeh, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Palette.Sadaf)
            Text(
                "موجودی: ${Persian.digits(owned)}",
                style = MaterialTheme.typography.labelSmall,
                color = Palette.SadafDim
            )
        }
        GameButton(
            text = Persian.number(price),
            onClick = onBuy,
            tone = ButtonTone.Ghost,
            enabled = coins >= price,
            height = 42.dp
        )
    }
}

@Composable
private fun CoinPackRow(title: String, amount: Int, price: String?, onBuy: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Palette.Zaferan, Color(0xFFC9821A))))
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Palette.Sadaf)
                Text(
                    "${Persian.number(amount)} سکه",
                    style = MaterialTheme.typography.labelSmall,
                    color = Palette.SadafDim
                )
            }
            GameButton(
                text = price ?: "خرید",
                onClick = onBuy,
                tone = ButtonTone.Gold,
                height = 44.dp
            )
        }
    }
}

@Composable
private fun VipCard(
    player: PlayerState,
    monthlyPrice: String?,
    yearlyPrice: String?,
    onSubscribe: (String) -> Unit
) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.WorkspacePremium, null,
                    tint = Palette.Zaferan, modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.vip_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Palette.Sadaf
                )
            }
            Spacer(Modifier.height(12.dp))
            listOf(
                stringResource(R.string.vip_perk_ads),
                stringResource(R.string.vip_perk_daily),
                stringResource(R.string.vip_perk_hints),
                stringResource(R.string.vip_perk_themes)
            ).forEach { perk ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                    Icon(Icons.Filled.Check, null, tint = Palette.Firouzeh, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(perk, style = MaterialTheme.typography.bodyMedium, color = Palette.SadafDim)
                }
            }
            Spacer(Modifier.height(16.dp))

            if (player.isVip()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Palette.Zaferan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.vip_active_until, Persian.shortDate(player.vipUntilMillis)),
                        style = MaterialTheme.typography.labelLarge,
                        color = Palette.Zaferan
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameButton(
                        text = monthlyPrice ?: stringResource(R.string.subscribe_monthly),
                        onClick = { onSubscribe(Products.VIP_PLAN_MONTHLY) },
                        tone = ButtonTone.Ghost,
                        modifier = Modifier.weight(1f)
                    )
                    GameButton(
                        text = yearlyPrice ?: stringResource(R.string.subscribe_yearly),
                        onClick = { onSubscribe(Products.VIP_PLAN_YEARLY) },
                        tone = ButtonTone.Gold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
