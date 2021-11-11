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
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import kotlinx.coroutines.delay
import java.lang.NumberFormatException
import java.time.LocalDate
import java.time.LocalDateTime

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val glucoseViewModel: GlucoseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set the input overview fragment up
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // TODO: Switch fragment, history fragment testing
        if (currentFragment == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                .add<HistoryFragment>(R.id.fragment_container)
            }
        }
    }
}