package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NutriPulse Health Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic nudges for hydration targets and clinical meal logging."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showHydrationNudge(currentWaterMl: Int, targetWaterMl: Int) {
        val remaining = (targetWaterMl - currentWaterMl).coerceAtLeast(0)
        val title = "💧 NutriPulse Hydration Alert"
        val message = if (remaining > 0) {
            "You're $remaining ml away from your daily fluid target! Sip a glass of water now to stay active."
        } else {
            "🎉 Hydration target reached! Great job keeping your body cellularly hydrated today."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID_HYDRATION, builder.build())
    }

    fun showMealReminderNudge() {
        val title = "🧠 NutriPulse AI Meal Log"
        val message = "Time to scan or log your meal! Capture your plate to evaluate glycemic index and macro targets."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID_MEAL, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "nutripulse_reminders"
        const val NOTIFICATION_ID_HYDRATION = 1001
        const val NOTIFICATION_ID_MEAL = 1002
    }
}
