package edu.arizona.cast.austinramsay.glucosemonitor

import java.text.SimpleDateFormat
import java.util.*

class DateFormatter {
    companion object {
        fun formatShort(date: Date): String {
            val fmt = SimpleDateFormat("MMM dd, yyyy")
            val formattedDate = fmt.format(date)

            return formattedDate
        }

        fun formatLong(date: Date): String {
            val fmt = SimpleDateFormat("EEEE, MMMM dd, yyyy")
            val formattedDate = fmt.format(date)

            return formattedDate
        }
    }
}