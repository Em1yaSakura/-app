package com.example.englishapp.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VocabularyRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dbHelper = LocalVocabularyDbHelper(appContext)

    fun ensureSampleData() {
        val db = dbHelper.writableDatabase
        val now = System.currentTimeMillis()

        // 清除旧版 CET-4 词书（CET-4 词汇已合并到考研词汇）
        cleanupOldCET4Wordbook(db)

        // 逐个词书检查，确保已安装的旧版本也能添加新词书
        rebuildKaoYanWordbook(db, now)
        ensureCET6Wordbook(db, now)
        ensureCET4TxtWordbook(db, now)
    }

    private fun cleanupOldCET4Wordbook(db: android.database.sqlite.SQLiteDatabase) {
        val exists = db.rawQuery("SELECT COUNT(1) FROM wordbooks WHERE id=1", null).use { c ->
            c.moveToFirst(); c.getInt(0) > 0
        }
        if (!exists) return

        db.beginTransaction()
        try {
            db.execSQL(
                "DELETE FROM learning_records WHERE word_id IN (SELECT id FROM words WHERE wordbook_id=1)"
            )
            db.execSQL(
                "DELETE FROM review_history WHERE word_id IN (SELECT id FROM words WHERE wordbook_id=1)"
            )
            db.execSQL("DELETE FROM words WHERE wordbook_id=1")
            db.execSQL("DELETE FROM user_wordbooks WHERE wordbook_id=1")
            db.execSQL("DELETE FROM wordbooks WHERE id=1")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun rebuildKaoYanWordbook(db: android.database.sqlite.SQLiteDatabase, now: Long) {
        db.beginTransaction()
        try {
            // 无论是否已存在，先删旧数据（保证老用户也能更新为新词库）
            val exists = db.rawQuery("SELECT COUNT(1) FROM wordbooks WHERE id=2", null).use { c ->
                c.moveToFirst(); c.getInt(0) > 0
            }
            if (exists) {
                db.execSQL(
                    "DELETE FROM learning_records WHERE word_id IN (SELECT id FROM words WHERE wordbook_id=2)"
                )
                db.execSQL(
                    "DELETE FROM review_history WHERE word_id IN (SELECT id FROM words WHERE wordbook_id=2)"
                )
                db.execSQL("DELETE FROM words WHERE wordbook_id=2")
                db.execSQL("DELETE FROM user_wordbooks WHERE wordbook_id=2")
                db.execSQL("DELETE FROM wordbooks WHERE id=2")
                // 当考研词书重建时，将其他词书也取消选中
                db.execSQL("UPDATE user_wordbooks SET selected=0")
            }

            val kaoyanBookId = 2L
            db.execSQL(
                "INSERT INTO wordbooks(id,name,description,word_count,source,created_at) VALUES(?,?,?,?,?,?)",
                arrayOf(kaoyanBookId, "考研英语词汇", "6500+ 考研核心词汇，含单词与中文释义。", 0, "builtin", now),
            )
            db.execSQL(
                "INSERT INTO user_wordbooks(wordbook_id,selected,updated_at) VALUES(?,?,?)",
                arrayOf(kaoyanBookId, 1, now),
            )

            var kaoyanCount = 0
            KaoYanWordData.WORDS.forEach { w ->
                val cv = ContentValues().apply {
                    put("wordbook_id", kaoyanBookId)
                    put("word", w.word)
                    put("phonetic", w.phonetic)
                    put("translation", w.translation)
                    put("example_sentence", w.example)
                    put("phrases", w.phrases)
                    put("roots", w.roots)
                    put("synonyms", w.synonyms)
                    put("antonyms", w.antonyms)
                }
                db.insert("words", null, cv)
                kaoyanCount++
            }
            db.execSQL("UPDATE wordbooks SET word_count=? WHERE id=?", arrayOf(kaoyanCount, kaoyanBookId))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun ensureCET6Wordbook(db: android.database.sqlite.SQLiteDatabase, now: Long) {
        val exists = db.rawQuery("SELECT COUNT(1) FROM wordbooks WHERE id=3", null).use { c ->
            c.moveToFirst(); c.getInt(0) > 0
        }
        if (exists) return

        db.beginTransaction()
        try {
            val bookId = 3L
            db.execSQL(
                "INSERT INTO wordbooks(id,name,description,word_count,source,created_at) VALUES(?,?,?,?,?,?)",
                arrayOf(bookId, "CET-6 词汇大全", "4000+ CET-6级词汇，含单词与中文释义。", 0, "builtin", now),
            )
            // CET-6 默认不选中，让用户手动切换
            db.execSQL(
                "INSERT INTO user_wordbooks(wordbook_id,selected,updated_at) VALUES(?,?,?)",
                arrayOf(bookId, 0, now),
            )

            var count = 0
            CET6WordData.WORDS.forEach { w ->
                val cv = ContentValues().apply {
                    put("wordbook_id", bookId)
                    put("word", w.word)
                    put("phonetic", w.phonetic)
                    put("translation", w.translation)
                    put("example_sentence", w.example)
                    put("phrases", w.phrases)
                    put("roots", w.roots)
                    put("synonyms", w.synonyms)
                    put("antonyms", w.antonyms)
                }
                db.insert("words", null, cv)
                count++
            }
            db.execSQL("UPDATE wordbooks SET word_count=? WHERE id=?", arrayOf(count, bookId))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun ensureCET4TxtWordbook(db: android.database.sqlite.SQLiteDatabase, now: Long) {
        val exists = db.rawQuery("SELECT COUNT(1) FROM wordbooks WHERE id=4", null).use { c ->
            c.moveToFirst(); c.getInt(0) > 0
        }
        if (exists) return

        db.beginTransaction()
        try {
            val bookId = 4L
            db.execSQL(
                "INSERT INTO wordbooks(id,name,description,word_count,source,created_at) VALUES(?,?,?,?,?,?)",
                arrayOf(bookId, "CET-4 词汇大全", "4400+ CET-4级词汇，来自四级词汇正序版。", 0, "builtin", now),
            )
            // CET-4 默认不选中，让用户手动切换
            db.execSQL(
                "INSERT INTO user_wordbooks(wordbook_id,selected,updated_at) VALUES(?,?,?)",
                arrayOf(bookId, 0, now),
            )

            var count = 0
            CET4TxtWordData.WORDS.forEach { w ->
                val cv = ContentValues().apply {
                    put("wordbook_id", bookId)
                    put("word", w.word)
                    put("phonetic", w.phonetic)
                    put("translation", w.translation)
                    put("example_sentence", w.example)
                    put("phrases", w.phrases)
                    put("roots", w.roots)
                    put("synonyms", w.synonyms)
                    put("antonyms", w.antonyms)
                }
                db.insert("words", null, cv)
                count++
            }
            db.execSQL("UPDATE wordbooks SET word_count=? WHERE id=?", arrayOf(count, bookId))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getWordbooks(): List<Wordbook> {
        val db = dbHelper.readableDatabase
        val sql =
            """
            SELECT wb.id, wb.name, wb.description, wb.word_count,
                   COALESCE(uw.selected, 0) AS selected
            FROM wordbooks wb
            LEFT JOIN user_wordbooks uw ON uw.wordbook_id = wb.id
            ORDER BY wb.id ASC
            """.trimIndent()

        return db.rawQuery(sql, null).use { c ->
            val list = mutableListOf<Wordbook>()
            while (c.moveToNext()) {
                list.add(
                    Wordbook(
                        id = c.getLong(0),
                        name = c.getString(1),
                        description = c.getStringOrNull(2),
                        wordCount = c.getInt(3),
                        selected = c.getInt(4) == 1,
                    ),
                )
            }
            list
        }
    }

    fun setWordbookSelected(wordbookId: Long, selected: Boolean) {
        val db = dbHelper.writableDatabase
        val now = System.currentTimeMillis()
        val cv = ContentValues().apply {
            put("wordbook_id", wordbookId)
            put("selected", if (selected) 1 else 0)
            put("updated_at", now)
        }
        db.insertWithOnConflict("user_wordbooks", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun selectSingleWordbook(wordbookId: Long) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("UPDATE user_wordbooks SET selected=0")
            db.execSQL("INSERT OR REPLACE INTO user_wordbooks(wordbook_id,selected,updated_at) VALUES(?,1,?)",
                arrayOf(wordbookId, System.currentTimeMillis()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun resetLearningData() {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM review_history")
            db.execSQL("DELETE FROM daily_stats")
            db.execSQL("DELETE FROM learning_records")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun resetMasteredWords() {
        val db = dbHelper.writableDatabase
        db.execSQL(
            """
            UPDATE learning_records
            SET mastered=0, memory_strength=0, repetitions=0, interval=0, next_review_time=0
            WHERE mastered=1 OR memory_strength=100
            """.trimIndent(),
        )
    }

    fun getDashboardStats(): DashboardStats {
        val db = dbHelper.readableDatabase
        val today = todayKey()
        val now = System.currentTimeMillis()

        val dueReviews = db.rawQuery(
            """
            SELECT COUNT(1)
            FROM learning_records
            WHERE memory_strength < 100
              AND next_review_time IS NOT NULL
              AND next_review_time != 0
              AND next_review_time <= ?
            """.trimIndent(),
            arrayOf(now.toString()),
        ).useFirstInt()

        val masteredWords = db.rawQuery(
            "SELECT COUNT(1) FROM learning_records WHERE mastered=1 OR memory_strength=100",
            null,
        ).useFirstInt()

        val totalWords = db.rawQuery("SELECT COUNT(DISTINCT word) FROM words", null).useFirstInt()

        val todayStats = db.rawQuery(
            "SELECT new_words, review_words, correct_count, total_time FROM daily_stats WHERE date=?",
            arrayOf(today),
        ).use { c ->
            if (!c.moveToFirst()) {
                intArrayOf(0, 0, 0, 0)
            } else {
                intArrayOf(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3))
            }
        }

        val reviewWords = todayStats[1]
        val correctCount = todayStats[2]
        val correctRate = if (reviewWords <= 0) 0 else ((correctCount * 100.0) / reviewWords).toInt()

        return DashboardStats(
            todayNewWords = todayStats[0],
            dueReviews = dueReviews,
            todayStudySeconds = todayStats[3],
            streakDays = calcStreakDays(db),
            masteredWords = masteredWords,
            totalWords = totalWords,
            todayCorrectRate = correctRate.coerceIn(0, 100),
        )
    }

        fun getNextLearningItem(mode: LearningMode = LearningMode.MIXED): LearningItem? {
        val db = dbHelper.readableDatabase
        val now = System.currentTimeMillis()
        val selectedWordbookIds = getSelectedWordbookIds(db)
        if (selectedWordbookIds.isEmpty()) return null

        // MIXED 模式：到期复习优先，无复习推新词（原算法）
        // REVIEW 模式：只推到期复习
        if (mode == LearningMode.MIXED || mode == LearningMode.REVIEW) {
            // 取到期复习
            val dueSql =
                """
                SELECT w.id, w.wordbook_id, w.word, w.phonetic, w.translation, w.definition, w.example_sentence,
                       w.phrases, w.roots, w.synonyms, w.antonyms
                FROM words w
                JOIN learning_records lr ON lr.word_id = w.id
                WHERE lr.memory_strength < 100
                  AND lr.next_review_time IS NOT NULL
                  AND lr.next_review_time != 0
                  AND lr.next_review_time <= ?
                  AND w.wordbook_id IN (${selectedWordbookIds.joinToString(",")})
                ORDER BY
                  CASE WHEN lr.memory_strength < 40 THEN 0
                       WHEN lr.memory_strength < 60 THEN 1
                       ELSE 2
                  END,
                  lr.next_review_time ASC
                LIMIT 1
                """.trimIndent()

            db.rawQuery(dueSql, arrayOf(now.toString())).use { c ->
                if (c.moveToFirst()) {
                    val word = c.toWordEntry()
                    val record = getLearningRecord(word.id)
                    return LearningItem(word = word, record = record, isDueReview = true)
                }
            }
            if (mode == LearningMode.REVIEW) return null  // REVIEW 模式：无复习就结束
        }

        // MIXED 或 NEW_WORDS 模式：推新词
        if (mode == LearningMode.MIXED || mode == LearningMode.NEW_WORDS) {
            val newSql =
                """
                SELECT w.id, w.wordbook_id, w.word, w.phonetic, w.translation, w.definition, w.example_sentence,
                       w.phrases, w.roots, w.synonyms, w.antonyms
                FROM words w
                LEFT JOIN learning_records lr ON lr.word_id = w.id
                WHERE lr.word_id IS NULL
                  AND w.wordbook_id IN (${selectedWordbookIds.joinToString(",")})
                ORDER BY RANDOM()
                LIMIT 1
                """.trimIndent()

            db.rawQuery(newSql, null).use { c ->
                if (!c.moveToFirst()) return null
                val word = c.toWordEntry()
                return LearningItem(word = word, record = null, isDueReview = false)
            }
        }

        return null
    }

    data class LearningProgress(
        val learningCount: Int,
        val totalAvailable: Int,
    )

    fun getLearningProgress(): LearningProgress {
        val db = dbHelper.readableDatabase
        val selectedWordbookIds = getSelectedWordbookIds(db)
        if (selectedWordbookIds.isEmpty()) return LearningProgress(0, 0)

        val ids = selectedWordbookIds.joinToString(",")

        // 学习中 = 有学习记录但未掌握 (memory_strength < 100)
        val learningCount = db.rawQuery(
            "SELECT COUNT(1) FROM learning_records WHERE memory_strength < 100 AND word_id IN (SELECT id FROM words WHERE wordbook_id IN ($ids))",
            null,
        ).useFirstInt()

        // 可选总数 = 所有选中词书的单词数
        val totalWords = db.rawQuery(
            "SELECT COUNT(1) FROM words WHERE wordbook_id IN ($ids)",
            null,
        ).useFirstInt()

        // 已掌握 = memory_strength >= 100
        val masteredCount = db.rawQuery(
            "SELECT COUNT(1) FROM learning_records WHERE memory_strength >= 100 AND word_id IN (SELECT id FROM words WHERE wordbook_id IN ($ids))",
            null,
        ).useFirstInt()

        return LearningProgress(
            learningCount = learningCount,
            totalAvailable = totalWords - masteredCount,
        )
    }

    fun getLearningRecord(wordId: Long): LearningRecord? {
        val db = dbHelper.readableDatabase
        return db.rawQuery(
            """
            SELECT word_id, word, ease_factor, interval, repetitions, memory_strength, 
                   COALESCE(next_review_time, 0), COALESCE(last_review_time, 0),
                   total_reviews, correct_reviews, consecutive_failures, mastered
            FROM learning_records
            WHERE word_id=?
            """.trimIndent(),
            arrayOf(wordId.toString()),
        ).use { c ->
            if (!c.moveToFirst()) return null
            LearningRecord(
                wordId = c.getLong(0),
                word = c.getString(1),
                easeFactor = c.getDouble(2),
                intervalSeconds = c.getLong(3),
                repetitions = c.getInt(4),
                memoryStrength = c.getInt(5),
                nextReviewTime = c.getLong(6),
                lastReviewTime = c.getLong(7),
                totalReviews = c.getInt(8),
                correctReviews = c.getInt(9),
                consecutiveFailures = c.getInt(10),
                mastered = c.getInt(11) == 1,
            )
        }
    }

    fun recordAnswer(word: WordEntry, rating: Rating, timeTakenSeconds: Int) {
        val db = dbHelper.writableDatabase
        val now = System.currentTimeMillis()
        val previous = getLearningRecord(word.id)
        val update = SpacedRepetition.applyRating(now, previous, rating)

        // 新单词首次答对（Hard/Know）直接跳到强度40，加速熟练掌握
        val isFirstCorrectBonus = previous == null && (rating == Rating.Hard || rating == Rating.Know)
        val finalStrength = if (isFirstCorrectBonus) 40 else update.memoryStrength
        // 同步调整 repetitions 和 interval 以匹配强度40（=4次正确复习）
        val finalRepetitions = if (isFirstCorrectBonus) {
            SpacedRepetition.strengthToRepetitions(40)
        } else {
            update.repetitions
        }
        val finalInterval = if (isFirstCorrectBonus) {
            SpacedRepetition.intervalsSeconds[(finalRepetitions - 1).coerceIn(0, SpacedRepetition.intervalsSeconds.lastIndex)]
        } else {
            update.intervalSeconds
        }
        val finalNextReviewTime = if (isFirstCorrectBonus) now + finalInterval * 1000L else update.nextReviewTime

        val today = todayKey()

        db.beginTransaction()
        try {
            // upsert learning record
            val cv = ContentValues().apply {
                put("word_id", word.id)
                put("word", word.word)
                put("ease_factor", previous?.easeFactor ?: 2.5)
                put("interval", finalInterval)
                put("repetitions", finalRepetitions)
                put("memory_strength", finalStrength)
                put("next_review_time", finalNextReviewTime)
                put("last_review_time", now)
                put("total_reviews", update.totalReviews)
                put("correct_reviews", update.correctReviews)
                put("consecutive_failures", update.consecutiveFailures)
                put("mastered", if (update.mastered) 1 else 0)
                put("created_at", previous?.let { now } ?: now)
                put("updated_at", now)
            }
            db.insertWithOnConflict("learning_records", null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)

            // review history
            val history = ContentValues().apply {
                put("word_id", word.id)
                put("rating", rating.value)
                put("review_time", now)
                put("time_taken", timeTakenSeconds)
            }
            db.insert("review_history", null, history)

            // daily stats
            ensureDailyRow(db, today)
            if (previous == null) {
                // 第一次答题认为是“新词”
                db.execSQL("UPDATE daily_stats SET new_words=new_words+1 WHERE date=?", arrayOf(today))
            } else {
                // 进入学习记录后都算复习统计（短期5秒重现也算一次尝试）
                // 去重：同一个单词一天内多次复习只计1次
                val hasReviewedToday = db.rawQuery(
                    "SELECT COUNT(1) FROM review_history WHERE word_id=? AND date(review_time/1000,'unixepoch')=?",
                    arrayOf(word.id.toString(), today),
                ).useFirstInt() > 1
                if (!hasReviewedToday) {
                    db.execSQL("UPDATE daily_stats SET review_words=review_words+1 WHERE date=?", arrayOf(today))
                }
            }
            if (update.correct) {
                db.execSQL("UPDATE daily_stats SET correct_count=correct_count+1 WHERE date=?", arrayOf(today))
            }
            db.execSQL("UPDATE daily_stats SET total_time=total_time+? WHERE date=?", arrayOf(timeTakenSeconds, today))

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun ensureDailyRow(db: android.database.sqlite.SQLiteDatabase, date: String) {
        val exists = db.rawQuery("SELECT COUNT(1) FROM daily_stats WHERE date=?", arrayOf(date)).useFirstInt() > 0
        if (exists) return
        val cv = ContentValues().apply { put("date", date) }
        db.insert("daily_stats", null, cv)
    }

    private fun getSelectedWordbookIds(db: android.database.sqlite.SQLiteDatabase): List<Long> {
        return db.rawQuery(
            "SELECT wordbook_id FROM user_wordbooks WHERE selected=1 ORDER BY wordbook_id ASC",
            null,
        ).use { c ->
            val ids = mutableListOf<Long>()
            while (c.moveToNext()) ids.add(c.getLong(0))
            ids
        }
    }

    private fun calcStreakDays(db: android.database.sqlite.SQLiteDatabase): Int {
        // 获取所有有学习记录的日期（按日期降序）
        val dates = db.rawQuery(
            "SELECT date FROM daily_stats WHERE total_time > 0 ORDER BY date DESC",
            null,
        ).use { c ->
            val list = mutableListOf<String>()
            while (c.moveToNext()) list.add(c.getString(0))
            list
        }
        if (dates.isEmpty()) return 0

        val today = todayKey()
        val yesterday = previousDayKey(today)

        // 最近的学习日期必须是今天或昨天，否则连续学习已中断
        if (dates[0] != today && dates[0] != yesterday) return 0

        var streak = 0
        var expected = dates[0] // 从最近的学习日开始往前推算
        for (date in dates) {
            if (date != expected) break // 出现空缺 → 中断
            streak++
            expected = previousDayKey(expected)
        }
        return streak
    }

    private fun todayKey(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return fmt.format(Date())
    }

    private fun previousDayKey(dateKey: String): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d = fmt.parse(dateKey) ?: return dateKey
        val prev = Date(d.time - 24 * 60 * 60 * 1000L)
        return fmt.format(prev)
    }

    private fun Cursor.getStringOrNull(index: Int): String? =
        if (isNull(index)) null else getString(index)

    private fun Cursor.toWordEntry(): WordEntry =
        WordEntry(
            id = getLong(0),
            wordbookId = getLong(1),
            word = getString(2),
            phonetic = getStringOrNull(3),
            translation = getStringOrNull(4),
            definition = getStringOrNull(5),
            example = getStringOrNull(6),
            phrases = getStringOrNull(7),
            roots = getStringOrNull(8),
            synonyms = getStringOrNull(9),
            antonyms = getStringOrNull(10),
        )

    private fun Cursor.useFirstInt(): Int {
        use {
            if (!moveToFirst()) return 0
            return getInt(0)
        }
    }

    fun getMemoryDistribution(): Triple<Int, Int, Int> {
        val db = dbHelper.readableDatabase
        val forgotten = db.rawQuery(
            "SELECT COUNT(1) FROM learning_records WHERE memory_strength = 0 AND mastered = 0",
            null,
        ).useFirstInt()
        val mastered = db.rawQuery(
            "SELECT COUNT(1) FROM learning_records WHERE mastered = 1 OR memory_strength = 100",
            null,
        ).useFirstInt()
        val totalLearned = db.rawQuery(
            "SELECT COUNT(1) FROM learning_records",
            null,
        ).useFirstInt()
        val learning = totalLearned - forgotten - mastered
        return Triple(forgotten, learning.coerceAtLeast(0), mastered)
    }

    fun getUpcomingReviews(limit: Int = 10): List<Pair<WordEntry, LearningRecord>> {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT w.id, w.wordbook_id, w.word, w.phonetic, w.translation, w.definition, w.example_sentence,
                   w.phrases, w.roots, w.synonyms, w.antonyms,
                   lr.word_id, lr.word, lr.ease_factor, lr.interval, lr.repetitions, lr.memory_strength,
                   COALESCE(lr.next_review_time, 0), COALESCE(lr.last_review_time, 0),
                   lr.total_reviews, lr.correct_reviews, lr.consecutive_failures, lr.mastered
            FROM words w
            JOIN learning_records lr ON lr.word_id = w.id
            WHERE lr.memory_strength < 100
              AND lr.next_review_time IS NOT NULL
              AND lr.next_review_time != 0
            ORDER BY
              CASE WHEN lr.memory_strength < 40 THEN 0
                   WHEN lr.memory_strength < 60 THEN 1
                   ELSE 2
              END,
              lr.next_review_time ASC
            LIMIT ?
        """.trimIndent()
        return db.rawQuery(sql, arrayOf(limit.toString())).use { c ->
            val list = mutableListOf<Pair<WordEntry, LearningRecord>>()
            while (c.moveToNext()) {
                val word = WordEntry(
                    id = c.getLong(0),
                    wordbookId = c.getLong(1),
                    word = c.getString(2),
                    phonetic = c.getStringOrNull(3),
                    translation = c.getStringOrNull(4),
                    definition = c.getStringOrNull(5),
                    example = c.getStringOrNull(6),
                    phrases = c.getStringOrNull(7),
                    roots = c.getStringOrNull(8),
                    synonyms = c.getStringOrNull(9),
                    antonyms = c.getStringOrNull(10),
                )
                val rec = LearningRecord(
                    wordId = c.getLong(11),
                    word = c.getString(12),
                    easeFactor = c.getDouble(13),
                    intervalSeconds = c.getLong(14),
                    repetitions = c.getInt(15),
                    memoryStrength = c.getInt(16),
                    nextReviewTime = c.getLong(17),
                    lastReviewTime = c.getLong(18),
                    totalReviews = c.getInt(19),
                    correctReviews = c.getInt(20),
                    consecutiveFailures = c.getInt(21),
                    mastered = c.getInt(22) == 1,
                )
                list.add(word to rec)
            }
            list
        }
    }

    fun undoLastAnswer(wordId: Long) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1) 获取最后一条 review_history 的时间和数据
            val lastReview = db.rawQuery(
                """
                SELECT time_taken, rating
                FROM review_history
                WHERE word_id=?
                ORDER BY review_time DESC LIMIT 1
                """.trimIndent(),
                arrayOf(wordId.toString()),
            ).use { c ->
                if (!c.moveToFirst()) null
                else Pair(c.getInt(0), c.getInt(1))
            }

            if (lastReview != null) {
                val today = todayKey()
                val (timeTaken, rating) = lastReview
                // 减去记录的时间
                if (timeTaken > 0) {
                    db.execSQL("UPDATE daily_stats SET total_time = MAX(0, total_time - ?) WHERE date=?", arrayOf(timeTaken, today))
                }
                // 检查这是新词还是复习：如果 review_history 中只有这一条记录，说明第一次学 → 新词
                val totalEntries = db.rawQuery(
                    "SELECT COUNT(1) FROM review_history WHERE word_id=?",
                    arrayOf(wordId.toString()),
                ).useFirstInt()
                val isNewWord = totalEntries <= 1

                if (isNewWord) {
                    db.execSQL("UPDATE daily_stats SET new_words = MAX(0, new_words - 1) WHERE date=?", arrayOf(today))
                } else {
                    db.execSQL("UPDATE daily_stats SET review_words = MAX(0, review_words - 1) WHERE date=?", arrayOf(today))
                }
                // 如果是正确评分（Hard=3, Know=4, Fluent=5），减去正确计数
                if (rating >= 3) {
                    db.execSQL("UPDATE daily_stats SET correct_count = MAX(0, correct_count - 1) WHERE date=?", arrayOf(today))
                }
            }

            db.delete("review_history", "word_id=?", arrayOf(wordId.toString()))
            db.delete("learning_records", "word_id=?", arrayOf(wordId.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ── 新增：本周统计 ──
    data class WeeklyStats(
        val newWords: Int,
        val reviewWords: Int,
        val correctCount: Int,
        val totalTimeSec: Int,
    ) {
        val correctRate: Int get() = if (reviewWords > 0) (correctCount * 100 / reviewWords) else 0
    }

    fun getWeeklyStats(): WeeklyStats {
        val db = dbHelper.readableDatabase
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val monday = String.format(
            "%04d-%02d-%02d",
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH),
        )
        return db.rawQuery(
            """
            SELECT 
              COALESCE(SUM(new_words), 0),
              COALESCE(SUM(review_words), 0),
              COALESCE(SUM(correct_count), 0),
              COALESCE(SUM(total_time), 0)
            FROM daily_stats WHERE date >= ?
            """.trimIndent(),
            arrayOf(monday),
        ).use { c ->
            c.moveToFirst()
            WeeklyStats(
                newWords = c.getInt(0),
                reviewWords = c.getInt(1),
                correctCount = c.getInt(2),
                totalTimeSec = c.getInt(3),
            )
        }
    }

    data class DailyStatsItem(val date: String, val newWords: Int, val reviewWords: Int, val totalTimeSec: Int)

    fun getDailyStatsForDays(days: Int = 7): List<DailyStatsItem> {
        val db = dbHelper.readableDatabase
        val cal = java.util.Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        cal.add(java.util.Calendar.DAY_OF_YEAR, -(days - 1))
        val startDate = fmt.format(cal.time)

        // 初始化每一天
        val map = linkedMapOf<String, DailyStatsItem>()
        cal.time = fmt.parse(startDate)!!
        for (i in 0 until days) {
            val date = fmt.format(cal.time)
            map[date] = DailyStatsItem(date, 0, 0, 0)
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        db.rawQuery(
            "SELECT date, new_words, review_words, total_time FROM daily_stats WHERE date >= ? ORDER BY date",
            arrayOf(startDate),
        ).use { c ->
            while (c.moveToNext()) {
                val date = c.getString(0)
                map[date] = DailyStatsItem(
                    date = date,
                    newWords = c.getInt(1),
                    reviewWords = c.getInt(2),
                    totalTimeSec = c.getInt(3),
                )
            }
        }
        return map.values.toList()
    }

    // ── 新增：已学单词列表 ──
    data class LearnedWordItem(val word: WordEntry, val memoryStrength: Int, val totalReviews: Int, val mastered: Boolean)

    fun getLearnedWords(limit: Int = 50): List<LearnedWordItem> {
        val db = dbHelper.readableDatabase
        return db.rawQuery(
            """
            SELECT w.id, w.wordbook_id, w.word, w.phonetic, w.translation, w.definition, w.example_sentence,
                   w.phrases, w.roots, w.synonyms, w.antonyms,
                   lr.memory_strength, lr.total_reviews, lr.mastered
            FROM words w
            JOIN learning_records lr ON lr.word_id = w.id
            ORDER BY lr.last_review_time DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(limit.toString()),
        ).use { c ->
            val list = mutableListOf<LearnedWordItem>()
            while (c.moveToNext()) {
                val word = WordEntry(
                    id = c.getLong(0), wordbookId = c.getLong(1), word = c.getString(2),
                    phonetic = c.getStringOrNull(3), translation = c.getStringOrNull(4),
                    definition = c.getStringOrNull(5), example = c.getStringOrNull(6),
                    phrases = c.getStringOrNull(7), roots = c.getStringOrNull(8),
                    synonyms = c.getStringOrNull(9), antonyms = c.getStringOrNull(10),
                )
                list.add(
                    LearnedWordItem(
                        word = word,
                        memoryStrength = c.getInt(11),
                        totalReviews = c.getInt(12),
                        mastered = c.getInt(13) == 1,
                    )
                )
            }
            list
        }
    }

    // ── 新增：已掌握单词列表 ──
    fun getMasteredWords(limit: Int = 50): List<LearnedWordItem> {
        val db = dbHelper.readableDatabase
        return db.rawQuery(
            """
            SELECT w.id, w.wordbook_id, w.word, w.phonetic, w.translation, w.definition, w.example_sentence,
                   w.phrases, w.roots, w.synonyms, w.antonyms,
                   lr.memory_strength, lr.total_reviews, lr.mastered
            FROM words w
            JOIN learning_records lr ON lr.word_id = w.id
            WHERE lr.mastered = 1 OR lr.memory_strength = 100
            ORDER BY lr.last_review_time DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(limit.toString()),
        ).use { c ->
            val list = mutableListOf<LearnedWordItem>()
            while (c.moveToNext()) {
                val word = WordEntry(
                    id = c.getLong(0), wordbookId = c.getLong(1), word = c.getString(2),
                    phonetic = c.getStringOrNull(3), translation = c.getStringOrNull(4),
                    definition = c.getStringOrNull(5), example = c.getStringOrNull(6),
                    phrases = c.getStringOrNull(7), roots = c.getStringOrNull(8),
                    synonyms = c.getStringOrNull(9), antonyms = c.getStringOrNull(10),
                )
                list.add(
                    LearnedWordItem(
                        word = word,
                        memoryStrength = c.getInt(11),
                        totalReviews = c.getInt(12),
                        mastered = c.getInt(13) == 1,
                    )
                )
            }
            list
        }
    }
}
