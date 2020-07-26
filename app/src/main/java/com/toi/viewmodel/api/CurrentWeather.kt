package com.toi.viewmodel.api

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Context.*
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.toi.viewmodel.R
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.text.DecimalFormat

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CurrentWeather : JobService() {

    companion object {
        private val TAG = CurrentWeather::class.java.simpleName
        //internal const val CITY = "ISIKAN DENGAN NAMA KOTA KAMU"
        internal const val CITY = "Jakarta"
        private const val JOB_ID = 10

        internal const val APP_ID = "93a3696714297ee5a9f65486aa8cb824"
    }
    fun startJob( context: Context) {

        /*
        Cek running job terlebih dahulu
         */
        if (isJobRunning(context)) {
            Toast.makeText(context, "Job Service is already scheduled", Toast.LENGTH_SHORT).show()
            return
        }


        val mServiceComponent = ComponentName(context, CurrentWeather::class.java)

        val builder = JobInfo.Builder(JOB_ID, mServiceComponent)

        /*
        Kondisi network,
        NETWORK_TYPE_ANY, berarti tidak ada ketentuan tertentu
        NETWORK_TYPE_UNMETERED, adalah network yang tidak dibatasi misalnya wifi
        */
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

        /*
        Kondisi device, secara default sudah pada false
        false, berarti device tidak perlu idle ketika job ke trigger
        true, berarti device perlu dalam kondisi idle ketika job ke trigger
        */
        builder.setRequiresDeviceIdle(false)

        /*
        Kondisi charging
        false, berarti device tidak perlu di charge
        true, berarti device perlu dicharge
        */
        builder.setRequiresCharging(false)

        /*
        Periode interval sampai ke trigger
        Dalam milisecond, 1000ms = 1detik
        */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPeriodic(900000) //15 menit
        } else {
            builder.setPeriodic(180000) //3 menit
        }
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.schedule(builder.build())
        Toast.makeText(context, "Job Service started", Toast.LENGTH_SHORT).show()
    }

    fun cancelJob(context: Context) {
        val scheduler =  context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        scheduler.cancel(JOB_ID)
        Toast.makeText(context, "Job Service canceled", Toast.LENGTH_SHORT).show()
    }

    /*
    Gunakanlah method ini untuk melakukan pengecekan "apakah job dengan id 10 sudah berjalan ?"
     */
    private fun isJobRunning(context: Context): Boolean {
        var isScheduled = false

        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == JOB_ID) {
                isScheduled = true
                break
            }
        }

        return isScheduled
    }
    /**
     * onStartJob berjalan di dalam mainthread, return true jika ada proses yang membuat thread baru
     */
    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "onStartJob()")
        getCurrentWeather(params)

        return true
    }

    /**
     * onStopJob akan dipanggil ketika proses belum selesai dikarenakan constraint requirements tidak terpenuhi
     * return true untuk re-scheduler
     */
    override fun onStopJob(params: JobParameters): Boolean {
        Log.d(TAG, "onStopJob()")
        return true
    }

    /**
     * Request data ke API weather, jobFinished dipanggil secara manual ketika proses sudah selesai
     *
     * @param job parameters
     */
    private fun getCurrentWeather(job: JobParameters) {
        Log.d(TAG, "getCurrentWeather: Mulai.....")
        val client = AsyncHttpClient()
        val url = "http://api.openweathermap.org/data/2.5/weather?q=$CITY&appid=$APP_ID"
        Log.d(TAG, "getCurrentWeather: $url")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {
                val result = String(responseBody)
                Log.d(TAG, result)
                try {
                    val responseObject = JSONObject(result)

                    /*
                    Perlu diperhatikan bahwa angka 0 pada getJSONObject menunjukkan index ke-0
                    Jika data yang ingin kita ambil ada lebih dari satu maka gunakanlah looping
                     */
                    val currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main")
                    val description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description")
                    val tempInKelvin = responseObject.getJSONObject("main").getDouble("temp")

                    val tempInCelsius = tempInKelvin - 273
                    val temperature = DecimalFormat("##.##").format(tempInCelsius)

                    val title = "Current Weather"
                    val message = "$currentWeather, $description with $temperature celsius"
                    val notifId = 100

                    showNotification(applicationContext, title, message, notifId)

                    Log.d(TAG, "onSuccess: Job Sukses.....")
                    // ketika proses selesai, maka perlu dipanggil jobFinished dengan parameter false;
                    jobFinished(job, false)

                } catch (e: Exception) {
                    Log.d(TAG, "onSuccess: Gagal Mendapatkan Cuaca.....")
                    // ketika terjadi error, maka jobFinished diset dengan parameter true. Yang artinya job perlu di reschedule
                    jobFinished(job, true)
                    e.printStackTrace()
                }

            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {
                Log.d(TAG, "onFailure: Gagal.....")
                // ketika proses gagal, maka jobFinished diset dengan parameter true. Yang artinya job perlu di reschedule
                jobFinished(job, true)
            }
        })
    }


    /**
     * Menampilkan datanya ke dalam notification
     *
     * @param context context dari notification
     * @param title   judul notifikasi
     * @param message isi dari notifikasi
     * @param notifId id notifikasi
     */
    private fun showNotification(context: Context, title: String, message: String, notifId: Int) {
        val CHANNEL_ID = "Channel_1"
        val CHANNEL_NAME = "Job scheduler channel"

        val notificationManagerCompat = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_refresh)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, android.R.color.black))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setSound(alarmSound)

        /*
        Untuk android Oreo ke atas perlu menambahkan notification channel
        Materi ini akan dibahas lebih lanjut di modul extended
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            val channel = NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT)

            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

            builder.setChannelId(CHANNEL_ID)

            notificationManagerCompat.createNotificationChannel(channel)
        }

        val notification = builder.build()

        notificationManagerCompat.notify(notifId, notification)

    }
}