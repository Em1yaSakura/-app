package com.example.englishapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.englishapp.data.db.VocabularyRepository
import com.example.englishapp.ui.EnglishWordApp
import com.example.englishapp.ui.theme.EnglishAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = VocabularyRepository(this).also { it.ensureSampleData() }
        setContent {
            EnglishAPPTheme {
                EnglishWordApp(repository = repository)
            }
        }
    }
}
