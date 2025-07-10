package com.nullpointer.squad

import android.app.Application
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NullPointerApp: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    companion object{
        lateinit var instance: NullPointerApp

    }

}