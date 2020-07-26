package com.toi.viewmodel.service

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat


/**
 * Ahmet Ertugrul OZCAN
 * Cihazin konum bilgisini goruntuler
 */
class GPSTracker(private val mContext: Context) : Service(),
    LocationListener {

    var isGPSEnabled = false

    var isNetworkEnabled = false
    var canGetLocation = false


    private var location: Location? = null
    private var latitude = 0.0
    private var longitude = 0.0

    // LocationManager nesnesi
    protected var locationManager: LocationManager? = null


    fun getLocation(): Location? {
        try {
            locationManager =
                mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // GPS acik mi?
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // Internet acik mi?
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkEnabled) {
            } else {
                canGetLocation = true

                // Once internetten alinan konum bilgisi kayitlanir
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    )
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                        this
                    )
                    Log.d("Network", "Network")
                    if (locationManager != null) {
                        location =
                            locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null) {
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    }
                }
                // GPS'ten alinan konum bilgisi;
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                            this
                        )
                        Log.d("GPS Enabled", "GPS Enabled")
                        if (locationManager != null) {
                            location =
                                locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return location
    }

    // Enlem bilgisini dondurur
    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }
        return latitude
    }

    // Boylam bilgisini dondurur
    fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }
        return longitude
    }

    override fun onLocationChanged(location: Location) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(
        provider: String,
        status: Int,
        extras: Bundle
    ) {
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    // Konum bilgisi kapali ise kullaniciya ayarlar sayfasina baglanti iceren bir mesaj goruntulenir
    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(mContext)

        // Mesaj basligi
        alertDialog.setTitle("GPS Kapalı")

        // Mesaj
        alertDialog.setMessage("Konum bilgisi alınamıyor. Ayarlara giderek gps'i aktif hale getiriniz.")

        // Mesaj ikonu
        //alertDialog.setIcon(R.drawable.delete);

        // Ayarlar butonuna tiklandiginda
        alertDialog.setPositiveButton(
            "Ayarlar"
        ) { dialog, which ->
            val intent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            mContext.startActivity(intent)
        }

        // Iptal butonuna tiklandiginda
        alertDialog.setNegativeButton(
            "İptal"
        ) { dialog, which -> dialog.cancel() }

        // Mesaj kutusunu goster
        alertDialog.show()
    }

    // LocationManager'in gps isteklerini durdurur
    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }

    companion object {
        // Konum guncellemesi gerektirecek minimum degisim miktari
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // metre

        // Konum guncellemesi gerektirecek minimum sure miktari
        private const val MIN_TIME_BW_UPDATES = 1000 * 60 * 1 // dakika
            .toLong()
    }

    //
    // Kurucu Metod - Constructor
    //
    init {
        getLocation()
    }
}