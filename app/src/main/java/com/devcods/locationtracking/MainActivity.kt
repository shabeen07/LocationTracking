package com.devcods.locationtracking

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       val startButton : Button = findViewById(R.id.btn_startLocationService)
        startButton.setOnClickListener {
            if(hasLocationPermissions())
                startLocationService()
            else
                requestLocationPermissions()
        }

        val stopButton : Button = findViewById(R.id.btn_stopLocationService)
        stopButton.setOnClickListener{
           stopLocationService()
        }

    }

    private fun isLocationServiceRunning() : Boolean {
        val activityManager : ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (serviceInfo in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (LocationTrackingService::class.java.name == serviceInfo.service.className) {
                if (serviceInfo.foreground)
                    return true
            }
        }
        return false
    }

    private fun stopLocationService() {
        if (isLocationServiceRunning()) {
            startService(Intent(this,LocationTrackingService::class.java)
                .setAction(Constants.ACTION_STOP_LOCATION_SERVICE) )
            Toast.makeText(applicationContext, "Location service stopped", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun startLocationService() {
        if (!isLocationServiceRunning()) {
            startService(Intent(this,LocationTrackingService::class.java)
                .setAction(Constants.ACTION_START_LOCATION_SERVICE))
            Toast.makeText(this, "Location Service started", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Location Access Denied")
                    .setMessage("Please Enable Location Permission")
                    .setCancelable(false)
                    .setPositiveButton("Ok") { _, _ ->
                        ActivityCompat.requestPermissions(this@MainActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Constants.LOCATION_REQUEST_CODE) }.show()
                Log.d(tag, "requestLocationPermissions: show alert dialog")
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Constants.LOCATION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.LOCATION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                startLocationService()
            }else{
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show()
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(this.packageName)))
                }
            }
        }
    }
}