package edu.arizona.cast.austinramsay.glucosemonitor

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Glucose(@PrimaryKey var date: Date = GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR),
    Calendar.getInstance().get(Calendar.MONTH),
    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).time,
                   var fasting: Int = -1,
                   var breakfast: Int = -1,
                   var lunch: Int = -1,
                   var dinner: Int = -1) {

    var fastingStatus: String?
    var breakfastStatus: String?
    var lunchStatus: String?
    var dinnerStatus: String?
    var average: Int?
    var overallStatus: String?

    companion object {
        const val STATUS_NORMAL = "Normal"
        const val STATUS_ABNORMAL = "Abnormal"
        const val STATUS_HYPOGLYCEMIC= "Hypoglycemic"
        const val STATUS_NONE = "No Value"

        fun getRandomGlucoseLevel(): Int {
            // Return a reasonable glucose level, which would be between 60 and 180
            return (60..180).random()
        }
    }

    // Upon instance creation, calculate the status properties using the provided glucose numbers
    // If fasting levels are less than 70 or above 100 the levels are abnormal
    // For between meals, values should be between 70 and 140
    init {
        // Set the fasting status
        fastingStatus = when {
            (fasting == 0) -> STATUS_NONE
            (fasting < 70) -> STATUS_HYPOGLYCEMIC
            (fasting in 70..99) -> STATUS_NORMAL
            else -> STATUS_ABNORMAL
        }

        breakfastStatus = when {
            (breakfast == 0) -> STATUS_NONE
            (breakfast < 70) -> STATUS_HYPOGLYCEMIC
            (breakfast > 140) -> STATUS_ABNORMAL
            else -> STATUS_NORMAL
        }

        lunchStatus = when {
            (lunch == 0) -> STATUS_NONE
            (lunch < 70) -> STATUS_HYPOGLYCEMIC
            (lunch > 140) -> STATUS_ABNORMAL
            else -> STATUS_NORMAL
        }

        dinnerStatus = when {
            (dinner == 0) -> STATUS_NONE
            (dinner < 70) -> STATUS_HYPOGLYCEMIC
            (dinner > 140) -> STATUS_ABNORMAL
            else -> STATUS_NORMAL
        }

        // Set the overall average values
        average = ((fasting + breakfast + lunch + dinner) / 4)

        overallStatus = when {
            (average!! < 70) -> STATUS_HYPOGLYCEMIC
            (average!! > 140) -> STATUS_ABNORMAL
            else -> STATUS_NORMAL
        }
    }
}
