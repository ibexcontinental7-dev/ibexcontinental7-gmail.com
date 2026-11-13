package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

class FollowUpReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        FollowUpReminderScheduler.showDailyFollowUpNotification(context)
    }
}

object FollowUpReminderScheduler {
    private const val PREFS_NAME = "askari_reminder_prefs"
    private const val KEY_ENABLED = "reminder_enabled"
    private const val KEY_HOUR = "reminder_hour"
    private const val KEY_DUE_COUNT = "cached_due_count"
    private const val KEY_IS_URDU = "cached_is_urdu"

    private const val CHANNEL_ID = "askari_followup_daily"
    private const val NOTIFICATION_ID = 49201
    private const val REQUEST_CODE = 49202

    fun isEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
    }

    fun getReminderHour(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_HOUR, 9)
    }

    fun updateDueCount(context: Context, dueCount: Int, isUrdu: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_DUE_COUNT, dueCount)
            .putBoolean(KEY_IS_URDU, isUrdu)
            .apply()
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun scheduleDailyMorningReminder(
        context: Context,
        hourOfDay: Int = getReminderHour(context),
        dueCount: Int,
        isUrdu: Boolean
    ) {
        val safeHour = hourOfDay.coerceIn(6, 20)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, true)
            .putInt(KEY_HOUR, safeHour)
            .putInt(KEY_DUE_COUNT, dueCount)
            .putBoolean(KEY_IS_URDU, isUrdu)
            .apply()

        createChannel(context)

        val triggerCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, safeHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerCal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            buildAlarmPendingIntent(context)
        )
    }

    fun cancelDailyReminder(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, false)
            .apply()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildAlarmPendingIntent(context))
    }

    fun showDailyFollowUpNotification(context: Context, overrideDueCount: Int? = null, overrideIsUrdu: Boolean? = null): Boolean {
        if (!hasNotificationPermission(context)) return false
        createChannel(context)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dueCount = overrideDueCount ?: prefs.getInt(KEY_DUE_COUNT, 0)
        val isUrdu = overrideIsUrdu ?: prefs.getBoolean(KEY_IS_URDU, true)

        val title = if (isUrdu) {
            "عسکری تکافل — روزانہ فالو اپ یاد دہانی"
        } else {
            "Askari Takaful — Morning Follow-up Brief"
        }

        val body = if (isUrdu) {
            if (dueCount > 0) {
                "آج آپ کے $dueCount واجب الادا کلائنٹ فالو اپس ہیں۔ ابھی جائزہ لیں اور رابطہ کریں۔"
            } else {
                "آج کوئی واجب الادا فالو اپ نہیں — نئے پراسپیکٹس تلاش کریں اور اپنا ماہانہ ہدف پورا کریں۔"
            }
        } else {
            if (dueCount > 0) {
                "You have $dueCount due follow-ups today. Open Follow-up Manager to review and reach out."
            } else {
                "No overdue follow-ups today. Review new Islamabad prospects and track your monthly GM targets."
            }
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE + 1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.takaful_launcher_icon_1794594951045)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun buildAlarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, FollowUpReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Morning Follow-up Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily morning reminders for due Askari Family Takaful client follow-ups."
            }
            manager.createNotificationChannel(channel)
        }
    }
}
