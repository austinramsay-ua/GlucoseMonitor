package edu.arizona.cast.austinramsay.glucosemonitor

class GlucoseCalculator {

    companion object {
        const val STATUS_NORMAL = "Normal"
        const val STATUS_ABNORMAL = "Abnormal"
        const val STATUS_HYPOGLYCEMIC= "Hypoglycemic"
        const val STATUS_NONE = "No Value"

        fun getRandomGlucoseLevel(): Int {
            // Return a reasonable glucose level, which would be between 60 and 180
            return (60..180).random()
        }

        fun getFastingStatus(fasting: Int?): String {

            val f = fasting ?: 0
            return when {
                (f == 0) -> STATUS_NONE
                (f < 70) -> STATUS_HYPOGLYCEMIC
                (f in 70..99) -> STATUS_NORMAL
                else -> STATUS_ABNORMAL
            }
        }

        fun getBreakfastStatus(breakfast: Int?): String {

            val b = breakfast ?: 0
            return when {
                (b == 0) -> STATUS_NONE
                (b < 70) -> STATUS_HYPOGLYCEMIC
                (b > 140) -> STATUS_ABNORMAL
                else -> STATUS_NORMAL
            }
        }

        fun getLunchStatus(lunch: Int?): String {

            val l = lunch ?: 0
            return when {
                (l == 0) -> STATUS_NONE
                (l < 70) -> STATUS_HYPOGLYCEMIC
                (l > 140) -> STATUS_ABNORMAL
                else -> STATUS_NORMAL
            }
        }

        fun getDinnerStatus(dinner: Int?): String {

            val d = dinner ?: 0
            return when {
                (d == 0) -> STATUS_NONE
                (d < 70) -> STATUS_HYPOGLYCEMIC
                (d > 140) -> STATUS_ABNORMAL
                else -> STATUS_NORMAL
            }
        }

        fun getAverageStatus(fasting: Int?, breakfast: Int?, lunch: Int?, dinner: Int?): String {

            val f = fasting ?: 0
            val b = breakfast ?: 0
            val l = lunch ?: 0
            val d = dinner ?: 0

            val average = ((f + b + l + d) / 4)

            return when {
                (average < 70) -> STATUS_HYPOGLYCEMIC
                (average > 140) -> STATUS_ABNORMAL
                else -> STATUS_NORMAL
            }
        }

        fun getAverage(fasting: Int?, breakfast: Int?, lunch: Int?, dinner: Int?): Int {

            val f = fasting ?: 0
            val b = breakfast ?: 0
            val l = lunch ?: 0
            val d = dinner ?: 0

            return ((f + b + l + d) / 4)
        }
    }
}