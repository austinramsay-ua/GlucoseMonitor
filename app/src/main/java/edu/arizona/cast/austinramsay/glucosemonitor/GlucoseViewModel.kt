package edu.arizona.cast.austinramsay.glucosemonitor

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "GlucoseViewModel"

class GlucoseViewModel : ViewModel() {

    private val glucoseRepository = GlucoseRepository.get()
    val glucoseList = glucoseRepository.getGlucoseList()
    var glucose = MutableLiveData<Glucose>()
    var glucoseHistory: List<Glucose> = listOf()
    val defaultColor = Color.GRAY
    var fastingColor: Int = defaultColor
    var breakfastColor: Int = defaultColor
    var lunchColor: Int = defaultColor
    var dinnerColor: Int = defaultColor
    val dateFormatterFull = DateTimeFormatter.ofPattern("MMM dd, YYYY HH:mm:ss")

    // Update the view model's glucose object
    // If the object already has a date, use it, otherwise stamp it now
    fun updateGlucose(date: Date = glucose.value?.date ?: GregorianCalendar(
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).time,
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

    // TODO: Remove this function after testing
    /*fun setRandomGlucose() {

        val today: LocalDateTime = LocalDateTime.now()

        // Begin creating objects at the 100th day prior to today
        val begin: LocalDateTime = today.minusDays(100)

        // Create a mutable list to hold our glucose objects
        val glucoseList = mutableListOf<Glucose>()

        // Create 100 random glucose objects offsetting the beginning date by the loop index
        for (offset in 0..99) {
            val randomGlucose = Glucose(
                date = begin.plusDays(offset.toLong()),
                fasting = Glucose.getRandomGlucoseLevel(),
                breakfast = Glucose.getRandomGlucoseLevel(),
                lunch = Glucose.getRandomGlucoseLevel(),
                dinner = Glucose.getRandomGlucoseLevel()
            )

            glucoseList.add(randomGlucose)
        }

        glucoseHistory = glucoseList
    }

     */
}