package com.example.englishapp.data.db

// CET-6 词汇 - 共4032个单词
object CET6WordData {
    val WORDS: List<WordDataEntry> = listOf(
        *CET6WordData1.WORDS.toTypedArray(),
        *CET6WordData2.WORDS.toTypedArray(),
        *CET6WordData3.WORDS.toTypedArray(),
        *CET6WordData4.WORDS.toTypedArray(),
        *CET6WordData5.WORDS.toTypedArray(),
        *CET6WordData6.WORDS.toTypedArray(),
        *CET6WordData7.WORDS.toTypedArray(),
        *CET6WordData8.WORDS.toTypedArray(),
        *CET6WordData9.WORDS.toTypedArray(),
    )
}