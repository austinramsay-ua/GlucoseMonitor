package edu.arizona.cast.austinramsay.glucosemonitor

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
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.fragment.app.FragmentResultListener
import java.util.*

private const val TAG = "InputFragment"
private const val ARG_GLUCOSE_DATE = "glucose_date"
private const val REQUEST_DATE = "DialogDate"

class InputFragment : Fragment(R.layout.input_fragment), FragmentResultListener {

    private val dbViewModel: DBViewModel by activityViewModels()
    private val inputViewModel: InputViewModel by activityViewModels()

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

        // Retrieve selected date from fragment arguments passed from the MainActivity
        val glucoseDate: Date = arguments?.getSerializable(ARG_GLUCOSE_DATE) as Date

        // Query the database using the extracted date
        dbViewModel.loadGlucose(glucoseDate)
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
            DatePickerFragment.newInstance(inputViewModel.glucose.date, REQUEST_DATE).show(childFragmentManager, REQUEST_DATE)
        }

        // Setup the clear button
        clearButton.setOnClickListener {
            clearFields()
        }

        // Setup the history button to save values to the glucose object and then call the
        // 'saveGlucose' function in the view model to update the object in the database
        historyButton.setOnClickListener {
            // Update all the local glucose object values before pushing to database
            inputViewModel.glucose.fasting = fastingInput.text.toString().toIntOrNull() ?: 0
            inputViewModel.glucose.breakfast = breakfastInput.text.toString().toIntOrNull() ?: 0
            inputViewModel.glucose.lunch = lunchInput.text.toString().toIntOrNull() ?: 0
            inputViewModel.glucose.dinner = dinnerInput.text.toString().toIntOrNull() ?: 0

            // When the fragment stops, save the glucose object into the database
            // Check if the database had any entries for this date, if not, create a new one
            if (inputViewModel.glucose.date != dbViewModel.glucose.value?.date) {
                // The requested glucose date was not found in the database, create it now
                dbViewModel.addGlucose(inputViewModel.glucose)
            } else {
                dbViewModel.updateGlucose(inputViewModel.glucose)
            }

            activity?.supportFragmentManager?.popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val valueWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                val fasting = fastingInput.text.toString().toIntOrNull() ?: 0
                val breakfast = breakfastInput.text.toString().toIntOrNull() ?: 0
                val lunch = lunchInput.text.toString().toIntOrNull() ?: 0
                val dinner = dinnerInput.text.toString().toIntOrNull() ?: 0

                // Push new values to the input fragment's local view model and update the UI
                inputViewModel.updateGlucoseLevels(fasting, breakfast, lunch, dinner)
                updateUI()
                Log.d(TAG, "onTextChanged")
            }

            override fun afterTextChanged(sequence: Editable?) {
                // do nothing
            }
        }

        // When the view model glucose object changes, sync this fragments glucose object to the
        // updated view model
        dbViewModel.glucose.observe(viewLifecycleOwner, Observer { glucose ->
            glucose?.let {
                // Keep the local glucose value separate from the view model's values
                // This allows us to distinguish whether values have been changed/updated to later
                // determine whether this local glucose object should be updated or added to the DB

                fastingInput.removeTextChangedListener(valueWatcher)
                breakfastInput.removeTextChangedListener(valueWatcher)
                lunchInput.removeTextChangedListener(valueWatcher)
                dinnerInput.removeTextChangedListener(valueWatcher)

                // Extract the date and level values from the newly retrieved glucose object
                val date = glucose.date
                val fasting = glucose.fasting
                val breakfast = glucose.breakfast
                val lunch = glucose.lunch
                val dinner = glucose.dinner

                // Update the extracted values from the database to the local view model
                inputViewModel.updateGlucoseDate(date)
                inputViewModel.updateGlucoseLevels(fasting, breakfast, lunch, dinner)

                // Set the initial input field values
                fastingInput.setText(inputViewModel.glucose.fasting.toString())
                breakfastInput.setText(inputViewModel.glucose.breakfast.toString())
                lunchInput.setText(inputViewModel.glucose.lunch.toString())
                dinnerInput.setText(inputViewModel.glucose.dinner.toString())

                // Update the UI to display the new values
                updateUI()

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
                inputViewModel.updateGlucoseDate(DatePickerFragment.getSelectedDate(result))

                // Perform check for existing entries in the database and advise the user if an
                // entry was already found for this selected date or if a new one will be created
                dbViewModel.checkExists(DatePickerFragment.getSelectedDate(result)).observe(viewLifecycleOwner, Observer { exists ->
                    if (exists) {
                        Toast.makeText(context, "A previous entry was found with the selected date and is now shown above.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No existing entry was found with the selected date. A new entry will be created upon tapping 'History'.", Toast.LENGTH_LONG).show()
                        clearFields()
                        updateUI()
                    }
                })

                // Attempt to load a glucose object from the database using selected date
                // The database might not have an entry with the selected date, this is fine
                // The entry will be added later if needed
                dbViewModel.loadGlucose(DatePickerFragment.getSelectedDate(result))
            }
        }
    }

    private fun clearFields() {
        // Clear all input fields
        fastingInput.text = null
        breakfastInput.text = null
        lunchInput.text = null
        dinnerInput.text = null
    }

    private fun updateUI() {
        dateOutput.text = DateFormatter.formatLong(inputViewModel.glucose.date)
        fastingOutput.text = GlucoseCalculator.getFastingStatus(inputViewModel.glucose.fasting)
        breakfastOutput.text = GlucoseCalculator.getBreakfastStatus(inputViewModel.glucose.breakfast)
        lunchOutput.text = GlucoseCalculator.getLunchStatus(inputViewModel.glucose.lunch)
        dinnerOutput.text = GlucoseCalculator.getDinnerStatus(inputViewModel.glucose.dinner)

        fastingInput.setTextColor(inputViewModel.fastingColor)
        breakfastInput.setTextColor(inputViewModel.breakfastColor)
        lunchInput.setTextColor(inputViewModel.lunchColor)
        dinnerInput.setTextColor(inputViewModel.dinnerColor)

        dateButton.text = DateFormatter.formatLong(inputViewModel.glucose.date)
    }
}