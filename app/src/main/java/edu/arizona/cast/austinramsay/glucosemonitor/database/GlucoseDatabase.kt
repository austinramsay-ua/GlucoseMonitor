package edu.arizona.cast.austinramsay.glucosemonitor.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.arizona.cast.austinramsay.glucosemonitor.Glucose

@Database(entities = [Glucose::class], version = 1)
@TypeConverters(GlucoseTypeConverters::class)
abstract class GlucoseDatabase : RoomDatabase() {
    abstract fun glucoseDao(): GlucoseDAO
}
