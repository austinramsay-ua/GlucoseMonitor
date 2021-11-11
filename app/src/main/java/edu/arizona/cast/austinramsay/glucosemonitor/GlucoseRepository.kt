package edu.arizona.cast.austinramsay.glucosemonitor

import android.content.Context
import androidx.room.Room
import androidx.lifecycle.LiveData
import edu.arizona.cast.austinramsay.glucosemonitor.database.GlucoseDatabase
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "glucose-database"

class GlucoseRepository private constructor(context: Context) {

    private val database : GlucoseDatabase = Room.databaseBuilder(
        context.applicationContext,
        GlucoseDatabase::class.java,
        DATABASE_NAME
    ).build()
    private val glucoseDao = database.glucoseDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getGlucoseList(): LiveData<List<Glucose>> = glucoseDao.getGlucoseList()

    fun getGlucose(date: Date): LiveData<Glucose?> = glucoseDao.getGlucose(date)

    fun updateGlucose(glucose: Glucose) {
        executor.execute {
            glucoseDao.updateGlucose(glucose)
        }
    }

    fun addGlucose(glucose: Glucose) {
        executor.execute {
            glucoseDao.addGlucose(glucose)
        }
    }

    companion object {
        private var INSTANCE: GlucoseRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = GlucoseRepository(context)
            }
        }

        fun get(): GlucoseRepository {
            return INSTANCE ?: throw IllegalStateException("GlucoseRepository must be initialized")
        }
    }
}