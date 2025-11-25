package com.dakh.prettynotes.presentation.utils

import android.icu.text.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dakh.prettynotes.R
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

object DateFormatter {

    private val millisInHour = TimeUnit.HOURS.toMillis(1)
    private val millisInDay = TimeUnit.DAYS.toMillis(1)
    val formatter = SimpleDateFormat.getDateInstance(DateFormat.SHORT)

    @Composable
    fun formatDateToString(timeStamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeStamp

        return when {
            diff < millisInHour -> stringResource(R.string.just_now)
            diff < millisInDay -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                stringResource(R.string.h_ago, hours)
            }
            else -> {
                formatter.format(timeStamp)
            }
        }
    }

    fun formatCurrentDate(): String {
        return formatter.format(System.currentTimeMillis())
    }
}