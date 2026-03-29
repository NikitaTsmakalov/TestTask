package com.example.testtask.presentation.search

internal fun globalSearchRelevanceScore(title: String, query: String): Long {
    val t = title.lowercase()
    val q = query.trim().lowercase()
    if (q.isEmpty()) return 0L

    if (t.startsWith(q)) {
        return 10_000_000L + minOf(q.length, 32) * 1_000L
    }

    val tokens = t.split(WORD_SPLIT_REGEX).filter { it.isNotEmpty() }
    val wordIndex = tokens.indexOfFirst { it.startsWith(q) }
    if (wordIndex >= 0) {
        return 5_000_000L - wordIndex * 50_000L
    }

    val firstIdx = t.indexOf(q)
    if (firstIdx >= 0) {
        var occ = 0
        var from = 0
        while (from <= t.length - q.length) {
            val i = t.indexOf(q, from)
            if (i < 0) break
            occ++
            from = i + q.length
        }
        return 1_000_000L -
            firstIdx * 2_000L +
            minOf(occ, 20) * 100L
    }

    return 0L
}

internal fun List<GlobalSearchItem>.sortedBySearchRelevance(query: String): List<GlobalSearchItem> {
    val q = query.trim()
    if (q.isEmpty()) return this
    return sortedWith(
        compareByDescending<GlobalSearchItem> { globalSearchRelevanceScore(it.title, q) }
            .thenBy { it.title.lowercase() },
    )
}

private val WORD_SPLIT_REGEX = Regex("[^a-z0-9а-яё]+", RegexOption.IGNORE_CASE)
