package edu.arizona.cast.austinramsay.glucosemonitor

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "HistoryFragment"

class HistoryFragment : Fragment(R.layout.history_view) {

    private val glucoseViewModel: GlucoseViewModel by activityViewModels()
    private lateinit var rView: RecyclerView
    private lateinit var rViewManager: RecyclerView.LayoutManager
    private var rViewAdapter: GlucoseRViewAdapter? = GlucoseRViewAdapter(emptyList())

    /*
     * Interface for switching between fragments
     */
    interface Callbacks {
        fun onGlucoseSelected(glucoseDate: Date)
    }

    private var callbacks: Callbacks? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    /*
     * RecyclerView Adapter and Holder classes
     */
    private inner class GlucoseHolder(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var glucose: Glucose
        private val dateView: TextView = itemView.findViewById(R.id.glucose_date)
        private val avgView: TextView = itemView.findViewById(R.id.glucose_avg)
        private val statusCheck: CheckBox = itemView.findViewById(R.id.glucose_check)
        private val avgStatus: TextView = itemView.findViewById(R.id.glucose_status)

        fun bind(glucose: Glucose) {
            this.glucose = glucose

            dateView.text = DateFormatter.formatShort(this.glucose.date)

            avgView.text = GlucoseCalculator.getAverage(
                this.glucose.fasting,
                this.glucose.breakfast,
                this.glucose.lunch,
                this.glucose.dinner
            ).toString()

            avgStatus.text = GlucoseCalculator.getAverageStatus(
                this.glucose.fasting,
                this.glucose.breakfast,
                this.glucose.lunch,
                this.glucose.dinner)

            statusCheck.isChecked = when {
                GlucoseCalculator.getFastingStatus(this.glucose.fasting) != GlucoseCalculator.STATUS_NORMAL -> true
                GlucoseCalculator.getBreakfastStatus(this.glucose.breakfast) != GlucoseCalculator.STATUS_NORMAL -> true
                GlucoseCalculator.getLunchStatus(this.glucose.lunch) != GlucoseCalculator.STATUS_NORMAL -> true
                GlucoseCalculator.getDinnerStatus(this.glucose.dinner) != GlucoseCalculator.STATUS_NORMAL -> true
                else -> false
            }
        }
    }

    private inner class GlucoseRViewAdapter(var glucoseList: List<Glucose>) : RecyclerView.Adapter<GlucoseHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlucoseHolder {
            val view = layoutInflater.inflate(R.layout.list_item_glucose, parent, false)

            return GlucoseHolder(view)
        }

        override fun onBindViewHolder(holder: GlucoseHolder, position: Int) {
            val glucose = glucoseList[position]
            holder.bind(glucose)

            // When an item is clicked on, display a Toast with the Glucose details
            holder.itemView.setOnClickListener {
                callbacks?.onGlucoseSelected(glucose.date)
            }
        }

        override fun getItemCount() = glucoseList.size
    }

    /*
     * Main History fragment onCreateView function
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.history_view, container, false)

        rViewManager = LinearLayoutManager(context)

        rView = view.findViewById<RecyclerView>(R.id.history_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = rViewManager
            adapter = rViewAdapter
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        glucoseViewModel.glucoseList.observe(
            viewLifecycleOwner,
            { glucoseList ->
                glucoseList?.let {
                    Log.i(TAG, "Got glucose list of size: ${glucoseList.size}")
                    updateUI(glucoseList)
                }
            }
        )
    }

    private fun updateUI(glucoseList: List<Glucose>) {
        rViewAdapter = GlucoseRViewAdapter(glucoseList)
        rView.adapter = rViewAdapter
    }
}