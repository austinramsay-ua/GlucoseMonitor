package edu.arizona.cast.austinramsay.glucosemonitor

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.core.content.edit

private const val PREF_REMINDER_ENABLED = "reminderEnabled"

object Preferences {
    fun isReminderEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            PREF_REMINDER_ENABLED, false)
    }

    fun setReminderEnabled(context: Context, isEnabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(PREF_REMINDER_ENABLED, isEnabled)
        }
    }
}