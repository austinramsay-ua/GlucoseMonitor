package edu.arizona.cast.austinramsay.glucosemonitor

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "GlucoseViewModel"

class DBViewModel : ViewModel() {

    private val glucoseRepository = GlucoseRepository.get()
    val glucoseList = glucoseRepository.getGlucoseList()

    // When the glucose date is changed,the view model will automatically query the database
    // and retrieve the corresponding object stored in the database
    private val glucoseDateLiveData = MutableLiveData<Date>()
    var glucose: LiveData<Glucose?> =
        Transformations.switchMap(glucoseDateLiveData) { glucoseDate ->
            glucoseRepository.getGlucose(glucoseDate)
        }

    fun checkExists(date: Date) = glucoseRepository.checkExists(date)

    fun loadGlucose(glucoseDate: Date) {
        glucoseDateLiveData.value = glucoseDate
    }

    fun updateGlucose(glucose: Glucose) {
        glucoseRepository.updateGlucose(glucose)
    }

    fun addGlucose(glucose: Glucose) {
        glucoseRepository.addGlucose(glucose)
    }

    fun deleteGlucose(glucose: Glucose) {
        glucoseRepository.deleteGlucose(glucose)
    }
}