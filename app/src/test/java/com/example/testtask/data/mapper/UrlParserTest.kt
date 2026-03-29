package com.example.testtask.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlParserTest {
    @Test
    fun `extracts numeric id from url`() {
        assertEquals(1, extractIdFromUrl("https://swapi.dev/api/people/1/"))
    }

    @Test
    fun `returns null for invalid url`() {
        assertNull(extractIdFromUrl("not-valid"))
    }
}
