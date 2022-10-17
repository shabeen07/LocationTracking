package com.devcods.locationtracking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


class LocationTrackingService : Service() {

    private val tag = "LocationTrackingService"

    private val locationCallback: LocationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if (p0.lastLocation!=null){
                val latitude: Double = p0.lastLocation!!.latitude
                val longitude: Double = p0.lastLocation!!.longitude
                Log.d("LOCATION_UPDATE", "$latitude,$longitude")
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent!=null){
            val action = intent.action
            if (action!=null){
                if (action == Constants.ACTION_START_LOCATION_SERVICE){
                    startLocationService()
                }else if (action == Constants.ACTION_STOP_LOCATION_SERVICE){
                    stopLocationService()
                }
                Log.d(tag, "onStartCommand: $action")
            }
        }
        return START_STICKY
    }

    private fun stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    private fun startLocationService() {
        val channelId = "Location_service_channel";
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val  resultIntent = Intent()
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                applicationContext,
                0,
                resultIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                applicationContext,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val builder : NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext,channelId)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Location Service")
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setContentText("Running")
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(false)
        builder.priority = NotificationCompat.PRIORITY_MAX

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    "Location Service",
                    NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.description = "This channel is used by location service"
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        startForeground(Constants.LOCATION_SERVICE_ID, builder.build())
    }

}