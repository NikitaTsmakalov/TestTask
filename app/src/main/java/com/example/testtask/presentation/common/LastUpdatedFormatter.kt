package com.example.testtask.presentation.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatLastUpdated(template: String, timestampMs: Long): String {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestampMs))
    return String.format(template, time)
}

