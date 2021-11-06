package edu.arizona.cast.austinramsay.glucosemonitor.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.lifecycle.LiveData
import java.util.*
import edu.arizona.cast.austinramsay.glucosemonitor.Glucose

@Dao
interface GlucoseDAO {

    @Query("SELECT * FROM glucose")
    fun getGlucoseList(): LiveData<List<Glucose>>

    @Query("SELECT * FROM glucose WHERE date=(:date)")
    fun getGlucose(date: Long): LiveData<Glucose?>
}