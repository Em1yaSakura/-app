package com.example.englishapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.data.api.WordApiClient
import com.example.englishapp.data.api.WordDetail
import com.example.englishapp.data.db.VocabularyRepository
import com.example.englishapp.data.db.Wordbook

@Composable
fun HomeScreen(
    repository: VocabularyRepository,
    onStartLearning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var refreshTrigger by remember { mutableStateOf(0) }
    var wordbooks by remember { mutableStateOf(repository.getWordbooks()) }
    val stats = remember(refreshTrigger) { repository.getDashboardStats() }
    val memDist = remember(refreshTrigger) { repository.getMemoryDistribution() }

    // ── Daily recommendation from online API ────────
    var dailyWord by remember { mutableStateOf<WordDetail?>(null) }
    var dailyLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        dailyLoading = true
        val resp = WordApiClient.fetchRandomWord()
        dailyWord = resp.data
        dailyLoading = false
    }

    // 优雅渐变色
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
        ),
    )
    val accentGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF8A65),
            Color(0xFFFFB74D),
        ),
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ── Header ──
        item {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(primaryGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📊", fontSize = 18.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "学习概览",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // ── Wordbook selector ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "📚 词书切换",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        wordbooks.forEach { wb ->
                            val isSelected = wb.selected
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        repository.selectSingleWordbook(wb.id)
                                        wordbooks = repository.getWordbooks()
                                        refreshTrigger++
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerLow,
                                ),
                            ) {
                                Text(
                                    text = wb.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Daily recommendation card ──
        item {
            AnimatedVisibility(visible = !dailyLoading && dailyWord != null, enter = fadeIn() + slideInVertically()) {
                dailyWord?.let { word ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐ 每日推荐", style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = word.word,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                if (word.usphone != null) {
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "/${word.usphone}/",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f),
                                    )
                                }
                            }
                            if (word.translations.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = word.translations.joinToString("；") { t ->
                                        buildString {
                                            if (t.pos != null) append("${t.pos} ")
                                            append(t.tranCn ?: "")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                                )
                            }
                            val sentence = word.sentences.firstOrNull()
                            if (sentence != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "📖 ${sentence.content}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                                )
                                if (sentence.cn != null) {
                                    Text(
                                        text = sentence.cn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.55f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Today's stats grid ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 今日新词
                GlowingCard(
                    label = "今日新词",
                    value = "${stats.todayNewWords}",
                    gradient = primaryGradient,
                    modifier = Modifier.weight(1f),
                )
                // 待复习
                GlowingCard(
                    label = "待复习",
                    value = "${stats.dueReviews}",
                    gradient = accentGradient,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Second row: duration + streak + mastered ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 今日时长（修复显示：X分Y秒）
                val mins = stats.todayStudySeconds / 60
                val secs = stats.todayStudySeconds % 60
                val timeStr = if (mins > 0) "${mins}分${secs}秒" else "${secs}秒"
                GlowingCard(
                    label = "今日学习",
                    value = timeStr,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF42A5F5), Color(0xFF26C6DA)),
                    ),
                    modifier = Modifier.weight(1f),
                )
                GlowingCard(
                    label = "连续学习",
                    value = "${stats.streakDays}天",
                    gradient = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFAB47BC), Color(0xFF7E57C2)),
                    ),
                    modifier = Modifier.weight(1f),
                )
                GlowingCard(
                    label = "已掌握",
                    value = "${stats.masteredWords}",
                    gradient = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF66BB6A), Color(0xFF26A69A)),
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Memory distribution bar ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF66BB6A)),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "记忆状态分布",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    val total = (memDist.first + memDist.second + memDist.third).coerceAtLeast(1)
                    val fPct = memDist.first * 100f / total
                    val lPct = memDist.second * 100f / total
                    val mPct = memDist.third * 100f / total

                    // Animated progress bar
                    val fWeight by animateFloatAsState(targetValue = fPct, animationSpec = tween(600), label = "f")
                    val lWeight by animateFloatAsState(targetValue = lPct, animationSpec = tween(600), label = "l")
                    val mWeight by animateFloatAsState(targetValue = mPct, animationSpec = tween(600), label = "m")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp)),
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            if (fPct > 0)
                                Box(Modifier.weight(fWeight).fillMaxSize().background(Color(0xFFEF5350)))
                            if (lPct > 0)
                                Box(Modifier.weight(lWeight).fillMaxSize().background(Color(0xFFFFA726)))
                            if (mPct > 0)
                                Box(Modifier.weight(mWeight).fillMaxSize().background(Color(0xFF66BB6A)))
                        }
                        // Center percentage text on bar
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${fPct.toInt()}% | ${lPct.toInt()}% | ${mPct.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        LegendDot("遗忘", Color(0xFFEF5350), "${memDist.first}")
                        LegendDot("学习中", Color(0xFFFFA726), "${memDist.second}")
                        LegendDot("已掌握", Color(0xFF66BB6A), "${memDist.third}")
                    }
                }
            }
        }

        // ── Start learning button ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(primaryGradient),
            ) {
                FilledTonalButton(
                    onClick = onStartLearning,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        "🚀 开始学习",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GlowingCard(
    label: String,
    value: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(gradient),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
