package com.rodionovmax.runningapp.other

import android.graphics.Color

object Constants {

    const val RUNNING_DATABASE_NAME = "running_db"
    const val REQUEST_CODE_LOCATION_PERMISSION = 0
    const val REQUEST_CODE_GOOGLE_SIGN_IN = 2

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val TIMER_UPDATE_INTERVAL = 100L

    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
    const val KEY_FIREBASE_UID = "KEY_FIREBASE_UID"
    const val KEY_FIREBASE_EMAIL = "KEY_FIREBASE_EMAIL"

    const val LOCATION_UPDATE_INTERVAL = 3000L
    const val FASTEST_LOCATION_INTERVAL = 1000L

    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1
}