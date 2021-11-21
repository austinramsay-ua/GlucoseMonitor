package edu.arizona.cast.austinramsay.glucosemonitor.database

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*
import edu.arizona.cast.austinramsay.glucosemonitor.Glucose

@Dao
interface GlucoseDAO {

    @Query("SELECT * FROM glucose")
    fun getGlucoseList(): LiveData<List<Glucose>>

    @Query("SELECT * FROM glucose WHERE date=(:date)")
    fun getGlucose(date: Date): LiveData<Glucose?>

    @Update
    fun updateGlucose(glucose: Glucose)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addGlucose(glucose: Glucose)
}