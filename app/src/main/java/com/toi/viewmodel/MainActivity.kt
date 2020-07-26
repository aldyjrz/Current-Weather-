package com.toi.viewmodel

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.toi.viewmodel.service.GPSTracker
import com.toi.viewmodel.ui.main.MainFragment
import kotlinx.android.synthetic.main.main_fragment.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()

        }
        cekPermisi()

    }

    fun cekPermisi(){

        val allPermission = 101
        val permissions = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        ActivityCompat.requestPermissions(this, permissions, allPermission)

    }
}
