package com.example.runningapp2.other

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.example.runningapp2.R
import com.example.runningapp2.roomdb.RunEntity
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



class CustomMarkerView (val runs: List<RunEntity>, context: Context, layoutID: Int): MarkerView(context,layoutID) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }



        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            super.refreshContent(e, highlight)

            if (childCount == 0) {
                inflate(context, R.layout.marker_view, this)
            }

            val tvDate = findViewById<TextView>(R.id.tvDateMarker)
            val tvAvgSpeed = findViewById<TextView>(R.id.tvAvgSpeedMarker)
            val tvDistance = findViewById<TextView>(R.id.tvDistanceMarker)
            val tvCalories = findViewById<TextView>(R.id.tvCaloriesBurnedMarker)
            val tvDuration = findViewById<TextView>(R.id.tvDurationMarker)


            if (e == null) {
                return
            }
            val curRunID = e.x.toInt()
            val run = runs[curRunID]

            val calender = Calendar.getInstance().apply {
                Log.e("Suraj==>>", "refreshContent:=> " + run)
                Log.e("Suraj==>>", "curRunID:=> ${curRunID} :" + runs[curRunID])
                timeInMillis = run.timestamp
            }

            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            tvDate.text = dateFormat.format(calender.time)

            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            Log.e("Suraj==>>", "avgSpeed:=> ${avgSpeed} :")
            tvAvgSpeed.text = avgSpeed

            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            tvDistance.text = distanceInKm

            tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned}kcal"
            tvCalories.text = caloriesBurned
        }





}