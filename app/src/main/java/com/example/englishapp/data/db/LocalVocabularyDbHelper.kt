package com.example.englishapp.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * 按《功能介绍.md》的结构实现（做了少量字段补充，如 mastered）。
 *
 * 说明：
 * - 目前仍保持“完全本地化”，不依赖 Room，避免额外依赖下载带来的环境问题。
 */
class LocalVocabularyDbHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE settings (
                key TEXT PRIMARY KEY,
                value TEXT
            );
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE wordbooks (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT,
                word_count INTEGER DEFAULT 0,
                source TEXT,
                created_at INTEGER
            );
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE words (
                id INTEGER PRIMARY KEY,
                wordbook_id INTEGER,
                word TEXT NOT NULL,
                phonetic TEXT,
                definition TEXT,
                translation TEXT,
                example_sentence TEXT,
                phrases TEXT,
                roots TEXT,
                synonyms TEXT,
                antonyms TEXT,
                FOREIGN KEY (wordbook_id) REFERENCES wordbooks(id)
            );
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE learning_records (
                word_id INTEGER PRIMARY KEY,
                word TEXT,
                ease_factor REAL DEFAULT 2.5,
                interval INTEGER DEFAULT 0,
                repetitions INTEGER DEFAULT 0,
                memory_strength INTEGER DEFAULT 0,
                next_review_time INTEGER,
                last_review_time INTEGER,
                total_reviews INTEGER DEFAULT 0,
                correct_reviews INTEGER DEFAULT 0,
                consecutive_failures INTEGER DEFAULT 0,
                mastered INTEGER DEFAULT 0,
                created_at INTEGER,
                updated_at INTEGER
            );
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE user_wordbooks (
                wordbook_id INTEGER PRIMARY KEY,
                selected INTEGER DEFAULT 0,
                updated_at INTEGER
            );
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE daily_stats (
                date TEXT PRIMARY KEY,
                new_words INTEGER DEFAULT 0,
                review_words INTEGER DEFAULT 0,
                correct_count INTEGER DEFAULT 0,
                total_time INTEGER DEFAULT 0
            );
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE review_history (
                id INTEGER PRIMARY KEY,
                word_id INTEGER,
                rating INTEGER,
                review_time INTEGER,
                time_taken INTEGER
            );
            """.trimIndent(),
        )

        db.execSQL("CREATE INDEX idx_words_wordbook_id ON words(wordbook_id);")
        db.execSQL("CREATE INDEX idx_learning_next_review ON learning_records(next_review_time);")
        db.execSQL("CREATE INDEX idx_history_word_id ON review_history(word_id);")
        db.execSQL("CREATE INDEX idx_history_review_time ON review_history(review_time);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // MVP阶段：直接重建（后续可做数据迁移）
        db.execSQL("DROP TABLE IF EXISTS review_history")
        db.execSQL("DROP TABLE IF EXISTS daily_stats")
        db.execSQL("DROP TABLE IF EXISTS user_wordbooks")
        db.execSQL("DROP TABLE IF EXISTS learning_records")
        db.execSQL("DROP TABLE IF EXISTS words")
        db.execSQL("DROP TABLE IF EXISTS wordbooks")
        db.execSQL("DROP TABLE IF EXISTS settings")
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "local_vocabulary.db"
        private const val DB_VERSION = 1
    }
}

