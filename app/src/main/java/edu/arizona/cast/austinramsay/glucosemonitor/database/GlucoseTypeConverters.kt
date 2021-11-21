package edu.arizona.cast.austinramsay.glucosemonitor.database

import androidx.room.TypeConverter
import java.util.*

class GlucoseTypeConverters {

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let {
            Date(it)
        }
    }
}