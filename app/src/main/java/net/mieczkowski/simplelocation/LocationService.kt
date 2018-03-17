package net.mieczkowski.simplelocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Josh Mieczkowski on 3/17/2018.
 */

class LocationService {

    companion object {

        private lateinit var locationClient: FusedLocationProviderClient

        fun init(context: Context){
            locationClient = LocationServices.getFusedLocationProviderClient(context)
        }
    }

    private var obsCount = 0
    private var locationSubject: PublishSubject<Location> = PublishSubject.create()
    private var locationRequest = LocationRequest().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var locationCallBack: LocationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            locationResult.locations.forEach {
                locationSubject.onNext(it)
            }
        }
    }

    fun configureLocationSettings(settings: LocationRequest.() -> Unit): LocationService{
        settings(locationRequest)

        return this
    }

    fun getLastLocation(): Single<Location>{
        return if (ContextCompat.checkSelfPermission(locationClient.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Single.error(Throwable("Missing Fine Location Permission"))

        }else{
            Single.create<Location>{ emitter ->
                locationClient.lastLocation.addOnSuccessListener { emitter.onSuccess(it) }
                        .addOnFailureListener { emitter.onError(it) }
            }.subscribeOn(Schedulers.io())
        }
    }

    fun getLocationObserver(): Observable<Location>{
        return if (ContextCompat.checkSelfPermission(locationClient.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Observable.error(Throwable("Missing Fine Location Permission"))

        }else{
            locationSubject.subscribeOn(Schedulers.io())
                    .doOnSubscribe {
                        if(obsCount == 0)
                            locationClient.requestLocationUpdates(locationRequest, locationCallBack, null)

                        obsCount++
                    }
                    .doOnDispose {
                        obsCount--

                        if(obsCount == 0)
                            locationClient.removeLocationUpdates(locationCallBack)
                    }
        }
    }


}