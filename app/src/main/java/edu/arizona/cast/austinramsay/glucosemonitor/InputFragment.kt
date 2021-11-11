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
import java.time.LocalDateTime
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import java.util.*

private const val TAG = "InputFragment"
private const val ARG_GLUCOSE_DATE = "glucose_date"

class InputFragment : Fragment(R.layout.input_fragment) {

    private val glucoseViewModel: GlucoseViewModel by activityViewModels()

    private lateinit var glucose: Glucose
    private lateinit var dateButton: Button
    private lateinit var clearButton: Button
    private lateinit var historyButton: Button
    private lateinit var fastingInput: EditText
    private lateinit var breakfastInput: EditText
    private lateinit var lunchInput: EditText
    private lateinit var dinnerInput: EditText

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        glucoseViewModel.glucose.observe(viewLifecycleOwner, Observer { glucose ->
            glucose?.let {
                this.glucose = glucose
                glucoseViewModel.sync()
                sync()
            }
        })
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

        // Create a text watcher to apply to all EditText inputs for glucose values
        // Upon values being changed, update the glucose object IF all values are filled
        val valueWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                // removed old code for assignment 9
            }

            override fun afterTextChanged(sequence: Editable?) {
                // do nothing
            }
        }

        // Apply the text watcher to all input fields
        fastingInput.addTextChangedListener(valueWatcher)
        breakfastInput.addTextChangedListener(valueWatcher)
        lunchInput.addTextChangedListener(valueWatcher)
        dinnerInput.addTextChangedListener(valueWatcher)

        // Setup the date button (only shows date for now, no logic)
        dateButton.text = LocalDateTime.now().format(glucoseViewModel.dateFormatterFull)

        // Setup the history button (disabled for now)
        historyButton.isEnabled = false

        // Setup the clear button
        clearButton.setOnClickListener {
            clearFields()
        }

        return view
    }

    override fun onStop() {
        super.onStop()
        glucoseViewModel.saveGlucose(glucose)
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

        // Reset date button to now
        dateButton.text = LocalDateTime.now().format(glucoseViewModel.dateFormatterFull)
    }

    private fun sync() {
        fastingInput.setText(glucose.fasting.toString())
        breakfastInput.setText(glucose.breakfast.toString())
        lunchInput.setText(glucose.lunch.toString())
        dinnerInput.setText(glucose.dinner.toString())

        fastingInput.setTextColor(glucoseViewModel.fastingColor)
        breakfastInput.setTextColor(glucoseViewModel.breakfastColor)
        lunchInput.setTextColor(glucoseViewModel.lunchColor)
        dinnerInput.setTextColor(glucoseViewModel.dinnerColor)

        // Update the date button to the glucose date too
        dateButton.text = glucose.date.toString()
    }
}