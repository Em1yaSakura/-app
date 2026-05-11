package com.example.englishapp.data.api

import org.json.JSONArray
import org.json.JSONObject

/**
 * Response from xxapi.cn English word APIs.
 * Both /api/englishwords and /api/randomenglishwords share this structure.
 */
data class WordApiResponse(
    val code: Int,
    val msg: String,
    val data: WordDetail?,
    val requestId: String?,
)

/** Detailed word info returned by the API. */
data class WordDetail(
    val word: String,
    val ukphone: String?,
    val usphone: String?,
    val ukspeech: String?,
    val usspeech: String?,
    val translations: List<Translation>,
    val phrases: List<Phrase>,
    val relWords: List<RelatedWord>,
    val synonyms: List<SynonymGroup>,
    val sentences: List<Sentence>,
)

data class Translation(val pos: String?, val tranCn: String?)
data class Phrase(val cn: String?, val content: String?)
data class RelatedWord(val words: List<RelWordItem>, val pos: String?)
data class RelWordItem(val word: String?, val tran: String?)
data class SynonymGroup(val words: List<SynonymWord>, val pos: String?, val tran: String?)
data class SynonymWord(val word: String?)
data class Sentence(val cn: String?, val content: String?)

// ── Top-level JSON parsing helpers (org.json, no external libs) ──

internal fun parseWordApiResponse(json: String): WordApiResponse {
    val root = JSONObject(json)
    return WordApiResponse(
        code = root.optInt("code", -1),
        msg = root.optString("msg", ""),
        data = if (root.has("data") && !root.isNull("data"))
            parseWordDetail(root.getJSONObject("data")) else null,
        requestId = root.optString("request_id", null),
    )
}

internal fun parseWordDetail(obj: JSONObject): WordDetail {
    fun safeArr(key: String): JSONArray = obj.optJSONArray(key) ?: JSONArray()

    return WordDetail(
        word = obj.optString("word", ""),
        ukphone = obj.optString("ukphone", null),
        usphone = obj.optString("usphone", null),
        ukspeech = obj.optString("ukspeech", null),
        usspeech = obj.optString("usspeech", null),
        translations = run {
            val list = mutableListOf<Translation>()
            val arr = safeArr("translations")
            for (i in 0 until arr.length()) {
                val t = arr.getJSONObject(i)
                list.add(Translation(pos = t.optString("pos", null), tranCn = t.optString("tran_cn", null)))
            }
            list
        },
        phrases = run {
            val list = mutableListOf<Phrase>()
            val arr = safeArr("phrases")
            for (i in 0 until arr.length()) {
                val p = arr.getJSONObject(i)
                list.add(Phrase(cn = p.optString("p_cn", null), content = p.optString("p_content", null)))
            }
            list
        },
        relWords = run {
            val list = mutableListOf<RelatedWord>()
            val arr = safeArr("relWords")
            for (i in 0 until arr.length()) {
                val r = arr.getJSONObject(i)
                val hwds = r.optJSONArray("Hwds") ?: JSONArray()
                val words = mutableListOf<RelWordItem>()
                for (j in 0 until hwds.length()) {
                    val h = hwds.getJSONObject(j)
                    words.add(RelWordItem(word = h.optString("hwd", null), tran = h.optString("tran", null)))
                }
                list.add(RelatedWord(words = words, pos = r.optString("Pos", null)))
            }
            list
        },
        synonyms = run {
            val list = mutableListOf<SynonymGroup>()
            val arr = safeArr("synonyms")
            for (i in 0 until arr.length()) {
                val s = arr.getJSONObject(i)
                val hwds = s.optJSONArray("Hwds") ?: JSONArray()
                val words = mutableListOf<SynonymWord>()
                for (j in 0 until hwds.length()) {
                    val h = hwds.getJSONObject(j)
                    words.add(SynonymWord(word = h.optString("word", null)))
                }
                list.add(SynonymGroup(words = words, pos = s.optString("pos", null), tran = s.optString("tran", null)))
            }
            list
        },
        sentences = run {
            val list = mutableListOf<Sentence>()
            val arr = safeArr("sentences")
            for (i in 0 until arr.length()) {
                val sn = arr.getJSONObject(i)
                list.add(Sentence(cn = sn.optString("s_cn", null), content = sn.optString("s_content", null)))
            }
            list
        },
    )
}
