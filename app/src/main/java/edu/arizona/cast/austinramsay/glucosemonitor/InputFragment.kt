package edu.arizona.cast.austinramsay.glucosemonitor

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.fragment.app.activityViewModels
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.fragment.app.FragmentResultListener
import com.google.gson.GsonBuilder
import com.google.gson.internal.GsonBuildConfig
import edu.arizona.cast.austinramsay.glucosemonitor.api.GlucoseApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
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

        setHasOptionsMenu(true)

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

                // If this is a new glucose entry or an entry without values, clear the fields to make input easier
                if (isIncomplete(inputViewModel.glucose))
                    clearFields()

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_glucose_input, menu)
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
                        Toast.makeText(context, "No pre-existing entry. A new entry will be created upon tapping 'History'.", Toast.LENGTH_LONG).show()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_glucose_button -> {
                // User is attempting to delete this glucose entry
                dbViewModel.deleteGlucose(inputViewModel.glucose)
                Toast.makeText(context, "Entry for ${DateFormatter.formatShort(inputViewModel.glucose.date)} deleted.", Toast.LENGTH_SHORT).show()
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            R.id.send_glucose_button -> {
                // User is attempting to send this glucose entry to another app such as email
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getGlucoseReport())
                    putExtra(Intent.EXTRA_SUBJECT, "Glucose Report for ${DateFormatter.formatShort(inputViewModel.glucose.date)}")
                }.also { intent ->
                    val chooserIntent = Intent.createChooser(intent, getString(R.string.send_glucose))
                    startActivity(chooserIntent)
                }
                true
            }
            R.id.upload_glucose_button -> {
                // User is attempting to upload this entry into the online database service

                // First check if the user has made any changes versus the extracted database entry
                if (!isEqual(dbViewModel.glucose.value, inputViewModel.glucose)) {
                    Toast.makeText(context, "Entry has been modified and hasn't been saved. Please save first.", Toast.LENGTH_SHORT).show()
                    return true
                }

                // Verify all fields are filled in
                if (isIncomplete(inputViewModel.glucose)) {
                    Toast.makeText(context, "Entry has missing values. Complete all fields and save first.", Toast.LENGTH_SHORT).show()
                    return true
                }

                val gson = GsonBuilder().setLenient().create()
                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl("http://u.arizona.edu/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                val glucoseApi: GlucoseApi = retrofit.create(GlucoseApi::class.java)
                val glucoseUploadRequest: Call<String> = glucoseApi.uploadGlucose("austinramsay", "a2391", gson.toJson(inputViewModel.glucose))
                glucoseUploadRequest.enqueue(object: Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(context, "Upload failed. $t", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        Toast.makeText(context, "Upload successful.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, response.toString())
                    }
                })
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun clearFields() {
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

    private fun getGlucoseReport(): String {
        val report = StringBuilder()

        report.appendLine("Glucose Entry Date: ${DateFormatter.formatLong(inputViewModel.glucose.date)}")
        report.appendLine()
        report.appendLine("Overall Average: ${GlucoseCalculator.getAverage(inputViewModel.glucose.fasting, inputViewModel.glucose.breakfast, inputViewModel.glucose.lunch, inputViewModel.glucose.dinner)}")
        report.appendLine()
        report.appendLine("Fasting: ${GlucoseCalculator.getFastingStatus(inputViewModel.glucose.fasting)} (${inputViewModel.glucose.fasting})")
        report.appendLine("Breakfast: ${GlucoseCalculator.getBreakfastStatus(inputViewModel.glucose.breakfast)} (${inputViewModel.glucose.breakfast})")
        report.appendLine("Lunch: ${GlucoseCalculator.getLunchStatus(inputViewModel.glucose.lunch)} (${inputViewModel.glucose.lunch})")
        report.appendLine("Dinner: ${GlucoseCalculator.getDinnerStatus(inputViewModel.glucose.dinner)} (${inputViewModel.glucose.dinner})")

        return report.toString()
    }

    // Function to check if all glucose entry values are equal excluding the date
    private fun isEqual(glucose1: Glucose?, glucose2: Glucose?): Boolean {
        Log.d(TAG, "glucose 1 fasting is ${glucose1?.fasting} - glucose 2 fasting is ${glucose2?.fasting}")
        Log.d(TAG, "glucose 1 breakfast is ${glucose1?.breakfast} - glucose 2 breakfast is ${glucose2?.breakfast}")
        Log.d(TAG, "glucose 1 lunch is ${glucose1?.lunch} - glucose 2 lunch is ${glucose2?.lunch}")
        Log.d(TAG, "glucose 1 dinner is ${glucose1?.dinner} - glucose 2 dinner is ${glucose2?.dinner}")
        return ((glucose1?.fasting == glucose2?.fasting) && (glucose1?.breakfast == glucose2?.breakfast) && (glucose1?.lunch == glucose2?.lunch) && (glucose1?.dinner == glucose2?.dinner))
    }

    private fun isIncomplete(glucose: Glucose): Boolean {
        return (glucose.fasting == 0 && glucose.breakfast == 0 && glucose.lunch == 0 && glucose.dinner == 0)
    }
}