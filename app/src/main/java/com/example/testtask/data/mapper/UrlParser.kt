package com.example.testtask.data.mapper

fun extractIdFromUrl(url: String): Int? {
    val cleaned = url.trim().trimEnd('/')
    return cleaned.substringAfterLast('/', "").toIntOrNull()
}
