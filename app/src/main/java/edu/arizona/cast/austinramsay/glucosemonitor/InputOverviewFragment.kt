package edu.arizona.cast.austinramsay.glucosemonitor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer

private const val TAG = "InputOverviewFragment"

class InputOverviewFragment : Fragment(R.layout.input_overview_fragment) {

    private lateinit var dateOutput: TextView
    private lateinit var fastingOutput: TextView
    private lateinit var breakfastOutput: TextView
    private lateinit var lunchOutput: TextView
    private lateinit var dinnerOutput: TextView

    private val glucoseViewModel: GlucoseViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.input_overview_fragment, container, false)

        dateOutput = view.findViewById(R.id.date_overview_label)
        fastingOutput = view.findViewById(R.id.fasting_overview_output)
        breakfastOutput = view.findViewById(R.id.breakfast_overview_output)
        lunchOutput = view.findViewById(R.id.lunch_overview_output)
        dinnerOutput = view.findViewById(R.id.dinner_overview_output)

        // When the view model glucose object is updated, sync with the view model values
        glucoseViewModel.glucose.observe(this, Observer {
            sync()
        })

        // Initial sync to view model data
        sync()

        return view
    }

    override fun onStart() {
        super.onStart()

        // Todo: maybe sync in the future?
    }

    // Set text labels according to glucose values in view model if they exist
    private fun sync() {
        dateOutput.text = when (glucoseViewModel.glucose.value) {
            null -> "No Glucose Input"
            else -> glucoseViewModel.glucose.value?.date?.format(glucoseViewModel.dateFormatterFull)
        }

        fastingOutput.text = when (glucoseViewModel.glucose.value) {
            null -> Glucose.STATUS_NONE
            else -> glucoseViewModel.glucose.value?.fastingStatus
        }

        breakfastOutput.text = when (glucoseViewModel.glucose.value) {
            null -> Glucose.STATUS_NONE
            else -> glucoseViewModel.glucose.value?.breakfastStatus
        }

        lunchOutput.text = when (glucoseViewModel.glucose.value) {
            null -> Glucose.STATUS_NONE
            else -> glucoseViewModel.glucose.value?.lunchStatus
        }

        dinnerOutput.text = when (glucoseViewModel.glucose.value) {
            null -> Glucose.STATUS_NONE
            else -> glucoseViewModel.glucose.value?.dinnerStatus
        }

        // Update the colors to the glucose values
        fastingOutput.setTextColor(glucoseViewModel.fastingColor)
        breakfastOutput.setTextColor(glucoseViewModel.breakfastColor)
        lunchOutput.setTextColor(glucoseViewModel.lunchColor)
        dinnerOutput.setTextColor(glucoseViewModel.dinnerColor)
    }
}