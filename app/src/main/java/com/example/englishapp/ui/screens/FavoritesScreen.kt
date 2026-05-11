package com.example.englishapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.englishapp.data.ProgressStore
import com.example.englishapp.data.Word
import com.example.englishapp.ui.util.rememberTtsSpeaker

@Composable
fun FavoritesScreen(
    words: List<Word>,
    progressStore: ProgressStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tts = rememberTtsSpeaker(context)

    val favorites = words.filter { progressStore.isFavorite(it.id) }
    var selected by remember { mutableStateOf<Word?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "收藏",
            style = MaterialTheme.typography.headlineSmall,
        )

        if (favorites.isEmpty()) {
            Text(
                text = "你还没有收藏单词。在「学习」里点心形图标即可收藏。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(favorites, key = { it.id }) { w ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = w }
                        .padding(vertical = 10.dp),
                ) {
                    Text(text = w.word, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "${w.phonetic}  ${w.meaning}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    val s = selected
    if (s != null) {
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(s.word) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = s.phonetic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = s.meaning)
                    Text(text = "例句：${s.example}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { selected = null }) { Text("关闭") }
            },
            dismissButton = {
                TextButton(onClick = { tts.speak(s.word) }) { Text("发音") }
            },
        )
    }
}
