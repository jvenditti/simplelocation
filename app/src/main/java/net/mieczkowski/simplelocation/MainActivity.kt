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
import io.reactivex.disposables.Disposable

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    lateinit var subscriber1: Disposable
    val subscriber2: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(TAG, "onCreate")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 2060)

        } else {

            LocationService.init(this)
            val locationService = LocationService().configureLocationSettings {
                interval = 5000
                fastestInterval = 1000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            // Gets a single location event
            // NOTE: Singles are auto dispose (so you don't need to make the dispose call)
            subscriber1 = locationService.getLocation()
                    .subscribe({
                        Log.d(TAG, "--- subscriber1: ${it.latitude}, ${it.longitude}")
                    }, {
                        it.printStackTrace()
                    })

            // Gets a location observable, where all location updates can be listened to
            subscriber2.add(locationService.getLocationObserver()
                    .subscribe({
                        Log.d(TAG, "--- subscriber2: ${it.latitude}, ${it.longitude}")
                    }, {
                        it.printStackTrace()
                    }))

            /*
            Completable.timer(5, TimeUnit.SECONDS)
                    .subscribe {
                        Log.d(TAG, "--- dispose subscriber2")
                        subscriber2.dispose()
                    }
                    */
        }
    }

    // TODO: Leverage start/pause

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        /*
        // Don't need to do this because you're getting a Single back $$$$$$
        if (!subscriber1.isDisposed) {
            Log.d(TAG, "--- dispose subscriber1")
            subscriber1.dispose()
        }
        */
        if (!subscriber2.isDisposed) {
            Log.d(TAG, "--- dispose subscriber2")
            subscriber2.dispose()
        }
    }
}
