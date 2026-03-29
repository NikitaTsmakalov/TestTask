package com.example.testtask.presentation.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalSearchRelevanceTest {

    @Test
    fun `prefix of name ranks above substring only`() {
        val q = "h"
        val han = globalSearchRelevanceScore("Han Solo", q)
        val vader = globalSearchRelevanceScore("Darth Vader", q)
        assertTrue("Han should rank above Vader for 'h'", han > vader)
    }

    @Test
    fun `name starting with letter ranks above name with letter only inside first word`() {
        val q = "h"
        val han = globalSearchRelevanceScore("Han Solo", q)
        val sheev = globalSearchRelevanceScore("Sheev Palpatine", q)
        assertTrue(han > sheev)
    }

    @Test
    fun `sorted list puts Han before Darth when query is h`() {
        val items = listOf(
            GlobalSearchItem("character", 1, "Darth Vader", "Character"),
            GlobalSearchItem("character", 2, "Han Solo", "Character"),
            GlobalSearchItem("character", 3, "Jar Jar Binks", "Character"),
        )
        val sorted = items.sortedBySearchRelevance("h")
        assertEquals("Han Solo", sorted[0].title)
    }
}
