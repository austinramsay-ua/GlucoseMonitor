package edu.arizona.cast.austinramsay.glucosemonitor

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.ViewModel
import java.util.*

private const val TAG = "InputViewModel"

class InputViewModel : ViewModel() {

    // Local glucose object separated from the view model's glucose object
    var glucose: Glucose = Glucose()

    // UI values
    val defaultColor = Color.GRAY
    var fastingColor: Int = defaultColor
    var breakfastColor: Int = defaultColor
    var lunchColor: Int = defaultColor
    var dinnerColor: Int = defaultColor

    fun updateGlucoseDate(date: Date) {
        glucose.date = date
    }

    fun updateGlucoseLevels(fasting: Int, breakfast: Int, lunch: Int, dinner: Int) {
        glucose.fasting = fasting
        glucose.breakfast = breakfast
        glucose.lunch = lunch
        glucose.dinner = dinner

        // Update the UI input and output field colors to match the calculated glucose value status (normal, abnormal, etc)
        fastingColor = when(GlucoseCalculator.getFastingStatus(fasting)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        breakfastColor = when(GlucoseCalculator.getBreakfastStatus(breakfast)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        lunchColor = when(GlucoseCalculator.getLunchStatus(lunch)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
        dinnerColor = when(GlucoseCalculator.getDinnerStatus(dinner)) {
            GlucoseCalculator.STATUS_NORMAL -> Color.rgb(0, 140, 0)
            GlucoseCalculator.STATUS_NONE -> defaultColor
            else -> Color.rgb(170, 0, 0)
        }
    }
}