package com.example.englishapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.data.api.WordApiClient
import com.example.englishapp.data.api.WordDetail
import com.example.englishapp.data.db.LearningItem
import com.example.englishapp.data.db.LearningMode
import com.example.englishapp.data.db.Rating
import com.example.englishapp.data.db.SpacedRepetition
import com.example.englishapp.data.db.VocabularyRepository
import com.example.englishapp.data.db.VocabularyRepository.LearningProgress
import com.example.englishapp.ui.util.rememberTtsSpeaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LearnScreen(
    repository: VocabularyRepository,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tts = rememberTtsSpeaker(context)

    var item by remember { mutableStateOf<LearningItem?>(null) }
    var isFlipped by remember { mutableStateOf(false) }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var sessionProgress by remember { mutableIntStateOf(0) }
    var sessionMax by remember { mutableIntStateOf(0) }
    var quickReview by remember { mutableStateOf(false) }
    var previousItems by remember { mutableStateOf<MutableList<LearningItem>>(mutableListOf()) }
    var wordDetail by remember { mutableStateOf<WordDetail?>(null) }
    var loadingDetail by remember { mutableStateOf(false) }
    var detailError by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }  // 当前单词计时（秒）
    var mode by remember { mutableStateOf(LearningMode.MIXED) }
    var progress by remember { mutableStateOf(repository.getLearningProgress()) }
    var modeClicked by remember { mutableStateOf(false) }  // 标记是否主动点击了模式按钮

    // 每秒更新计时器
    LaunchedEffect(item) {
        while (true) {
            elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000L).toInt()
            kotlinx.coroutines.delay(1000L)
        }
    }

    // 翻转卡片时自动获取在线增强释义
    LaunchedEffect(isFlipped) {
        if (isFlipped) {
            val cur = item ?: return@LaunchedEffect
            loadingDetail = true
            detailError = false
            wordDetail = null
            try {
                val resp = withContext(Dispatchers.IO) { WordApiClient.fetchWordDetail(cur.word.word) }
                wordDetail = resp.data
                if (resp.data == null) detailError = true
            } catch (_: Exception) {
                detailError = true
            }
            loadingDetail = false
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400),
        label = "cardFlip",
    )

    fun loadNext(loadMode: LearningMode = mode) {
        item = repository.getNextLearningItem(loadMode)
        isFlipped = false
        startTime = System.currentTimeMillis()
        sessionMax = 30
        sessionProgress = 0
        quickReview = false
        wordDetail = null
        loadingDetail = false
        detailError = false
        // 推送新单词时自动发音
        item?.let { tts.speak(it.word.word) }
        progress = repository.getLearningProgress()
    }

    LaunchedEffect(Unit) { loadNext() }

    // 使用 Box 布局：滚动区域在上方，评级按钮固定在底部
    Box(modifier = modifier.fillMaxSize()) {
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("当前没有可学习的单词", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("请先在「词库」里启用词库", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Box
        }

        val current = item!!
        val record = current.record
        val stage = SpacedRepetition.stageLabel(record?.repetitions ?: 0, record?.memoryStrength ?: 0)
        val strength = (record?.memoryStrength ?: 0).coerceIn(0, 100)

        // ── 上方可滚动区域 ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = if (isFlipped) 80.dp else 8.dp),  // 为底部固定按钮留空
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header with timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                    ),
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("📖", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("学习", style = MaterialTheme.typography.headlineSmall)
                }
                // 实时计时器
                val min = elapsedSeconds / 60
                val sec = elapsedSeconds % 60
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "⏱ %02d:%02d".format(min, sec),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // 模式选择按钮（天蓝高亮）
            Card(
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 模式切换按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val skyBlue = Color(0xFF4FC3F7)
                        val modeItems = listOf(
                            Triple("新单词", LearningMode.NEW_WORDS) { mode != LearningMode.NEW_WORDS },
                            Triple("复习", LearningMode.REVIEW) { mode != LearningMode.REVIEW },
                            Triple("混合", LearningMode.MIXED) { mode != LearningMode.MIXED },
                        )
                        modeItems.forEach { (label, m, isInactive) ->
                            val isActive = mode == m
                            Button(
                                onClick = {
                                    mode = m
                                    modeClicked = true
                                    loadNext(m)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isActive) skyBlue else MaterialTheme.colorScheme.surface,
                                    contentColor = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
                                ),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }

                    // 今日进度
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "今日进度：学习中 ${progress.learningCount} / ${progress.totalAvailable}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Session progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (current.isDueReview) "📝 待复习" else "🆕 新单词",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stage,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Quick review indicator
            if (quickReview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "⚠️ 之前标记为「忘记/模糊」，该词将在 5 秒后快速重现，请再次尝试",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // --- Card with Flip ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isFlipped) 480.dp else 340.dp)
                    .clickable {
                        if (!isFlipped) {
                            isFlipped = true
                            tts.speak(current.word.word)
                        }
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFlipped)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (rotation < 90f) {
                        // Front: word side
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Text(
                                text = current.word.word,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                            if (!current.word.phonetic.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = current.word.phonetic,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                text = "点击卡片翻转查看释义",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        // Back: online enhanced detail
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f }
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            // 单词+音标
                            Text(
                                text = current.word.word,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            if (!current.word.phonetic.isNullOrBlank()) {
                                Text(
                                    text = current.word.phonetic,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            when {
                                loadingDetail -> {
                                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        Text("正在加载在线释义…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                detailError -> {
                                    // 网络失败时显示本地基础内容
                                    Text(
                                        text = current.word.translation ?: current.word.definition ?: "（暂无释义）",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                    )
                                    if (!current.word.example.isNullOrBlank()) {
                                        Text("📖 ${current.word.example}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (!current.word.phrases.isNullOrBlank()) {
                                        Text("🔗 ${current.word.phrases}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                wordDetail != null -> {
                                    val d = wordDetail!!
                                    // 英美音标+发音
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (d.usphone != null) {
                                            AudioButton(text = "🇺🇸 /${d.usphone}/", url = d.usspeech)
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        if (d.ukphone != null) {
                                            AudioButton(text = "🇬🇧 /${d.ukphone}/", url = d.ukspeech)
                                        }
                                    }
                                    // 释义（放大字体）
                                    if (d.translations.isNotEmpty()) {
                                        Text("📝 释义", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                                        d.translations.forEach { t ->
                                            Text(
                                                text = buildString {
                                                    if (t.pos != null) append("${t.pos} ")
                                                    append(t.tranCn ?: "")
                                                },
                                                style = MaterialTheme.typography.titleSmall,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }
                                    }
                                    // 短语
                                    if (d.phrases.isNotEmpty()) {
                                        Text("🔗 短语", fontWeight = FontWeight.SemiBold)
                                        d.phrases.forEach { p ->
                                            Text(
                                                text = "${p.content}  — ${p.cn ?: ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    // 例句
                                    if (d.sentences.isNotEmpty()) {
                                        Text("📖 例句", fontWeight = FontWeight.SemiBold)
                                        d.sentences.take(2).forEach { s ->
                                            Text(s.content ?: "", style = MaterialTheme.typography.bodySmall)
                                            Text(s.cn ?: "", style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    // 同根词
                                    if (d.relWords.isNotEmpty()) {
                                        Text("🌱 同根词", fontWeight = FontWeight.SemiBold)
                                        d.relWords.forEach { rw ->
                                            val wordStr = rw.words.joinToString(", ") { it.word ?: "" }
                                            val tranStr = rw.words.firstOrNull()?.tran ?: ""
                                            if (wordStr.isNotBlank()) {
                                                Text(
                                                    text = "$wordStr  [${rw.pos ?: ""}] $tranStr",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }
                                    // 近义词
                                    if (d.synonyms.isNotEmpty()) {
                                        Text("✅ 近义词", fontWeight = FontWeight.SemiBold)
                                        d.synonyms.forEach { sg ->
                                            val wordStr = sg.words.joinToString(", ") { it.word ?: "" }
                                            Text(
                                                text = "$wordStr  [${sg.pos ?: ""}] ${sg.tran ?: ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Memory strength bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LinearProgressIndicator(
                    progress = { strength / 100f },
                    modifier = Modifier.weight(1f).height(6.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "强度 $strength%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "复习次数：${record?.totalReviews ?: 0}  记忆阶段：$stage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Pronunciation button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                FilledTonalButton(onClick = { tts.speak(current.word.word) }) {
                    Text("🔊 发音")
                }
            }

        }

        // ── 底部固定区域：翻转后显示评级按钮 ──
        if (isFlipped) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // 顶部装饰线
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        Color.Transparent,
                                    ),
                                )
                            ),
                    )
                    Spacer(Modifier.height(2.dp))
                    // ── 上一个按钮 ──
                    if (previousItems.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val prev = previousItems.removeLast()
                                repository.undoLastAnswer(current.word.id)
                                item = prev
                                isFlipped = false
                                startTime = System.currentTimeMillis()
                                quickReview = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("← 上一个（${previousItems.size}个可回退）")
                        }
                    }

                    // 评分提示
                    Text(
                        text = "请为你的记忆情况评分：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    // 五个评级按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        data class RatingOption(val label: String, val rating: Rating, val color: Color?)
                        val ratings = listOf(
                            RatingOption("忘记", Rating.Forget, MaterialTheme.colorScheme.error),
                            RatingOption("模糊", Rating.Vague, MaterialTheme.colorScheme.tertiary),
                            RatingOption("困难", Rating.Hard, MaterialTheme.colorScheme.secondary),
                            RatingOption("认识", Rating.Know, MaterialTheme.colorScheme.primary),
                            RatingOption("熟练", Rating.Fluent, null),
                        )
                        ratings.forEach { option ->
                            val btnModifier = Modifier.weight(1f)
                            val handleRating = {
                                previousItems.add(current.copy())
                                answer(repository, current, option.rating, startTime)
                                if (option.rating == Rating.Forget || option.rating == Rating.Vague) {
                                    quickReview = true
                                }
                                tts.speak(current.word.word)
                                loadNext()
                            }
                            if (option.color != null) {
                                Button(
                                    onClick = { handleRating() },
                                    modifier = btnModifier,
                                    colors = ButtonDefaults.buttonColors(containerColor = option.color),
                                ) {
                                    Text(
                                        option.label,
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                        maxLines = 1,
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { handleRating() },
                                    modifier = btnModifier,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                ) {
                                    Text(
                                        option.label,
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "忘记/模糊→清零(5秒重现)  困难/认识→+10上限90  熟练→已掌握(强度100)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun answer(
    repository: VocabularyRepository,
    item: LearningItem,
    rating: Rating,
    startTime: Long,
) {
    val seconds = ((System.currentTimeMillis() - startTime) / 1000L).toInt().coerceAtLeast(1)
    repository.recordAnswer(item.word, rating, seconds)
}

/**
 * Clickable phonetic button with audio playback from xxapi.cn speech URL.
 */
@androidx.compose.runtime.Composable
private fun AudioButton(text: String, url: String?) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    FilledTonalButton(
        onClick = {
            if (url != null && url.isNotBlank()) {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val mediaPlayer = android.media.MediaPlayer().apply {
                                setDataSource(url)
                                prepare()
                                start()
                                setOnCompletionListener { release() }
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
