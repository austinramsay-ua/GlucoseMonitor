package edu.arizona.cast.austinramsay.glucosemonitor

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import java.time.LocalDateTime
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.fragment.app.FragmentResultListener
import java.util.*

private const val TAG = "InputFragment"
private const val ARG_GLUCOSE_DATE = "glucose_date"
private const val REQUEST_DATE = "DialogDate"

class InputFragment : Fragment(R.layout.input_fragment), FragmentResultListener {

    private val glucoseViewModel: GlucoseViewModel by activityViewModels()

    // Local glucose object separated from the view model's glucose object
    private lateinit var glucose: Glucose

    // UI elements
    private lateinit var dateButton: Button
    private lateinit var clearButton: Button
    private lateinit var historyButton: Button
    private lateinit var fastingInput: EditText
    private lateinit var breakfastInput: EditText
    private lateinit var lunchInput: EditText
    private lateinit var dinnerInput: EditText
    private lateinit var dateOutput: TextView
    private lateinit var fastingOutput: TextView
    private lateinit var breakfastOutput: TextView
    private lateinit var lunchOutput: TextView
    private lateinit var dinnerOutput: TextView

    companion object {
        fun newInstance(glucoseDate: Date): InputFragment {
            val args = Bundle().apply {
                putSerializable(ARG_GLUCOSE_DATE, glucoseDate)
            }
            return InputFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve data from set fragment arguments
        glucose = Glucose()
        val glucoseDate: Date = arguments?.getSerializable(ARG_GLUCOSE_DATE) as Date
        Log.d(TAG, "args bundle glucose date retreived: $glucoseDate")
        glucoseViewModel.loadGlucose(glucoseDate)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.input_fragment, container, false)

        dateButton = view.findViewById(R.id.date_button)
        clearButton = view.findViewById(R.id.clear_button)
        historyButton = view.findViewById(R.id.history_button)
        fastingInput = view.findViewById(R.id.et_fasting)
        breakfastInput = view.findViewById(R.id.et_breakfast)
        lunchInput = view.findViewById(R.id.et_lunch)
        dinnerInput = view.findViewById(R.id.et_dinner)
        dateOutput = view.findViewById(R.id.date_overview_label)
        fastingOutput = view.findViewById(R.id.fasting_overview_output)
        breakfastOutput = view.findViewById(R.id.breakfast_overview_output)
        lunchOutput = view.findViewById(R.id.lunch_overview_output)
        dinnerOutput = view.findViewById(R.id.dinner_overview_output)

        // Setup the date button (only shows date for now, no logic)
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(glucose.date, REQUEST_DATE).show(childFragmentManager, REQUEST_DATE)
        }

        // Setup the clear button
        clearButton.setOnClickListener {
            clearFields()
        }

