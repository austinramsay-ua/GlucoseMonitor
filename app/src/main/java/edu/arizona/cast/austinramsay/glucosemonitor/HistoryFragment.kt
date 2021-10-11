package edu.arizona.cast.austinramsay.glucosemonitor

import android.os.Bundle
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

class HistoryFragment : Fragment(R.layout.history_view) {

    private val glucoseViewModel: GlucoseViewModel by activityViewModels()
    private lateinit var rView: RecyclerView
    private lateinit var rViewAdapter: RecyclerView.Adapter<*>
    private lateinit var rViewManager: RecyclerView.LayoutManager

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
            dateView.text = this.glucose.date.toString()
            avgView.text = this.glucose.average.toString()
            statusCheck.isChecked = if (this.glucose.fastingStatus != Glucose.STATUS_NORMAL) {
                true
            } else if (this.glucose.breakfastStatus != Glucose.STATUS_NORMAL) {
                true
            } else if (this.glucose.lunchStatus != Glucose.STATUS_NORMAL) {
                true
            } else if (this.glucose.dinnerStatus != Glucose.STATUS_NORMAL) {
                true
            } else {
                false
            }
            avgStatus.text = this.glucose.overallStatus
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
                Toast.makeText(context, glucose.toString(), Toast.LENGTH_LONG).show()
            }
        }

        override fun getItemCount() = glucoseList.size
    }

    /*
     * Main History fragment onCreateView function
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.history_view, container, false)

        // TODO: For hardcoded testing purposes only
        glucoseViewModel.setRandomGlucose()
        val data = glucoseViewModel.glucoseHistory

        rViewManager = LinearLayoutManager(context)
        rViewAdapter = GlucoseRViewAdapter(data)

        rView = view.findViewById<RecyclerView>(R.id.history_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = rViewManager
            adapter = rViewAdapter
        }

        return view
    }
}