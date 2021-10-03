package edu.arizona.cast.austinramsay.glucosemonitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import kotlinx.coroutines.delay
import java.lang.NumberFormatException
import java.time.LocalDate
import java.time.LocalDateTime

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var dateButton: Button
    private lateinit var clearButton: Button
    private lateinit var historyButton: Button
    private lateinit var fastingInput: EditText
    private lateinit var breakfastInput: EditText
    private lateinit var lunchInput: EditText
    private lateinit var dinnerInput: EditText

    private val glucoseViewModel: GlucoseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateButton = findViewById(R.id.date_button)
        clearButton = findViewById(R.id.clear_button)
        historyButton = findViewById(R.id.history_button)
        fastingInput = findViewById(R.id.et_fasting)
        breakfastInput = findViewById(R.id.et_breakfast)
        lunchInput = findViewById(R.id.et_lunch)
        dinnerInput = findViewById(R.id.et_dinner)

        // Set the input overview fragment up
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val fragment = InputOverviewFragment()
        if (currentFragment == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
        supportFragmentManager.beginTransaction().remove(fragment).commitNow()

        // Create a text watcher to apply to all EditText inputs for glucose values
        // Upon values being changed, update the glucose object IF all values are filled
        val valueWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                glucoseViewModel.updateGlucose(
                    fasting = fastingInput.text.toString().toIntOrNull() ?: 0,
                    breakfast = breakfastInput.text.toString().toIntOrNull() ?: 0,
                    lunch = lunchInput.text.toString().toIntOrNull() ?: 0,
                    dinner = dinnerInput.text.toString().toIntOrNull() ?: 0
                )
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

        // If the view model's glucose object changes, reflect the colors of the UI to match
        glucoseViewModel.glucose.observe(this, Observer {
            sync()
        })
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
        glucoseViewModel.glucose.value = null

        // Reset date button to now
        dateButton.text = LocalDateTime.now().format(glucoseViewModel.dateFormatterFull)
    }

    private fun sync() {
        fastingInput.setTextColor(glucoseViewModel.fastingColor)
        breakfastInput.setTextColor(glucoseViewModel.breakfastColor)
        lunchInput.setTextColor(glucoseViewModel.lunchColor)
        dinnerInput.setTextColor(glucoseViewModel.dinnerColor)

        // Update the date button to the glucose date too
        dateButton.text = glucoseViewModel.glucose.value?.date?.format(glucoseViewModel.dateFormatterFull)
    }
}