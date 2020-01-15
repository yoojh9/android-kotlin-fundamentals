package com.example.android.kotlincoroutines

import android.app.Application
import androidx.work.*
import com.example.android.kotlincoroutines.main.RefreshMainDataWork
import java.util.concurrent.TimeUnit

class KotlinCoroutinesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupWorkManagerJob()
    }

    /**
     * Setup WorkManager background job to 'fetch' new network data daily.
     */
    private fun setupWorkManagerJob() {
        // initialize WorkManager with a Factory
        val workManagerConfiguration = Configuration.Builder()
            .setWorkerFactory(RefreshMainDataWork.Factory())
            .build()

        WorkManager.initialize(this, workManagerConfiguration)

        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val work = PeriodicWorkRequestBuilder<RefreshMainDataWork>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(RefreshMainDataWork::class.java.name, ExistingPeriodicWorkPolicy.KEEP, work)

    }
}