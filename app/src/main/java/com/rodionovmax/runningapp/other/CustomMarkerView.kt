package com.rodionovmax.runningapp.other

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.rodionovmax.runningapp.R
import com.rodionovmax.runningapp.db.RunEntity
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<RunEntity>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId){

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val curRunId = e.x.toInt()
        val run = runs[curRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        val tvDate: TextView = findViewById(R.id.tv_date_marker)
        tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKMH}km/h"
        val tvAvgSpeed: TextView = findViewById(R.id.tv_avg_speed_marker)
        tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMeters / 1000f}km"
        val tvDistance: TextView = findViewById(R.id.tv_distance_marker)
        tvDistance.text = distanceInKm

        val tvDuration: TextView = findViewById(R.id.tv_duration_marker)
        tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val tvCaloriesBurned: TextView = findViewById(R.id.tv_calories_burned_marker)
        val caloriesBurned = "${run.caloriesBurned}kcal"
        tvCaloriesBurned.text = caloriesBurned
    }
}