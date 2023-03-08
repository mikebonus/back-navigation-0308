package com.luxpmsoft.luxaipoc.service

import android.annotation.SuppressLint
import android.app.*
import android.media.MediaPlayer
import android.net.Uri
import android.content.Intent
import android.util.Log
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.media.AudioAttributes
import android.content.Context
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.*
import android.provider.Settings
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.luxpmsoft.luxaipoc.R
import java.util.*
import java.util.concurrent.TimeUnit

class CallNotificationService: Service(), MediaPlayer.OnPreparedListener {
    private val CHANNEL_ID: String = "CallChannel"
    private val CHANNEL_NAME: String = "Call Channel"
    var mediaPlayer:MediaPlayer? = null
    var mvibrator: Vibrator? = null
    var audioManager: AudioManager? = null
    var playbackAttributes: AudioAttributes? = null
    private var handler: Handler? = null
    var afChangeListener: OnAudioFocusChangeListener? = null
    private var status = false
    private var vstatus = false


    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var data: Bundle? = null
        var name: String? = ""
        var callType: String? = ""
        val NOTIFICATION_ID = 120
        try {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audioManager != null) {
                when (audioManager!!.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> status = true
                    AudioManager.RINGER_MODE_SILENT -> status = false
                    AudioManager.RINGER_MODE_VIBRATE -> {
                        status = false
                        vstatus = true
                        Log.e("Service!!", "vibrate mode")
                    }
                }
            }
            if (status) {
                val delayedStopRunnable = Runnable { releaseMediaPlayer() }
                afChangeListener = OnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        // Permanent loss of audio focus
                        // Pause playback immediately
                        //mediaController.getTransportControls().pause();
                        if (mediaPlayer != null) {
                            if (mediaPlayer?.isPlaying!!) {
                                mediaPlayer?.pause()
                            }
                        }
                        // Wait 30 seconds before stopping playback
                        handler?.postDelayed(
                            delayedStopRunnable,
                            TimeUnit.SECONDS.toMillis(2)
                        )
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        // Pause playback
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        // Lower the volume, keep playing
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        // Your app has been granted audio focus again
                        // Raise volume to normal, restart playback if necessary
                    }
                }
                val keyguardManager =
                    getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
                mediaPlayer?.isLooping = true
                //mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    handler = Handler()
                    playbackAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    val focusRequest =
                        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes!!)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(afChangeListener!!, handler!!)
                            .build()
                    val res = audioManager!!.requestAudioFocus(focusRequest)
                    if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        if (!keyguardManager.isDeviceLocked) {
                            mediaPlayer?.start()
                        }
                    }
                } else {

                    // Request audio focus for playback
                    val result = audioManager!!.requestAudioFocus(
                        afChangeListener,  // Use the music stream.
                        AudioManager.STREAM_MUSIC,  // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                    )
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        if (!keyguardManager.isDeviceLocked) {
                            // Start playback
                            mediaPlayer?.start()
                        }
                    }
                }
            } else if (vstatus) {
                mvibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                // Start without a delay
                // Each element then alternates between vibrate, sleep, vibrate, sleep...
                val pattern = longArrayOf(
                    0, 250, 200, 250, 150, 150, 75,
                    150, 75, 150
                )

                // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
                mvibrator!!.vibrate(pattern, -1)
                Log.e("Service!!", "vibrate mode start")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (intent != null && intent.extras != null) {
            data = intent.extras
            name = data!!.getString("title")
            callType = data!!.getString("call_type")
        }
        try {
            val callDialogAction = Intent(
                applicationContext,
                CallNotificationActionReceiver::class.java
            )
            callDialogAction.putExtra("ACTION_TYPE", "DIALOG_CALL")
            callDialogAction.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            callDialogAction.action = "DIALOG_CALL"
            var callDialogPendingIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    applicationContext,
                    1202,
                    callDialogAction,
                    PendingIntent.FLAG_MUTABLE
                )
            } else {
                    PendingIntent.getBroadcast(
                        applicationContext,
                        1202,
                        callDialogAction,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
            }
            createChannel()

            var notificationBuilder: NotificationCompat.Builder? = null
            if (data != null) {
                // Uri ringUri= Settings.System.DEFAULT_RINGTONE_URI;
                notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(name)
                    .setContentText("$callType")
                    .setSmallIcon(R.drawable.logo_lux3d)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
//                    .addAction(
//                        R.drawable.ic_call_decline,
//                        getString(R.string.reject_call),
//                        cancelCallPendingIntent
//                    )
//                    .addAction(
//                        R.drawable.ic_call_accept,
//                        getString(R.string.answer_call),
//                        receiveCallPendingIntent
//                    )
                    .setAutoCancel(true) //.setSound(ringUri)
                    .setFullScreenIntent(callDialogPendingIntent, true)
            }

            var incomingCallNotification: Notification? = null
            if (notificationBuilder != null) {
                incomingCallNotification = notificationBuilder.build()
            }

            startForeground(NOTIFICATION_ID, incomingCallNotification)
//            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy() // release your media player here audioManager.abandonAudioFocus(afChangeListener);
        releaseMediaPlayer()
        releaseVibration()
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val ringUri: Uri = Settings.System.DEFAULT_RINGTONE_URI
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = "Call Notifications"
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                Objects.requireNonNull(
                    applicationContext.getSystemService(
                        NotificationManager::class.java
                    )
                ).createNotificationChannel(channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun releaseVibration() {
        try {
            if (mvibrator != null) {
                if (mvibrator!!.hasVibrator()) {
                    mvibrator!!.cancel()
                }
                mvibrator = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying!!) {
                    mediaPlayer?.stop()
                    mediaPlayer?.reset()
                    mediaPlayer?.release()
                }
                mediaPlayer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {}
}