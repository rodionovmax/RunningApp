package com.rodionovmax.runningapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(object : Timber.DebugTree() {
            /**
             * Override [log] to modify the tag and add a "global tag" prefix to it. You can rename the String "global_tag_" as you see fit.
             */
            override fun log(
                priority: Int, tag: String?, message: String, t: Throwable?
            ) {
                super.log(priority, "timber_$tag", message, t)
            }
        })
    }
}