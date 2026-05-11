package com.example.englishapp.data

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.max

class ProgressStore(context: Context) {
    private val sp: SharedPreferences =
        context.getSharedPreferences("word_progress", Context.MODE_PRIVATE)

    fun isFavorite(wordId: Int): Boolean = sp.getBoolean(keyFav(wordId), false)

    fun toggleFavorite(wordId: Int) {
        sp.edit().putBoolean(keyFav(wordId), !isFavorite(wordId)).apply()
    }

    fun isKnown(wordId: Int): Boolean = sp.getBoolean(keyKnown(wordId), false)

    fun setKnown(wordId: Int, known: Boolean) {
        sp.edit().putBoolean(keyKnown(wordId), known).apply()
        markSeen(wordId)
        if (known && getNextReviewAt(wordId) == 0L) {
            // 第一次掌握后，默认安排明天复习
            setNextReviewAt(wordId, System.currentTimeMillis() + DAY_MS)
        }
    }

    fun markSeen(wordId: Int) {
        sp.edit().putLong(keySeen(wordId), System.currentTimeMillis()).apply()
    }

    fun hasSeen(wordId: Int): Boolean = sp.getLong(keySeen(wordId), 0L) != 0L

    fun getNextReviewAt(wordId: Int): Long = sp.getLong(keyNextReview(wordId), 0L)

    private fun setNextReviewAt(wordId: Int, at: Long) {
        sp.edit().putLong(keyNextReview(wordId), at).apply()
    }

    fun getStreak(wordId: Int): Int = sp.getInt(keyStreak(wordId), 0)

    private fun setStreak(wordId: Int, streak: Int) {
        sp.edit().putInt(keyStreak(wordId), max(0, streak)).apply()
    }

    /**
     * 简化版SRS：
     * - 记得：streak +1，间隔按 1/3/7/14/30 天递增
     * - 忘了：streak=0，10分钟后再复习
     */
    fun answerReview(wordId: Int, remembered: Boolean) {
        markSeen(wordId)
        val now = System.currentTimeMillis()
        if (!remembered) {
            setStreak(wordId, 0)
            setNextReviewAt(wordId, now + 10 * MINUTE_MS)
            return
        }

        val nextStreak = getStreak(wordId) + 1
        setStreak(wordId, nextStreak)

        val days = when (nextStreak) {
            1 -> 1
            2 -> 3
            3 -> 7
            4 -> 14
            else -> 30
        }
        setNextReviewAt(wordId, now + days * DAY_MS)
        // 记得一次也视为“掌握过”
        sp.edit().putBoolean(keyKnown(wordId), true).apply()
    }

    fun getDailyGoal(): Int = sp.getInt(KEY_DAILY_GOAL, 20)

    fun setDailyGoal(value: Int) {
        sp.edit().putInt(KEY_DAILY_GOAL, value.coerceIn(5, 200)).apply()
    }

    fun resetAll() {
        sp.edit().clear().apply()
    }

    private fun keyFav(id: Int) = "fav_$id"
    private fun keyKnown(id: Int) = "known_$id"
    private fun keySeen(id: Int) = "seen_$id"
    private fun keyNextReview(id: Int) = "next_$id"
    private fun keyStreak(id: Int) = "streak_$id"

    companion object {
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val MINUTE_MS = 60_000L
        private const val DAY_MS = 24 * 60 * MINUTE_MS
    }
}

