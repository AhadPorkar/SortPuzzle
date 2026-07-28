package com.parsgames.sortpuzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.parsgames.sortpuzzle.R
import com.parsgames.sortpuzzle.core.data.PlayerState
import com.parsgames.sortpuzzle.core.model.Levels
import com.parsgames.sortpuzzle.core.util.Persian
import com.parsgames.sortpuzzle.ui.components.CoinPill
import com.parsgames.sortpuzzle.ui.components.GirihBackground
import com.parsgames.sortpuzzle.ui.theme.LocalGameTheme
import com.parsgames.sortpuzzle.ui.theme.Palette

private const val COLUMNS = 5

@Composable
fun LevelMapScreen(
    player: PlayerState,
    onBack: () -> Unit,
    onPlay: (Int) -> Unit
) {
    val gridState = rememberLazyGridState()
    val accent = LocalGameTheme.current.accent

    // موقعیتِ مرحله‌ی جاری، با احتساب سرصفحه‌ی هر فصل.
    LaunchedEffect(Unit) {
        val level = player.lastPlayed.coerceAtLeast(1)
        val chapterIndex = (level - 1) / Levels.LEVELS_PER_CHAPTER
        val within = (level - 1) % Levels.LEVELS_PER_CHAPTER
        val itemsBefore = chapterIndex * (Levels.LEVELS_PER_CHAPTER + COLUMNS) + within
        gridState.scrollToItem((itemsBefore - COLUMNS * 2).coerceAtLeast(0))
    }

    Box(Modifier.fillMaxSize()) {
        GirihBackground(intensity = 0.7f)

        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
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
                        text = stringResource(R.string.levels),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Palette.Sadaf
                    )
                }
                CoinPill(player.coins)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(COLUMNS),
                state = gridState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 18.dp, end = 18.dp, bottom = 32.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Levels.chapters.forEach { chapter ->
                    item(span = { GridItemSpan(COLUMNS) }, key = "ch-${chapter.number}") {
                        ChapterHeader(
                            title = chapter.title,
                            number = chapter.number,
                            stars = (chapter.firstLevel..chapter.lastLevel).sumOf { player.stars[it] ?: 0 },
                            maxStars = Levels.LEVELS_PER_CHAPTER * 3
                        )
                    }
                    items(
                        count = Levels.LEVELS_PER_CHAPTER,
                        key = { i -> chapter.firstLevel + i }
                    ) { i ->
                        val level = chapter.firstLevel + i
                        LevelNode(
                            level = level,
                            stars = player.stars[level] ?: 0,
                            unlocked = level <= player.highestUnlocked,
                            isCurrent = level == player.lastPlayed,
                            accent = accent,
                            onClick = { onPlay(level) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterHeader(title: String, number: Int, stars: Int, maxStars: Int) {
    Column(Modifier.padding(top = 22.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Palette.Sadaf
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.chapter, Persian.digits(number)),
                style = MaterialTheme.typography.labelSmall,
                color = Palette.SadafDim
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.Star, null, tint = Palette.Zaferan, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${Persian.number(stars)}/${Persian.number(maxStars)}",
                style = MaterialTheme.typography.labelSmall,
                color = Palette.SadafDim
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(if (maxStars == 0) 0f else stars.toFloat() / maxStars)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Palette.Zaferan, Palette.Firouzeh)))
            )
        }
    }
}

@Composable
private fun LevelNode(
    level: Int,
    stars: Int,
    unlocked: Boolean,
    isCurrent: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .aspectRatio(0.92f)
            .clip(shape)
            .background(
                if (unlocked) Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.04f))
                ) else Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.18f))
                )
            )
            .border(
                width = if (isCurrent) 2.dp else 1.dp,
                color = when {
                    isCurrent -> accent
                    stars > 0 -> Palette.Zaferan.copy(alpha = 0.4f)
                    else -> Palette.GlassEdge
                },
                shape = shape
            )
            .clickable(enabled = unlocked, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!unlocked) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = stringResource(R.string.locked),
                tint = Palette.SadafDim.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Persian.digits(level),
                    style = MaterialTheme.typography.titleMedium,
                    color = Palette.Sadaf
                )
                Spacer(Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) { i ->
                        Box(
                            Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i < stars) Palette.Zaferan
                                    else Palette.SadafDim.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
            }
        }
    }
}
