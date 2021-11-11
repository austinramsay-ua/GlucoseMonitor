package edu.arizona.cast.austinramsay.glucosemonitor

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "GlucoseViewModel"

class GlucoseViewModel : ViewModel() {

    // Data values
    private val glucoseRepository = GlucoseRepository.get()
    val glucoseList = glucoseRepository.getGlucoseList()
    private val glucoseDateLiveData = MutableLiveData<Date>()
    var glucose: LiveData<Glucose?> =
        Transformations.switchMap(glucoseDateLiveData) { glucoseDate ->
            glucoseRepository.getGlucose(glucoseDate)
        }

    // UI values
    val defaultColor = Color.GRAY
    var fastingColor: Int = defaultColor
    var breakfastColor: Int = defaultColor
    var lunchColor: Int = defaultColor
    var dinnerColor: Int = defaultColor
    val dateFormatterFull = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")

    fun loadGlucose(glucoseDate: Date) {
        glucoseDateLiveData.value = glucoseDate
    }

    fun saveGlucose(glucose: Glucose) {
        glucoseRepository.updateGlucose(glucose)
    }

    // TODO: modify this for livedata, don't need to update manually anymore but still need colors
    // If the object already has a date, use it, otherwise stamp it now
    fun sync() {

        // Update the UI input and output field colors to match the calculated glucose value status (normal, abnormal, etc)
        fastingColor = when(GlucoseCalculator.getFastingStatus(glucose.value?.fasting )) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        breakfastColor = when(GlucoseCalculator.getBreakfastStatus(glucose.value?.breakfast)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        lunchColor = when(GlucoseCalculator.getLunchStatus(glucose.value?.lunch)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        dinnerColor = when(GlucoseCalculator.getDinnerStatus(glucose.value?.dinner)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
    }
}