package com.cdpg.kmppoc

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.cdpg.dhruva.core.config.AppConfigs
import com.cdpg.dhruva.di.AppGraph
import com.cdpg.dhruva.platform.createAndroidAppGraph
import com.cdpg.kmppoc.sync.OutboxWorker

/** Owns the one shared [AppGraph] for the process. */
class DhruvaApplication : Application() {

    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        // No real backend yet: run against the in-process mock (OTP 111111).
        graph = createAndroidAppGraph(
            context = this,
            config = AppConfigs.mock(appVersion = BuildConfig.VERSION_NAME, platform = "android"),
        )

        OutboxWorker.schedulePeriodic(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                // App came to the foreground: deliver anything queued while away.
                override fun onStart(owner: LifecycleOwner) = graph.drainOutboxInBackground()
            },
        )
    }
}
