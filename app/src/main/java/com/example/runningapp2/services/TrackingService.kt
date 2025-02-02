package com.example.runningapp2.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningapp2.R
import com.example.runningapp2.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp2.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningapp2.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp2.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp2.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runningapp2.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningapp2.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapp2.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningapp2.other.Constants.NOTIFICATION_ID
import com.example.runningapp2.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.runningapp2.other.TrackingUtility
import com.example.runningapp2.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.truncate


//format of val pathPoints = MutableLivedata<MutableList<MutableList<LatLng>>>()
typealias polyline = MutableList<LatLng>
typealias polylines = MutableList<polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true   //for is it first run
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient //for almost accurate location update

    private val timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder:NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder

    companion object{

        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<polylines>()
    }

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                //time difference between now and timestarted
                lapTime = System.currentTimeMillis() - timeStarted
                //post the new laptime
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }
    //update the value
    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it) //update the location if istracking is true
            updateNotificationTrackingState(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE ->{
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    }
                    else{
                       startTimer()
                        Timber.d("resuming service......")
                    }

                }
                ACTION_PAUSE_SERVICE ->{
                   pauseService()
                }
                ACTION_STOP_SERVICE ->{
                    Timber.d("stopped service")
                    killService()
                }

            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    //for pause the service
    private fun pauseService(){
        isTracking.postValue(false) //if user click pause btn then is tracking is false
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)

        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder,ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled){
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp,notificationActionText,pendingIntent)
            notificationManager.notify(NOTIFICATION_ID,curNotificationBuilder.build())
        }

    }

    //this method update the location after 2 or 5 seconds
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking:Boolean){
        if (isTracking){
            if (TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }

                fusedLocationProviderClient.requestLocationUpdates(request,locationCallback,Looper.getMainLooper())
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    //this callback give updated location called in updatetracking method
    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result : LocationResult?) {
            super.onLocationResult(result)

            if (isTracking.value!!){
                result?.locations?.let {locations ->
                    for (location in locations){
                        addPathPoint(location)
                        Timber.d("New Location: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }


    private fun addPathPoint(location: Location?){
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    //this method add empty list with coordinates
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    }?: pathPoints.postValue(mutableListOf(mutableListOf()))

    //start foreground service
    private fun startForegroundService(){

        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            if (!serviceKilled){
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID,notification.build())
            }
        })
    }

    //create notification channel
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }



}