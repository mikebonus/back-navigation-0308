package com.luxpmsoft.luxaipoc.widget

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class CurrentLocation (val activity: Activity,
                       var mFusedLocationClient: FusedLocationProviderClient?,
                       var mOnLocationResolved: OnLocationResolved?) {
    val PERMISSION_ID = 42

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    fun requestPermissions() {
        activity.requestPermissions(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient!!.lastLocation.addOnCompleteListener(activity) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        mOnLocationResolved!!.onLocationResolved(location)
                    }
                }
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            mOnLocationResolved!!.onLocationResolved(mLastLocation)
        }
    }

    interface OnLocationResolved {
        fun onLocationResolved(location: Location?)
    }
}