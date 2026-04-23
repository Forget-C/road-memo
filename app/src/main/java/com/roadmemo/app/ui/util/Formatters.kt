package com.roadmemo.app.ui.util

import com.roadmemo.app.domain.model.Money
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun Money.toCurrencyText(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return formatter.format(amountInCent / 100.0)
}

fun Instant.toDateText(): String = dateFormatter.format(atZone(ZoneId.systemDefault()).toLocalDate())

fun Instant.toDateTimeText(): String = dateTimeFormatter.format(atZone(ZoneId.systemDefault()).toLocalDateTime())
