package com.example.runningapp2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningapp2.R
import com.example.runningapp2.other.TrackingUtility
import com.example.runningapp2.roomdb.RunEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val ivRunImage = itemView.findViewById<ImageView>(R.id.ivRunImage)
        val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        val tvAvgSpeed = itemView.findViewById<TextView>(R.id.tvAvgSpeed)
        val tvDistance = itemView.findViewById<TextView>(R.id.tvDistance)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val tvCalories = itemView.findViewById<TextView>(R.id.tvCalories)
    }

    val diffCallback = object :DiffUtil.ItemCallback<RunEntity>(){
        override fun areItemsTheSame(oldItem: RunEntity, newItem: RunEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RunEntity, newItem: RunEntity): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    val differ = AsyncListDiffer(this,diffCallback)

    fun submitList(list:List<RunEntity>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_run,parent,false))
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.img).into(holder.ivRunImage)

            val calender = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }

            val dateFormat = SimpleDateFormat("dd.MM.yy",Locale.getDefault())
            holder.tvDate.text = dateFormat.format(calender.time)

            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            holder.tvAvgSpeed.text = avgSpeed

            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            holder.tvDistance.text = distanceInKm

            holder.tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned}kcal"
            holder.tvCalories.text = caloriesBurned

        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}