package edu.arizona.cast.austinramsay.glucosemonitor

import android.app.Application
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

class GlucoseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GlucoseRepository.initialize(this)
    }
}