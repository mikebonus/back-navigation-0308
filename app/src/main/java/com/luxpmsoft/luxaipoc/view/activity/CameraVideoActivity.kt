package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.example.android.camerax.video.extensions.getAspectRatio
import com.example.android.camerax.video.extensions.getNameString
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.RecordVideo
import kotlinx.android.synthetic.main.activity_video_camera.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraVideoActivity : AppCompatActivity(), RecordVideo.onCaptureImage {
    private val captureLiveStatus = MutableLiveData<String>()
    private var previewView: PreviewView? = null
    private val cameraCapabilities = mutableListOf<CameraCapability>()
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private lateinit var recordingState: VideoRecordEvent
    var nameFolderFile = ""
    var from = ""
    var mediaStorageDir: File?= null
    private var cameraIndex = 0
    private var qualityIndex = DEFAULT_QUALITY_IDX
    private var isLimit = false
    private var mRecordingSecond = 0
    private val customHandler = Handler()
    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(this) }
    private var enumerationDeferred: Deferred<Unit>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_camera)
        MyUtils.setStatusBarTransparentFlagBlack(this)

        val bundle = intent.extras
        if (bundle != null) {
            from = bundle.getString("from").toString()
        }

        icBack.setOnClickListener {
            finish()
        }

        //photo
        buttonRecordVideo.setOnClickListener {
            if (!this@CameraVideoActivity::recordingState.isInitialized ||
                recordingState is VideoRecordEvent.Finalize)
            {
                startRecording()
                customHandler.postDelayed(updateTimerThread, 0)
            } else {
                when (recordingState) {
                    is VideoRecordEvent.Start -> {
                        currentRecording?.pause()
                        val recording = currentRecording
                        if (recording != null) {
                            recording.stop()
                            currentRecording = null
                        }
                    }
                    is VideoRecordEvent.Pause -> currentRecording?.resume()
                    is VideoRecordEvent.Resume -> currentRecording?.pause()
                    else -> throw IllegalStateException("recordingState in unknown state")
                }
            }
        }
        if (from == "body") {
            tvTime.text = "00:10"
        } else {
            tvTime.text = "00:00"
        }
        if (MyUtils.allPermissionsGranted(this)) {
            init()
        } else {
            ActivityCompat.requestPermissions(
                this, MyUtils.REQUIRED_PERMISSIONS, MyUtils.REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onResume() {
        super.onResume()
        MyUtils.unlockScreen(this)
    }
    fun init() {
        previewView = findViewById(R.id.previewView)
        initCameraFragment()
    }

    private val updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            mRecordingSecond++
            var secs: Int = mRecordingSecond % 60
            val mins: Int = mRecordingSecond / 60
            secs = secs % 60
            if (from == "body") {
                if (tvTime != null) tvTime.text = ("" + String.format("%02d", mins) + ":"
                        + String.format("%02d", 10-secs))
                if (secs == 10) {
                    currentRecording?.pause()
                    val recording = currentRecording
                    if (recording != null) {
                        recording.stop()
                        currentRecording = null
                    }
                }
            } else {
                if (tvTime != null) tvTime.text = ("" + String.format("%02d", mins) + ":"
                        + String.format("%02d", secs))
                if (mins == 1) {
                    currentRecording?.pause()
                    val recording = currentRecording
                    if (recording != null) {
                        recording.stop()
                        currentRecording = null
                    }
                }
            }

            customHandler.postDelayed(this, 1000)
        }
    }

    /**
     *   Always bind preview + video capture use case combinations in this sample
     *   (VideoCapture can work on its own). The function should always execute on
     *   the main thread.
     */
    private suspend fun bindCaptureUsecase() {
        val cameraProvider = ProcessCameraProvider.getInstance(applicationContext).get()

        val cameraSelector = getCameraSelector(cameraIndex)

        // create the user required QualitySelector (video resolution): we know this is
        // supported, a valid qualitySelector will be created.
        val quality = cameraCapabilities[cameraIndex].qualities[qualityIndex]
        val qualitySelector = QualitySelector.from(quality)

//        previewView?.updateLayoutParams<FrameLayout.LayoutParams> {
//            val orientation = resources.configuration.orientation
//            dimensionRatio = quality.getAspectRatioString(quality,
//                (orientation == Configuration.ORIENTATION_PORTRAIT))
//        }

        val preview = Preview.Builder()
            .setTargetAspectRatio(quality.getAspectRatio(quality))
            .build().apply {
                setSurfaceProvider(previewView?.surfaceProvider)
            }

        // build a recorder, which can:
        //   - record video/audio to MediaStore(only shown here), File, ParcelFileDescriptor
        //   - be used create recording(s) (the recording performs recording)
        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                videoCapture,
                preview
            )
        } catch (exc: Exception) {
            // we are on main thread, let's reset the controls on the UI.
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    /**
     * Kick start the video recording
     *   - config Recorder to capture to MediaStoreOutput
     *   - register RecordEvent Listener
     *   - apply audio request from user
     *   - start recording!
     * After this function, user could start/pause/resume/stop recording and application listens
     * to VideoRecordEvent for the current recording status.
     */
    @SuppressLint("MissingPermission")
    private fun startRecording() {
        // create MediaStoreOutputOptions for our recorder: resulting our recording!
        nameFolderFile = "Object_"+SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())+ ".mp4"

//        mediaStorageDir = MyUtils.getOutputFile(this)

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, nameFolderFile)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        // configure Recorder and Start recording to the mediaStoreOutput.
        currentRecording = videoCapture.output
            .prepareRecording(this, mediaStoreOutput)
            .apply {
//                if (audioEnabled) withAudioEnabled()
            }
            .start(mainThreadExecutor, captureListener)

        Log.i(TAG, "Recording started")
    }

    /**
     * CaptureEvent listener.
     */
    private val captureListener = Consumer<VideoRecordEvent> { event ->
        // cache the recording state
        if (event !is VideoRecordEvent.Status)
            recordingState = event

        updateUI(event)

        if (event is VideoRecordEvent.Finalize) {
            // display the captured video
            lifecycleScope.launch {
                if (from == "body") {
                    val intent = Intent(this@CameraVideoActivity, PreviewBodyVideoActivity::class.java)
                    intent.putExtra("pathFile", event.outputResults.outputUri.toString())
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@CameraVideoActivity, PreviewVideoActivity::class.java)
                    intent.putExtra("pathFile", event.outputResults.outputUri.toString())
                    if (from == "workout") {
                        intent.putExtra("from", from)
                    } else {
                        intent.putExtra("from", "preview")
                    }
                    startActivity(intent)
                    finish()
                }

            }
        }
    }

    /**
     * Retrieve the asked camera's type(lens facing type). In this sample, only 2 types:
     *   idx is even number:  CameraSelector.LENS_FACING_BACK
     *          odd number:   CameraSelector.LENS_FACING_FRONT
     */
    private fun getCameraSelector(idx: Int) : CameraSelector {
        if (cameraCapabilities.size == 0) {
            Log.i(TAG, "Error: This device does not have any camera, bailing out")
            finish()
        }
        return (cameraCapabilities[idx % cameraCapabilities.size].camSelector)
    }

    data class CameraCapability(val camSelector: CameraSelector, val qualities:List<Quality>)
    /**
     * Query and cache this platform's camera capabilities, run only once.
     */
    init {
        enumerationDeferred = lifecycleScope.async {
            whenCreated {
                val provider = ProcessCameraProvider.getInstance(this@CameraVideoActivity).get()

                provider.unbindAll()
                for (camSelector in arrayOf(
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    CameraSelector.DEFAULT_FRONT_CAMERA
                )) {
                    try {
                        // just get the camera.cameraInfo to query capabilities
                        // we are not binding anything here.
                        if (provider.hasCamera(camSelector)) {
                            val camera = provider.bindToLifecycle(this@CameraVideoActivity, camSelector)
                            QualitySelector
                                .getSupportedQualities(camera.cameraInfo)
                                .filter { quality ->
                                    listOf(Quality.FHD, Quality.HD, Quality.SD)
                                        .contains(quality)
                                }.also {
                                    cameraCapabilities.add(CameraCapability(camSelector, it))
                                }
                        }
                    } catch (exc: java.lang.Exception) {
                        Log.e(TAG, "Camera Face $camSelector is not supported")
                    }
                }
            }
        }
    }

    /**
     * One time initialize for CameraFragment (as a part of fragment layout's creation process).
     * This function performs the following:
     *   - initialize but disable all UI controls except the Quality selection.
     *   - set up the Quality selection recycler view.
     *   - bind use cases to a lifecycle camera, enable UI controls.
     */
    private fun initCameraFragment() {
//        initializeUI()
        lifecycleScope.launch {
            if (enumerationDeferred != null) {
                enumerationDeferred!!.await()
                enumerationDeferred = null
            }
            bindCaptureUsecase()
        }
    }

    /**
     * UpdateUI according to CameraX VideoRecordEvent type:
     *   - user starts capture.
     *   - this app disables all UI selections.
     *   - this app enables capture run-time UI (pause/resume/stop).
     *   - user controls recording with run-time UI, eventually tap "stop" to end.
     *   - this app informs CameraX recording to stop with recording.stop() (or recording.close()).
     *   - CameraX notify this app that the recording is indeed stopped, with the Finalize event.
     *   - this app starts VideoViewer fragment to view the captured result.
     */
    private fun updateUI(event: VideoRecordEvent) {
        val state = if (event is VideoRecordEvent.Status) recordingState.getNameString()
        else event.getNameString()
        when (event) {
            is VideoRecordEvent.Status -> {
                // placeholder: we update the UI with new status after this when() block,
                // nothing needs to do here.
            }
            is VideoRecordEvent.Start -> {
                buttonRecordVideo?.setImageResource(R.drawable.ic_start)
            }
            is VideoRecordEvent.Finalize-> {
                buttonRecordVideo?.setImageResource(R.drawable.ic_camera1)
            }
            is VideoRecordEvent.Pause -> {
                buttonRecordVideo?.setImageResource(R.drawable.ic_camera1)
            }
            is VideoRecordEvent.Resume -> {
                buttonRecordVideo?.setImageResource(R.drawable.ic_start)
            }
        }

        val stats = event.recordingStats
        val size = stats.numBytesRecorded / 1000
        val time = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(stats.recordedDurationNanos)
        var text = "${state}: recorded ${size}KB, in ${time}second"
        if(event is VideoRecordEvent.Finalize)
            text = "${text}\nFile saved to: ${event.outputResults.outputUri}"

        captureLiveStatus.value = text
        Log.i(TAG, "recording event: $text")
    }

    companion object {
        private const val TAG = "CameraActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, format + extension)
        // default Quality selection if no input from UI
        const val DEFAULT_QUALITY_IDX = 0
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MyUtils.REQUEST_CODE_PERMISSIONS) {
            if (MyUtils.allPermissionsGranted(this)) {
                init()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onRecordVideo(uri: Uri?) {

    }

    override fun onDestroy() {
        super.onDestroy()
        if(customHandler != null) {
            customHandler.removeCallbacks(updateTimerThread)
        }
    }
}