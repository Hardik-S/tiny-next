package com.batb4016.tinynext

import android.app.Application

class TinyNextApplication : Application() {
    lateinit var container: TinyNextContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = TinyNextContainer(this)
        container.billingRepository.start()
    }
}