        // Setup the history button to save values to the glucose object and then call the
        // 'saveGlucose' function in the view model to update the object in the database
        historyButton.setOnClickListener {
            // Update all the local glucose object values before pushing to database
            glucose.fasting = fastingInput.text.toString().toIntOrNull() ?: 0
            glucose.breakfast = breakfastInput.text.toString().toIntOrNull() ?: 0
            glucose.lunch = lunchInput.text.toString().toIntOrNull() ?: 0
            glucose.dinner = dinnerInput.text.toString().toIntOrNull() ?: 0

            // When the fragment stops, save the glucose object into the database
            // Check if the database had any entries for this date, if not, create a new one
            if (glucose.date != glucoseViewModel.glucose.value?.date) {
                // The requested glucose date was not found in the database, create it now
                glucoseViewModel.addGlucose(glucose)
                glucoseViewModel.loadGlucose(glucose.date)
            } else {
                glucoseViewModel.updateGlucose(glucose)
            }

            activity?.supportFragmentManager?.popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create a text watcher to apply to all EditText inputs for glucose values
        // Upon values being changed, update the glucose object IF all values are filled
        val valueWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                glucose.fasting = fastingInput.text.toString().toIntOrNull() ?: 0
                glucose.breakfast = breakfastInput.text.toString().toIntOrNull() ?: 0
                glucose.lunch = lunchInput.text.toString().toIntOrNull() ?: 0
                glucose.dinner = dinnerInput.text.toString().toIntOrNull() ?: 0

                glucoseViewModel.sync(glucose)
                syncAll()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // do nothing
            }
        }

        // When the view model glucose object changes, sync this fragments glucose object to the
        // updated view model
        glucoseViewModel.glucose.observe(viewLifecycleOwner, Observer { glucose ->
            glucose?.let {
                // Keep the local glucose value separate from the view model's values
                // This allows us to distinguish whether values have been changed/updated to later
                // determine whether this local glucose object should be updated or added to the DB
                this.glucose.date = glucose.date
                this.glucose.fasting = glucose.fasting
                this.glucose.breakfast = glucose.breakfast
                this.glucose.lunch = glucose.lunch
                this.glucose.dinner = glucose.dinner

                fastingInput.setText(glucose.fasting.toString())
                breakfastInput.setText(glucose.breakfast.toString())
                lunchInput.setText(glucose.lunch.toString())
                dinnerInput.setText(glucose.dinner.toString())

                glucoseViewModel.sync(this.glucose)
                syncAll()

                // Apply the text watcher to all input fields AFTER the initial values are set
                // into the input fields to not trigger the watcher prematurely
                fastingInput.addTextChangedListener(valueWatcher)
                breakfastInput.addTextChangedListener(valueWatcher)
                lunchInput.addTextChangedListener(valueWatcher)
                dinnerInput.addTextChangedListener(valueWatcher)
            }
        })

        childFragmentManager.setFragmentResultListener(REQUEST_DATE, viewLifecycleOwner, this)
    }

    override fun onStop() {
        super.onStop()
    }

    // User has selected a new date from the DateDialog
    override fun onFragmentResult(requestCode: String, result: Bundle) {
        when (requestCode) {
            REQUEST_DATE -> {
                Log.d(TAG, "Received result for $requestCode")

                glucose.date = DatePickerFragment.getSelectedDate(result)

                glucoseViewModel.checkExists(DatePickerFragment.getSelectedDate(result)).observe(viewLifecycleOwner, Observer { exists ->
                    if (exists) {
                        Toast.makeText(context, "A previous entry was found with the selected date and is now shown above.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No existing entry was found with the selected date. A new entry will be created upon tapping 'History'.", Toast.LENGTH_LONG).show()
                        clearFields()
                    }
                })

                // Attempt to load a glucose object from the database using selected date
                // The database might not have an entry with the selected date, this is fine
                // The entry will be added later if needed
                glucoseViewModel.loadGlucose(DatePickerFragment.getSelectedDate(result))

                // Update the date button to match the newly selected date
                syncAll()
            }
        }
    }

    // Check if any input fields are blank (fasting, breakfast, lunch or dinner glucose values)
    private fun fieldsBlank(): Boolean {
        val fastingValue = fastingInput.text.toString()
        val breakfastValue = breakfastInput.text.toString()
        val lunchValue = lunchInput.text.toString()
        val dinnerValue = dinnerInput.text.toString()

        return fastingValue.isBlank() || breakfastValue.isBlank() || lunchValue.isBlank() || dinnerValue.isBlank()
    }

    private fun clearFields() {
        // Clear all input fields
        fastingInput.text = null
        breakfastInput.text = null
        lunchInput.text = null
        dinnerInput.text = null
    }

    private fun syncAll() {
        dateOutput.text = DateFormatter.formatLong(glucose.date)
        fastingOutput.text = GlucoseCalculator.getFastingStatus(glucose.fasting)
        breakfastOutput.text = GlucoseCalculator.getBreakfastStatus(glucose.breakfast)
        lunchOutput.text = GlucoseCalculator.getLunchStatus(glucose.lunch)
        dinnerOutput.text = GlucoseCalculator.getDinnerStatus(glucose.dinner)

        fastingInput.setTextColor(glucoseViewModel.fastingColor)
        breakfastInput.setTextColor(glucoseViewModel.breakfastColor)
        lunchInput.setTextColor(glucoseViewModel.lunchColor)
        dinnerInput.setTextColor(glucoseViewModel.dinnerColor)

        dateButton.text = DateFormatter.formatLong(glucose.date)
    }
}