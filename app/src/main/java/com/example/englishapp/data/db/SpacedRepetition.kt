package com.example.englishapp.data.db

import kotlin.math.min

/**
 * 根据《功能介绍.md》：
 * - 艾宾浩斯时间点：5m,20m,40m,1d,2d,4d,7d,16d,30d
 * - 记忆强度：忘记/模糊 => 0；困难/认识 => +10(上限90)；熟练 => 100
 * - 忘记/模糊：5秒后重新出现
 */
object SpacedRepetition {
    val intervalsSeconds = longArrayOf(
        5 * 60L,            // 5分钟
        20 * 60L,           // 20分钟
        40 * 60L,           // 40分钟
        1 * 24 * 60 * 60L,  // 1天
        2 * 24 * 60 * 60L,  // 2天
        4 * 24 * 60 * 60L,  // 4天
        7 * 24 * 60 * 60L,  // 7天
        16 * 24 * 60 * 60L, // 16天
        30 * 24 * 60 * 60L, // 30天
    )

    /**
     * 将记忆强度映射到对应的间隔节点数（repetitions）。
     * 强度每+10对应一次正确复习，如强度40=4次正确复习。
     */
    fun strengthToRepetitions(strength: Int): Int =
        (strength / 10).coerceIn(0, intervalsSeconds.size)

    const val IMMEDIATE_RETRY_SECONDS = 5L
    const val PENALTY_RETRY_SECONDS = 4L  // 连续忘记3次后加速到4秒

    data class UpdateResult(
        val repetitions: Int,
        val intervalSeconds: Long,
        val memoryStrength: Int,
        val nextReviewTime: Long,
        val mastered: Boolean,
        val correct: Boolean,
        val consecutiveFailures: Int,
        val correctReviews: Int,
        val totalReviews: Int,
    )

    fun applyRating(
        now: Long,
        previous: LearningRecord?,
        rating: Rating,
    ): UpdateResult {
        val prev = previous
        val prevStrength = prev?.memoryStrength ?: 0
        val prevReps = prev?.repetitions ?: 0
        val prevFailures = prev?.consecutiveFailures ?: 0
        val prevCorrect = prev?.correctReviews ?: 0
        val prevTotal = prev?.totalReviews ?: 0

        // 总复习次数：任何评级都算一次尝试
        val totalReviews = prevTotal + 1

        return when (rating) {
            Rating.Forget, Rating.Vague -> {
                // 连续失败递增惩罚：忘记3次后从5秒缩短到4秒
                val newFailures = prevFailures + 1
                val retrySeconds = if (newFailures <= 3) IMMEDIATE_RETRY_SECONDS else PENALTY_RETRY_SECONDS
                UpdateResult(
                    repetitions = 0,
                    intervalSeconds = 0,
                    memoryStrength = 0,
                    nextReviewTime = now + retrySeconds * 1000L,
                    mastered = false,
                    correct = false,
                    consecutiveFailures = newFailures,
                    correctReviews = prevCorrect,
                    totalReviews = totalReviews,
                )
            }

            Rating.Hard, Rating.Know -> {
                val newStrength = min(90, prevStrength + 10)
                val newReps = prevReps + 1
                val interval = intervalsSeconds[(newReps - 1).coerceIn(0, intervalsSeconds.lastIndex)]
                UpdateResult(
                    repetitions = newReps,
                    intervalSeconds = interval,
                    memoryStrength = newStrength,
                    nextReviewTime = now + interval * 1000L,
                    mastered = false,
                    correct = true,
                    consecutiveFailures = 0,
                    correctReviews = prevCorrect + 1,
                    totalReviews = totalReviews,
                )
            }

            Rating.Fluent -> {
                UpdateResult(
                    repetitions = maxOf(prevReps + 1, 1),
                    intervalSeconds = 0,
                    memoryStrength = 100,
                    nextReviewTime = 0L,
                    mastered = true,
                    correct = true,
                    consecutiveFailures = 0,
                    correctReviews = prevCorrect + 1,
                    totalReviews = totalReviews,
                )
            }
        }
    }

    /**
     * 用于UI展示 “第N次复习 - 40分钟” 之类的文本。
     * @param repetitions 实际复习次数
     * @param memoryStrength 记忆强度（可选），若提供则取两者较大值作为展示阶段
     */
    fun stageLabel(repetitions: Int, memoryStrength: Int = 0): String {
        val strengthReps = strengthToRepetitions(memoryStrength)
        val effectiveReps = maxOf(repetitions, strengthReps)
        val nextIndex = effectiveReps.coerceAtLeast(0).coerceAtMost(intervalsSeconds.size)
        if (nextIndex == 0) return "学习中"
        if (nextIndex > intervalsSeconds.lastIndex) return "长期巩固"
        val sec = intervalsSeconds[(nextIndex - 1).coerceIn(0, intervalsSeconds.lastIndex)]
        return "第${nextIndex}次复习 - ${formatInterval(sec)}"
    }

    fun formatInterval(seconds: Long): String = when {
        seconds < 60 -> "${seconds}秒"
        seconds < 3600 -> "${seconds / 60}分钟"
        seconds < 24 * 3600 -> "${seconds / 3600}小时"
        else -> "${seconds / (24 * 3600)}天"
    }

    /**
     * 判断是否需要快速重现（推送机制第4条）
     * 评级<3（忘记/模糊）且间隔 <= 5分钟 → 立即再次出现
     */
    fun needsQuickReview(rating: Rating, intervalSeconds: Long): Boolean {
        return (rating == Rating.Forget || rating == Rating.Vague) && intervalSeconds <= 300L
    }
}

