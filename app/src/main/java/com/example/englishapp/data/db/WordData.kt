package com.example.englishapp.data.db

// 300+ CET-4 vocabulary words with complete data
data class WordDataEntry(
    val word: String,
    val phonetic: String,
    val translation: String,
    val example: String,
    val phrases: String,
    val roots: String,
    val synonyms: String,
    val antonyms: String
)

// CET-4 词汇已合并到考研英语词汇(KaoYanWordData)中
// 此处保留空列表以维持 KaoYanWordData.kt 中的引用
object WordData {
    val WORDS: List<WordDataEntry> = emptyList()
}
