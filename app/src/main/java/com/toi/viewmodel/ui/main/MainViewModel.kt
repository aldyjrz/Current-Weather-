package com.toi.viewmodel.ui.main

import android.app.Activity
import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModel
import com.toi.viewmodel.api.CurrentWeather


class MainViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    fun showToast(context: Context, string: String){
        Toast.makeText(
            context,
            string,
            Toast.LENGTH_LONG
        ).show()
    }
}
