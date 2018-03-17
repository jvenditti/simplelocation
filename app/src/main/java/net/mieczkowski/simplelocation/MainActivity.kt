package net.mieczkowski.simplelocation

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.LocationRequest
import io.reactivex.Completable
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    2060)
        }else{
            LocationService.init(this)
            val locationService = LocationService().configureLocationSettings {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            val sub1 = locationService.getLocationObserver()
                    .subscribe({
                        Log.wtf("TEST", "${it.latitude}, ${it.longitude}")
                    }, {
                        it.printStackTrace()
                    })

            val sub2 = locationService.getLocationObserver()
                    .subscribe({
                        Log.wtf("TEST2", "${it.latitude}, ${it.longitude}")
                    }, {
                        it.printStackTrace()
                    })

            Completable.timer(5, TimeUnit.SECONDS)
                    .subscribe {
                        Log.wtf("TEST##%#$", "Stop TEST2")
                        sub2.dispose()
                    }
        }

    }
}
