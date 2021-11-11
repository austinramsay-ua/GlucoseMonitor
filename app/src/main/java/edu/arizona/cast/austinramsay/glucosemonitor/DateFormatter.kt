package edu.arizona.cast.austinramsay.glucosemonitor

import java.text.SimpleDateFormat
import java.util.*

class DateFormatter {
    companion object {
        fun format(date: Date): String {
            val fmt = SimpleDateFormat("MMM dd, yyyy")
            val formattedDate = fmt.format(date)

            return formattedDate
        }
    }
}