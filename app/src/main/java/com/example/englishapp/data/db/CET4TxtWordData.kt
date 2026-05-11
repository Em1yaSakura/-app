package com.example.englishapp.data.db

// CET-4 词汇 - 共4446个单词，来自四级词汇正序版.txt
object CET4TxtWordData {
    val WORDS: List<WordDataEntry> = listOf(
        *CET4TxtWordData1.WORDS.toTypedArray(),
        *CET4TxtWordData2.WORDS.toTypedArray(),
        *CET4TxtWordData3.WORDS.toTypedArray(),
        *CET4TxtWordData4.WORDS.toTypedArray(),
        *CET4TxtWordData5.WORDS.toTypedArray(),
        *CET4TxtWordData6.WORDS.toTypedArray(),
        *CET4TxtWordData7.WORDS.toTypedArray(),
        *CET4TxtWordData8.WORDS.toTypedArray(),
        *CET4TxtWordData9.WORDS.toTypedArray(),
    )
}