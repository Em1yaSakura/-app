package com.example.englishapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.englishapp.data.ProgressStore
import com.example.englishapp.data.Word
import com.example.englishapp.ui.util.rememberTtsSpeaker

@Composable
fun ReviewScreen(
    words: List<Word>,
    progressStore: ProgressStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tts = rememberTtsSpeaker(context)

    val now = System.currentTimeMillis()
    val dueWords = words.filter { w ->
        val next = progressStore.getNextReviewAt(w.id)
        next != 0L && next <= now
    }

    var sessionIndex by remember { mutableIntStateOf(0) }
    var inSession by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "复习",
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(
            text = "到期单词：${dueWords.size}",
            style = MaterialTheme.typography.bodyLarge,
        )

        if (dueWords.isEmpty()) {
            Text(
                text = "暂无需要复习的单词。你可以先去「学习」刷一些新词。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        if (!inSession) {
            Button(onClick = { inSession = true; sessionIndex = 0 }) {
                Text("开始复习")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "提示：这里是简化版SRS（记得会拉长间隔，忘了会很快再出现）。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            return
        }

        val word = dueWords[sessionIndex.coerceIn(0, dueWords.lastIndex)]

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${sessionIndex + 1}/${dueWords.size}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { tts.speak(word.word) }) { Text("发音") }
                }
                Text(text = word.word, style = MaterialTheme.typography.headlineMedium)
                Text(text = word.phonetic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = word.meaning, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "例句：${word.example}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    progressStore.answerReview(word.id, remembered = false)
                    if (sessionIndex >= dueWords.lastIndex) inSession = false else sessionIndex++
                },
            ) { Text("忘了") }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    progressStore.answerReview(word.id, remembered = true)
                    if (sessionIndex >= dueWords.lastIndex) inSession = false else sessionIndex++
                },
            ) { Text("记得") }
        }
    }
}
