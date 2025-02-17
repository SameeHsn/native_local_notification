package com.example.nativelocalnotification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CHANNEL_ID = "10000_local_notification_channel"
        private const val NOTIFICATION_ID = 10000
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        val button: Button = findViewById(R.id.notify_button)
        button.setOnClickListener {
            sendNotification()

        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse("/storage/emulated/0/Music/1676641724335.mp3")
            val audioAttributes =
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
            val name = "Local Notification"
            val descriptionText = "This is a test notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setBypassDnd(true)
                setSound(soundUri , audioAttributes)
            }


            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

val soundUri = Uri.parse("/storage/emulated/0/Music/1676641724335.mp3")

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(androidx.core.R.drawable.notification_bg)
            .setContentTitle("Hello!")
            .setContentText("This is your local notification.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setAllowSystemGeneratedContextualActions(true)
            .setVisibility(VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setLocalOnly(true)
            .setShowWhen(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(1000)


        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }


    private fun getSoundUriFromFile(filePath: String): Uri? {
        val file = File(filePath)
        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            Log.e("Notification", "Sound file not found: $filePath")
            null
        }
    }



    private fun getSoundUriFromMediaStore(context: Context, fileName: String): Uri? {
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$fileName%") // Using LIKE for flexible matching

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {  // Change moveToNext() → moveToFirst()
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
            }
        }
        return null
    }
}
