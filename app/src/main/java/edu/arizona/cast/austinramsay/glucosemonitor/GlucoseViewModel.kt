package edu.arizona.cast.austinramsay.glucosemonitor

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "GlucoseViewModel"

class GlucoseViewModel : ViewModel() {

    var glucose = MutableLiveData<Glucose>()
    val defaultColor = Color.GRAY
    var fastingColor: Int = defaultColor
    var breakfastColor: Int = defaultColor
    var lunchColor: Int = defaultColor
    var dinnerColor: Int = defaultColor
    val dateFormatterMDY = DateTimeFormatter.ofPattern("MMM dd, YYYY")
    val dateFormatterFull = DateTimeFormatter.ofPattern("MMM dd, YYYY HH:mm:ss")

    // Update the view model's glucose object
    // If the object already has a date, use it, otherwise stamp it now
    fun updateGlucose(date: LocalDateTime = glucose.value?.date ?: LocalDateTime.now(),
                      fasting: Int,
                      breakfast: Int,
                      lunch: Int,
                      dinner: Int) {

        // Update the view model glucose object according to provided arguments
        val newGlucose = Glucose(
            date = date,
            fasting = fasting,
            breakfast = breakfast,
            lunch = lunch,
            dinner = dinner)

        // Update the UI input and output field colors to match the calculated glucose value status (normal, abnormal, etc)
        fastingColor = when(newGlucose.fastingStatus) {
            Glucose.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            Glucose.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        breakfastColor = when(newGlucose.breakfastStatus) {
            Glucose.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            Glucose.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        lunchColor = when(newGlucose.lunchStatus) {
            Glucose.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            Glucose.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        dinnerColor = when(newGlucose.dinnerStatus) {
            Glucose.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            Glucose.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }

        // Update the main glucose object to the new updated object (causes observers to see event)
        this.glucose.value = newGlucose
    }
}