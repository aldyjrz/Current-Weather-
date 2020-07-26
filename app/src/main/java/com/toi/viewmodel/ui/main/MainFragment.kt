package com.toi.viewmodel.ui.main

 import android.app.Activity
 import android.location.Address
 import android.location.Geocoder
 import android.os.Bundle
 import android.util.Log
 import android.view.LayoutInflater
 import android.view.View
 import android.view.ViewGroup
 import android.widget.AdapterView
 import android.widget.Toast
 import androidx.fragment.app.Fragment
 import androidx.lifecycle.ViewModelProviders
 import com.toi.viewmodel.R
 import com.toi.viewmodel.api.CurrentWeather
 import com.toi.viewmodel.service.GPSTracker
 import kotlinx.android.synthetic.main.main_fragment.*
 import java.util.*


@Suppress("DEPRECATION")
class MainFragment : Fragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
         // TODO: Use the ViewModel

        //button listener
        btn_get.setOnClickListener(this)
        btn_cancel.setOnClickListener(this)
        progress.visibility = View.VISIBLE

        val gpsTracker = GPSTracker(this.context!!)
        if(gpsTracker.canGetLocation() && gpsTracker.getLongitude() != 0.0) {

            val latitude: Double = gpsTracker.getLatitude()
            val longitude: Double = gpsTracker.getLongitude()

            progress.visibility = View.INVISIBLE
            viewModel.showToast(this.context!!, "Lat: $latitude\n Long: $longitude")
            val gcd = Geocoder(context, Locale.getDefault())
            val addresses: List<Address> = gcd.getFromLocation(latitude, longitude, 1)
            if (addresses.isNotEmpty()) {
                viewModel.showToast(this.context!!,addresses.get(0).getLocality())
                location.setText("Lat: $latitude\n" +
                        "Lng: $longitude\n"+"${addresses.get(0).adminArea}")
                Log.d("Lat", addresses.get(0).getLocality())
            } else {
                gpsTracker.getLocation()
            }
        }

    }


    override fun onClick(v: View?) {
        val z =  CurrentWeather();

        when (v!!.id) {
            R.id.btn_get -> z.startJob(v.context)

            R.id.btn_cancel ->z.cancelJob(v.context)
        }
    }
    fun restartActivity(activity: Activity) {

            activity.finish()
            activity.startActivity(activity.intent)

    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        TODO("Not yet implemented")
    }

}
