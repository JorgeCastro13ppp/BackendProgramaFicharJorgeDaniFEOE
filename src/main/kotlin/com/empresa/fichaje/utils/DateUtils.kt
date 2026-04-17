package com.empresa.fichaje.utils

import java.time.LocalDate
import java.time.ZoneId

fun todayRange(): Pair<Long, Long> {

    val start =
        LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    val end =
        LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    return start to end
}