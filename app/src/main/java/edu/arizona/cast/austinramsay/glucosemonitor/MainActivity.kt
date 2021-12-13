package edu.arizona.cast.austinramsay.glucosemonitor

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), HistoryFragment.Callbacks {

    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set the initial fragment in the main activity's fragment container
        if (currentFragment == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                .add<HistoryFragment>(R.id.fragment_container)
            }
        }
    }

    override fun onGlucoseSelected(glucoseDate: Date) {
        val fragment = InputFragment.newInstance(glucoseDate)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}