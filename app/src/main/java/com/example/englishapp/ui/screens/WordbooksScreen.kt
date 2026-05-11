package com.example.englishapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.englishapp.data.db.VocabularyRepository

@Composable
fun WordbooksScreen(
    repository: VocabularyRepository,
    modifier: Modifier = Modifier,
) {
    val wordbooksState = remember { mutableStateOf(repository.getWordbooks()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "词库管理", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "当前版本：已内置示例词库；“下载词库 / 导入词库”后续按需求补齐。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )

        wordbooksState.value.forEach { wb ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = wb.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${wb.wordCount} 个单词",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (!wb.description.isNullOrBlank()) {
                            Text(
                                text = wb.description,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Switch(
                        checked = wb.selected,
                        onCheckedChange = { checked ->
                            repository.setWordbookSelected(wb.id, checked)
                            wordbooksState.value = repository.getWordbooks()
                        },
                    )
                }
            }
        }
    }
}

