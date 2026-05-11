package com.example.englishapp.data.db

data class Wordbook(
    val id: Long,
    val name: String,
    val description: String?,
    val wordCount: Int,
    val selected: Boolean,
)

data class WordEntry(
    val id: Long,
    val wordbookId: Long,
    val word: String,
    val phonetic: String?,
    val translation: String?,
    val definition: String?,
    val example: String?,
    val phrases: String?,
    val roots: String?,
    val synonyms: String?,
    val antonyms: String?,
)

data class LearningRecord(
    val wordId: Long,
    val word: String,
    val easeFactor: Double,
    val intervalSeconds: Long,
    val repetitions: Int,
    val memoryStrength: Int,
    val nextReviewTime: Long,
    val lastReviewTime: Long,
    val totalReviews: Int,
    val correctReviews: Int,
    val consecutiveFailures: Int,
    val mastered: Boolean,
)

data class DashboardStats(
    val todayNewWords: Int,
    val dueReviews: Int,
    val todayStudySeconds: Int,
    val streakDays: Int,
    val masteredWords: Int,
    val totalWords: Int,
    val todayCorrectRate: Int, // 0-100
)

data class LearningItem(
    val word: WordEntry,
    val record: LearningRecord?,
    val isDueReview: Boolean,
)

enum class Rating(val value: Int) {
    Forget(1),
    Vague(2),
    Hard(3),
    Know(4),
    Fluent(5),
}

enum class LearningMode {
    /** 只推送新单词 */
    NEW_WORDS,
    /** 只推送到期复习 */
    REVIEW,
    /** 按原算法：到期复习优先，无复习推新词 */
    MIXED,
}

