package net.mieczkowski.simplelocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.location.LocationRequest
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private val subscriber: CompositeDisposable = CompositeDisposable()
    private var service: LocationService? = null

    /**
     * Make sure you have granted the necessary location permission in Settings.
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(TAG, "onCreate")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 2060)

        } else {

            /**
             * Using "applicationContext" to prevent memory leak from Google class
             */
            LocationService.init(applicationContext)
            service = LocationService().configureLocationSettings {
                interval = 2500
                fastestInterval = 1000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        /**
         * Listen for all location updates using the "getLocationObserver()". If you only want to
         * get the location once, use the "getLocation()" call.
         */
        service?.let {
            subscriber.add(it.getLocationObserver()
                    .subscribe({
                        Log.d(TAG, "--- subscriber: ${it.latitude}, ${it.longitude}")
                        textView.text = ("latitude: ${it.latitude} | longitude: ${it.longitude}")
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        Log.d(TAG, "--- clear subscriber")
        /**
         * Because we're adding to the CompositeDisposable on resume, we need to "clear()" the
         * disposable instead of using "dispose()". Clear clears the container, then disposes all
         * the previously contained disposables. Dispose clears the container and sets "isDisposed"
         * to true so it will not accept any new disposables.
         */
        /*
        if (!subscriber.isDisposed) {
            subscriber.dispose()
        }
        */
        subscriber.clear()
        textView.text = ("cleared")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }
}
