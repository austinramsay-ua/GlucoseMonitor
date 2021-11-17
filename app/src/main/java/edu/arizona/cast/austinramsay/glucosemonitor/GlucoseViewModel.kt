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

    private val glucoseRepository = GlucoseRepository.get()
    val glucoseList = glucoseRepository.getGlucoseList()

    // When the glucose date is changed,the view model will automatically query the database
    // and retrieve the corresponding object stored in the database
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
    fun sync(source: Glucose) {

        // Update the UI input and output field colors to match the calculated glucose value status (normal, abnormal, etc)
        fastingColor = when(GlucoseCalculator.getFastingStatus(source.fasting)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        breakfastColor = when(GlucoseCalculator.getBreakfastStatus(source.breakfast)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        lunchColor = when(GlucoseCalculator.getLunchStatus(source.lunch)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        dinnerColor = when(GlucoseCalculator.getDinnerStatus(source.dinner)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
    }
}