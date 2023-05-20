package com.example.safecross

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.safecross.databinding.ActivitySettingBinding
import com.example.safecross.fragment.HomeFragment
import com.example.safecross.fragment.NotesFragment

class SettingActivity : AppCompatActivity() {

    private val CHANNEL_ID ="channel_id_example_01"
    private val notificationId = 101

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
         val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        val switch =binding.Switch





        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                sendNotification()
            }
            else{
                Toast.makeText(this,"allow Notification",Toast.LENGTH_SHORT).show()
            }

        }




        createNotificationChannel()
    }
    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val important = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID,name,important).apply {
                description = descriptionText
            }
            val notificationManager :NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)


        }
    }
    private fun sendNotification(){
        val intent = Intent(this,MainActivity::class.java)


        val run = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
        )



        val bitmap = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.google_logo)
        val bitmapLargeIcon = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.twitter_logo)




        val builder = NotificationCompat.Builder(this,CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Something is Coming")
            .setContentText("Lower The Speed of Car")
            .setLargeIcon(bitmapLargeIcon)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
            //.setStyle(NotificationCompat.BigTextStyle().bigText("Much Longer Text that cannot fit one line so we extend it ot much much longer"))
            .setContentIntent(run)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)



        with(NotificationManagerCompat.from(this)){
            if (ActivityCompat.checkSelfPermission(
                    this@SettingActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling



                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(notificationId,builder.build())
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}