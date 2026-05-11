package com.example.englishapp.ui

import androidx.annotation.StringRes
import com.example.englishapp.R

enum class AppTab(@StringRes val labelRes: Int) {
    Home(R.string.tab_home),
    Learn(R.string.tab_learn),
    Wordbooks(R.string.tab_wordbooks),
    Statistics(R.string.tab_statistics),
    Settings(R.string.tab_settings),
}
