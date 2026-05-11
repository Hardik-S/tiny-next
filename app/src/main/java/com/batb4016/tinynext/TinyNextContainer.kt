package com.batb4016.tinynext

import android.content.Context
import com.batb4016.tinynext.data.TinyNextRepository
import com.batb4016.tinynext.data.local.TinyNextDatabase
import com.batb4016.tinynext.data.monetization.GooglePlayBillingRepository
import com.batb4016.tinynext.data.settings.DataStoreSettingsRepository

class TinyNextContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: TinyNextDatabase = TinyNextDatabase.getInstance(appContext)
    val repository: TinyNextRepository = TinyNextRepository(database)
    val settingsRepository = DataStoreSettingsRepository(appContext)
    val billingRepository = GooglePlayBillingRepository(appContext)
}

