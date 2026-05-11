package com.example.englishapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.englishapp.data.db.VocabularyRepository
import com.example.englishapp.ui.screens.HomeScreen
import com.example.englishapp.ui.screens.LearnScreen
import com.example.englishapp.ui.screens.SettingsScreen
import com.example.englishapp.ui.screens.StatisticsScreen
import com.example.englishapp.ui.screens.WordbooksScreen

@Composable
fun EnglishWordApp(repository: VocabularyRepository) {
    var currentTab by remember { mutableStateOf(AppTab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            // 不引入额外依赖的情况下，用文字充当图标占位
                            Text(text = stringResource(tab.labelRes).take(1))
                        },
                        label = { Text(text = stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (currentTab) {
            AppTab.Home ->
                HomeScreen(
                    repository = repository,
                    onStartLearning = { currentTab = AppTab.Learn },
                    modifier = Modifier.padding(innerPadding),
                )

            AppTab.Learn ->
                LearnScreen(
                    repository = repository,
                    modifier = Modifier.padding(innerPadding),
                )

            AppTab.Wordbooks ->
                WordbooksScreen(
                    repository = repository,
                    modifier = Modifier.padding(innerPadding),
                )

            AppTab.Statistics ->
                StatisticsScreen(
                    repository = repository,
                    modifier = Modifier.padding(innerPadding),
                )

            AppTab.Settings ->
                SettingsScreen(
                    repository = repository,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }
}
