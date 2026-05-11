package com.example.englishapp.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.data.db.VocabularyRepository
import kotlin.math.exp
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    repository: VocabularyRepository,
    modifier: Modifier = Modifier,
) {
    val stats = remember { repository.getDashboardStats() }
    val memDist = remember { repository.getMemoryDistribution() }
    val upcomingList = remember { repository.getUpcomingReviews(10) }
    val weeklyStats = remember { repository.getWeeklyStats() }
    val dailyStats = remember { repository.getDailyStatsForDays(7) }
    val learnedWords = remember { repository.getLearnedWords(50) }
    val masteredWords = remember { repository.getMasteredWords(50) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("📈 学习统计", style = MaterialTheme.typography.headlineSmall)
        }

        // Summary row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatsCard("总词数", "${stats.totalWords}", modifier = Modifier.weight(1f))
                StatsCard("已掌握", "${stats.masteredWords}", modifier = Modifier.weight(1f))
                StatsCard("连续学习", "${stats.streakDays}天", modifier = Modifier.weight(1f))
            }
        }

        // ── 本周统计（不含正确率） ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📊 本周统计", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        WeekStatItem("新词", "${weeklyStats.newWords}")
                        WeekStatItem("复习", "${weeklyStats.reviewWords}")
                        WeekStatItem("时长", formatDuration(weeklyStats.totalTimeSec))
                    }
                }
            }
        }

        // ── 学习趋势（近7天） ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(270.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📈 学习趋势", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    LearningTrendChart(dailyStats)
                }
            }
        }

        // ── 记忆分布 ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("记忆分布", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    val total = (memDist.first + memDist.second + memDist.third).coerceAtLeast(1)
                    val fPct = memDist.first * 100f / total
                    val lPct = memDist.second * 100f / total
                    val mPct = memDist.third * 100f / total
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            val strokeWidth = 36f
                            val radius = (size.minDimension - strokeWidth) / 2f
                            val topLeft = Offset(
                                (size.width - radius * 2 - strokeWidth) / 2f,
                                (size.height - radius * 2 - strokeWidth) / 2f,
                            )
                            val arcSize = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                            drawArc(
                                color = Color(0xFFEF5350),
                                startAngle = -90f,
                                sweepAngle = fPct * 3.6f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            )
                            val lStart = -90f + fPct * 3.6f
                            drawArc(
                                color = Color(0xFFFFA726),
                                startAngle = lStart,
                                sweepAngle = lPct * 3.6f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            )
                            val mStart = lStart + lPct * 3.6f
                            drawArc(
                                color = Color(0xFF66BB6A),
                                startAngle = mStart,
                                sweepAngle = mPct * 3.6f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Legend("遗忘", Color(0xFFEF5350), "${memDist.first}")
                        Legend("学习中", Color(0xFFFFA726), "${memDist.second}")
                        Legend("已掌握", Color(0xFF66BB6A), "${memDist.third}")
                    }
                }
            }
        }

        // ── 近期待复习 ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("近期待复习 (Top 10)", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    if (upcomingList.isEmpty()) {
                        Text("暂无待复习单词", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        upcomingList.take(10).forEachIndexed { idx, (word, record) ->
                            if (idx > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("${idx + 1}. ${word.word}", style = MaterialTheme.typography.bodyMedium)
                                val remain = record.nextReviewTime - System.currentTimeMillis()
                                val label = if (remain <= 0) {
                                    "已到期"
                                } else {
                                    val seconds = remain / 1000L
                                    val minutes = remain / 60000L
                                    val hours = remain / 3600000L
                                    val days = hours / 24
                                    when {
                                        seconds < 60 -> "${seconds}秒后"
                                        minutes < 60 -> "${minutes}分钟后"
                                        hours < 24 -> "${hours}小时后"
                                        else -> "${days}天后"
                                    }
                                }
                                Text(label, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // ── 已学单词列表 ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📖 已学单词（最近50个）", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    if (learnedWords.isEmpty()) {
                        Text("还没有学习记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        learnedWords.forEachIndexed { idx, item ->
                            if (idx > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.word.word,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    if (!item.word.translation.isNullOrBlank()) {
                                        Text(
                                            text = item.word.translation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "强度${item.memoryStrength}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        item.memoryStrength >= 100 -> Color(0xFF66BB6A)
                                        item.memoryStrength >= 60 -> Color(0xFFFFA726)
                                        else -> Color(0xFFEF5350)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 已掌握单词列表 ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("✅ 已掌握单词（最近50个）", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    if (masteredWords.isEmpty()) {
                        Text("还没有已掌握的单词", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        masteredWords.forEachIndexed { idx, item ->
                            if (idx > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = item.word.word,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = "强度${item.memoryStrength}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF66BB6A),
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatsCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun WeekStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── 学习趋势柱状图（近7天） ──
@Composable
private fun LearningTrendChart(items: List<VocabularyRepository.DailyStatsItem>) {
    val maxVal = (items.maxOfOrNull { it.newWords + it.reviewWords } ?: 1).coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary
    val lineColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = Modifier.fillMaxSize().padding(start = 8.dp)) {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            DotLabel("新词", barColor)
            Spacer(Modifier.width(12.dp))
            DotLabel("复习", lineColor)
        }
        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(150.dp),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val pad = 4f
                val chartW = size.width - pad * 2
                val chartH = size.height
                val barCount = items.size
                if (barCount == 0) return@Canvas
                val barSlotW = chartW / barCount
                val barW = barSlotW * 0.5f
                val maxH = chartH * 0.85f

                // Grid lines
                for (i in 0..4) {
                    val y = chartH * (1f - i * 0.25f)
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
                }

                items.forEachIndexed { idx, item ->
                    val cx = pad + barSlotW * idx + barSlotW / 2f

                    // 新词（蓝色柱状）
                    val newH = (item.newWords.toFloat() / maxVal) * maxH
                    if (newH > 0) {
                        drawRect(
                            color = barColor,
                            topLeft = Offset(cx - barW / 2f, chartH - newH),
                            size = androidx.compose.ui.geometry.Size(barW * 0.9f, newH),
                        )
                    }

                    // 复习（橙色柱状）
                    val revH = (item.reviewWords.toFloat() / maxVal) * maxH
                    if (revH > 0) {
                        drawRect(
                            color = lineColor,
                            topLeft = Offset(cx, chartH - revH),
                            size = androidx.compose.ui.geometry.Size(barW * 0.9f, revH),
                        )
                    }

                    // ── 数值标签（上下排列避免重叠） ──
                    val total = item.newWords + item.reviewWords
                    if (total > 0) {
                        val barTop = chartH - maxOf(newH, revH)
                        var labelY = barTop - 10f
                        // 新词（蓝色）
                        if (item.newWords > 0) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "新${item.newWords}",
                                cx,
                                labelY,
                                android.graphics.Paint().apply {
                                    color = barColor.hashCode()
                                    textSize = 20f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                },
                            )
                            labelY -= 18f
                        }
                        // 复习（橙色）
                        if (item.reviewWords > 0) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "复${item.reviewWords}",
                                cx,
                                labelY,
                                android.graphics.Paint().apply {
                                    color = lineColor.hashCode()
                                    textSize = 20f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                },
                            )
                        }
                    }
                }
            }
        }

        // X轴日期标签（仅显示月-日）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEach { item ->
                val parts = item.date.split("-")
                val label = if (parts.size >= 3) "${parts[1]}/${parts[2]}" else item.date
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                )
            }
        }
    }
}

// ── 记忆曲线预测 ──
@Composable
private fun MemoryCurveChart() {
    val curveColor = MaterialTheme.colorScheme.primary
    val fillGradient = Brush.verticalGradient(
        colors = listOf(
            curveColor.copy(alpha = 0.25f),
            Color.Transparent,
        ),
    )

    Box(modifier = Modifier.fillMaxWidth().height(130.dp).padding(top = 6.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val padL = 32f
            val padR = 8f
            val padT = 4f
            val padB = 20f
            val w = size.width - padL - padR
            val h = size.height - padT - padB

            // X轴标签位置: 1d, 2d, 4d, 7d, 16d, 30d
            val days = listOf(1, 2, 4, 7, 16, 30)
            val retention = days.map {
                // Ebbinghaus: R = exp(-t/s)  where s ≈ 5-7 days for typical memory
                (exp(-it / 6.0) * 100).roundToInt().toFloat()
            }

            // 绘制填充区域
            val fillPath = Path()
            val firstX = padL + (days[0] - 1).toFloat() / 29f * w
            val firstY = padT + h * (1f - retention[0] / 100f)
            fillPath.moveTo(firstX, padT + h)
            fillPath.lineTo(firstX, firstY)

            var prevX = firstX
            var prevY = firstY
            for (i in 1 until days.size) {
                val x = padL + (days[i] - 1).toFloat() / 29f * w
                val y = padT + h * (1f - retention[i] / 100f)
                // 平滑连接
                val cpx1 = (prevX + x) / 2f
                fillPath.cubicTo(cpx1, prevY, cpx1, y, x, y)
                prevX = x
                prevY = y
            }
            fillPath.lineTo(prevX, padT + h)
            fillPath.close()
            drawPath(fillPath, fillGradient)

            // 绘制曲线
            val linePath = Path()
            linePath.moveTo(firstX, firstY)
            prevX = firstX
            prevY = firstY
            for (i in 1 until days.size) {
                val x = padL + (days[i] - 1).toFloat() / 29f * w
                val y = padT + h * (1f - retention[i] / 100f)
                val cpx1 = (prevX + x) / 2f
                linePath.cubicTo(cpx1, prevY, cpx1, y, x, y)
                prevX = x
                prevY = y
            }
            drawPath(linePath, curveColor, style = Stroke(width = 3f, cap = StrokeCap.Round))

            // 绘制数据点
            prevX = firstX
            prevY = firstY
            for (i in 0 until days.size) {
                val x = padL + (days[i] - 1).toFloat() / 29f * w
                val y = padT + h * (1f - retention[i] / 100f)
                drawCircle(Color.White, radius = 5f, center = Offset(x, y))
                drawCircle(curveColor, radius = 4f, center = Offset(x, y))

                // 数值标签
                drawContext.canvas.nativeCanvas.drawText(
                    "${retention[i].roundToInt()}%",
                    x,
                    y - 10f,
                    android.graphics.Paint().apply {
                        color = curveColor.hashCode()
                        textSize = 22f
                        textAlign = android.graphics.Paint.Align.CENTER
                    },
                )
            }

            // X轴刻度标签
            days.forEachIndexed { i, day ->
                val x = padL + (day - 1).toFloat() / 29f * w
                drawContext.canvas.nativeCanvas.drawText(
                    "${day}d",
                    x,
                    size.height - 2f,
                    android.graphics.Paint().apply {
                        color = Color.Gray.hashCode()
                        textSize = 20f
                        textAlign = android.graphics.Paint.Align.CENTER
                    },
                )
            }
        }
    }
}

@Composable
private fun Legend(label: String, color: Color, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.padding(end = 4.dp).size(12.dp),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(color = color, radius = size.minDimension / 2f)
            }
        }
        Text("$label: $count", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DotLabel(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(8.dp),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(color = color, size = size)
            }
        }
        Spacer(Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatDuration(seconds: Int): String {
    if (seconds < 60) return "${seconds}秒"
    val min = seconds / 60
    if (min < 60) return "${min}分钟"
    val h = min / 60
    val m = min % 60
    return "${h}时${m}分"
}
