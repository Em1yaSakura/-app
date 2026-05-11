package com.example.englishapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.englishapp.data.db.VocabularyRepository

@Composable
fun SettingsScreen(
    repository: VocabularyRepository,
    modifier: Modifier = Modifier,
) {
    var dailyGoal by remember { mutableStateOf(30) }
    var showResetAll by remember { mutableStateOf(false) }
    var showResetMastered by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "设置", style = MaterialTheme.typography.headlineSmall)

        Text(text = "每日新单词目标：$dailyGoal 个（不做硬限制）", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = dailyGoal.toFloat(),
            onValueChange = { dailyGoal = it.toInt().coerceIn(5, 200) },
            valueRange = 5f..200f,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { /* MVP：后续写入 settings 表 */ },
            ) {
                Text("保存")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { showResetAll = true },
            ) {
                Text("清空进度")
            }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showResetMastered = true },
        ) {
            Text("重置已掌握单词")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "说明：当前已按《功能介绍.md》接入本地SQLite与艾宾浩斯+5级评级流程；后续可继续补“下载/导入词库、记忆曲线预测图、Top10即将复习”等。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }

    if (showResetAll) {
        AlertDialog(
            onDismissRequest = { showResetAll = false },
            title = { Text("确认清空进度？") },
            text = { Text("这会清空学习记录、复习计划与统计数据（词库与单词本身保留）。") },
            confirmButton = {
                TextButton(onClick = { repository.resetLearningData(); showResetAll = false }) {
                    Text("清空")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAll = false }) {
                    Text("取消")
                }
            },
        )
    }

    if (showResetMastered) {
        AlertDialog(
            onDismissRequest = { showResetMastered = false },
            title = { Text("重置已掌握？") },
            text = { Text("把已掌握的单词重置为学习中（用于重新巩固）。") },
            confirmButton = {
                TextButton(onClick = { repository.resetMasteredWords(); showResetMastered = false }) {
                    Text("重置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetMastered = false }) {
                    Text("取消")
                }
            },
        )
    }
}
