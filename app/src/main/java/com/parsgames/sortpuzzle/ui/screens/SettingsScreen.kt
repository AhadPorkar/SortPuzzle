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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parsgames.sortpuzzle.BuildConfig
import com.parsgames.sortpuzzle.LocalAppContainer
import com.parsgames.sortpuzzle.R
import com.parsgames.sortpuzzle.core.audio.SoundEngine
import com.parsgames.sortpuzzle.core.data.PlayerState
import com.parsgames.sortpuzzle.core.util.Persian
import com.parsgames.sortpuzzle.ui.components.ButtonTone
import com.parsgames.sortpuzzle.ui.components.GameButton
import com.parsgames.sortpuzzle.ui.components.GameDialog
import com.parsgames.sortpuzzle.ui.components.GirihBackground
import com.parsgames.sortpuzzle.ui.components.GlassCard
import com.parsgames.sortpuzzle.ui.theme.GameTheme
import com.parsgames.sortpuzzle.ui.theme.Palette
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    player: PlayerState,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var confirmReset by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        GirihBackground(intensity = 0.6f)

        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Palette.Sadaf
                )
            }

            // ---------------------------------------------------- صدا
            GlassCard(Modifier.fillMaxWidth()) {
                Column {
                    ToggleRow(
                        icon = Icons.Filled.MusicNote,
                        title = stringResource(R.string.music),
                        checked = player.musicOn
                    ) { on ->
                        scope.launch { container.repository.update { it.copy(musicOn = on) } }
                    }
                    Spacer(Modifier.height(6.dp))
                    ToggleRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = stringResource(R.string.sfx),
                        checked = player.sfxOn
                    ) { on ->
                        scope.launch { container.repository.update { it.copy(sfxOn = on) } }
                        if (on) container.sound.play(SoundEngine.Sfx.TAP)
                    }
                    Spacer(Modifier.height(6.dp))
                    ToggleRow(
                        icon = Icons.Filled.Vibration,
                        title = stringResource(R.string.haptics),
                        checked = player.hapticsOn
                    ) { on ->
                        scope.launch { container.repository.update { it.copy(hapticsOn = on) } }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // -------------------------------------------------- پوسته‌ها
            GlassCard(Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        stringResource(R.string.theme),
                        style = MaterialTheme.typography.titleLarge,
                        color = Palette.Sadaf
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GameTheme.entries.forEach { theme ->
                            val unlocked = theme.priceInCoins == 0 ||
                                theme.id in player.unlockedThemes ||
                                player.isVip()
                            ThemeSwatch(
                                theme = theme,
                                selected = player.themeId == theme.id,
                                unlocked = unlocked,
                                modifier = Modifier.weight(1f)
                            ) {
                                scope.launch {
                                    if (unlocked) {
                                        container.repository.update { it.copy(themeId = theme.id) }
                                    } else if (container.repository.spendCoins(theme.priceInCoins)) {
                                        container.repository.update {
                                            it.copy(
                                                themeId = theme.id,
                                                unlockedThemes = it.unlockedThemes + theme.id
                                            )
                                        }
                                        container.sound.play(SoundEngine.Sfx.COIN)
                                    } else {
                                        notice = "برای این پوسته ${Persian.number(theme.priceInCoins)} سکه لازم است."
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ------------------------------------------------------ سایر
            GlassCard(Modifier.fillMaxWidth()) {
                Column {
                    GameButton(
                        text = stringResource(R.string.restore_purchases),
                        onClick = { container.billing.restorePurchases() },
                        tone = ButtonTone.Ghost,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    GameButton(
                        text = stringResource(R.string.reset_progress),
                        onClick = { confirmReset = true },
                        tone = ButtonTone.Danger,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "${stringResource(R.string.studio_name)} • ${stringResource(R.string.version, Persian.digits(BuildConfig.VERSION_NAME))}",
                style = MaterialTheme.typography.labelSmall,
                color = Palette.SadafDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(30.dp))
        }

        notice?.let { message ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.Sadaf,
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Palette.Lajevard.copy(alpha = 0.95f))
                        .clickable { notice = null }
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
        }
    }

    if (confirmReset) {
        GameDialog(title = stringResource(R.string.reset_progress), onDismiss = { confirmReset = false }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.reset_progress_confirm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.SadafDim,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                GameButton(
                    text = stringResource(R.string.confirm),
                    onClick = {
                        scope.launch { container.repository.resetProgress() }
                        confirmReset = false
                    },
                    tone = ButtonTone.Danger,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                GameButton(
                    text = stringResource(R.string.cancel),
                    onClick = { confirmReset = false },
                    tone = ButtonTone.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Palette.Firouzeh, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Palette.Sadaf)
        Spacer(Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Palette.Shabrang,
                checkedTrackColor = Palette.Firouzeh,
                uncheckedThumbColor = Palette.SadafDim,
                uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
            )
        )
    }
}

@Composable
private fun ThemeSwatch(
    theme: GameTheme,
    selected: Boolean,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(theme.top, theme.bottom)))
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) theme.accent else Palette.GlassEdge,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                selected -> Icon(Icons.Filled.Check, null, tint = theme.accent, modifier = Modifier.size(20.dp))
                !unlocked -> Icon(Icons.Filled.Lock, null, tint = Palette.SadafDim, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = theme.title,
            style = MaterialTheme.typography.labelSmall,
            color = Palette.SadafDim,
            textAlign = TextAlign.Center
        )
        if (!unlocked) {
            Text(
                text = Persian.number(theme.priceInCoins),
                style = MaterialTheme.typography.labelSmall,
                color = Palette.Zaferan
            )
        }
    }
}
