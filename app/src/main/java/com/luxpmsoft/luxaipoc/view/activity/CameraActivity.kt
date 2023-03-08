package com.luxpmsoft.luxaipoc.view.activity

import android.Manifest
import android.R.attr
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.media.MediaActionSound
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.widget.CameraPreview
import com.luxpmsoft.luxaipoc.widget.RecordVideo
import kotlinx.android.synthetic.main.activity_camera1.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.window.WindowManager
import com.luxpmsoft.luxaipoc.api.Utils.afterMeasured
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import android.widget.Toast

/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Double) -> Unit

class CameraActivity : AppCompatActivity(), RecordVideo.onCaptureImage, SensorEventListener {
    private var mPreview: CameraPreview? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mSessionId: String? = null
    private var mNoOfPhoto: Int = 0
    private var startHTime = 0L
    private val customHandler: Handler = Handler()
    private var lightSensor: Sensor? = null
    private var lightSensorManager: SensorManager? = null
    var timerTextView: TextView? = null
    var tvNumberImage: TextView? = null
    var tvDone: TextView? = null
    var imvPreview: ImageView? = null
    var imgAfter: ImageView? = null
    var rgChoose: RadioGroup? = null
    var currentValueSensorX: Float = 0F
    var currentValueSensorY: Float = 0F
    var checkCapture: Int = 0
    var timeInMilliseconds = 0L
    var timeSwapBuff = 0L
    var updatedTime = 0L
    var isCheckVideo = false
    var name = ""
    var nameFolderFile = ""
    var mediaStorageDir: File?= null
    var recordVideo: RecordVideo? = null

    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var viewFinder: PreviewView? = null
    private lateinit var windowManager: WindowManager
    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    var isDialogShow = false
    private val displayManager by lazy {
        getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }
    private val sound = MediaActionSound()
    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private var mProgressDialog: ProgressDialog? = null
    private var isCapture = false
    private var isAuto = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera1)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog?.setMessage("Uploading ...")
        MyUtils.setStatusBarTransparentFlagBlack(this)
        val bundle = intent.extras
        if (bundle != null) {
            mSessionId = bundle.getString("session_id")
            Log.d(TAG, "session_id = ${mSessionId}")
        }

        lightSensorManager = getSystemService(
            SENSOR_SERVICE
        ) as SensorManager

        lightSensor = lightSensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (recordVideo == null) {
            recordVideo = RecordVideo(this, this, getPackageName())
        }

//        startCamera()

        if (!Utils.checkPermission(this)) {
            recordVideo!!.initPermission()
        }
        val captureButton: ImageView = findViewById(R.id.button_capture)
//        timerTextView = findViewById(R.id.tvTime)
        nameFolderFile = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        mediaStorageDir = MyUtils.getOutputDirectory(this, nameFolderFile)

        tvNumberImage = findViewById(R.id.tvNumberImage)
        tvDone = findViewById(R.id.tvDone)
        imvPreview = findViewById(R.id.imv_preview)
        imgAfter = findViewById(R.id.imgAfter)
        rgChoose = findViewById(R.id.rgChoose)

        lineAuto.setOnClickListener {
            isAuto = true
            lineAuto.background = resources.getDrawable(R.drawable.bg_blue_topleft_bottomleft)
            lineCapture.background = resources.getDrawable(R.drawable.bg_blue_topright_bottomright)
        }

        lineCapture.setOnClickListener {
            isAuto = false
            lightSensorManager!!.unregisterListener(this)
            captureButton.setImageResource(R.drawable.ic_camera1)
            lineAuto.background = resources.getDrawable(R.drawable.bg_blue_topleft_bottomleft_1)
            lineCapture.background = resources.getDrawable(R.drawable.bg_blue_topright_bottomright_1)
        }

        icBack.setOnClickListener {
            finish()
        }

        tvDone?.setOnClickListener {
            val intent = Intent(this@CameraActivity, ListImageActivity::class.java)
            intent.putExtra("pathFile", mediaStorageDir?.path)
            startActivity(intent)
            finish()

        }

            //photo
        captureButton.setOnClickListener {
//            if (Utils.checkPermission(this)) {
//                releaseMediaRecorder() // release the MediaRecorder object
//                // get an image from the camera
//                try {
//                    mCamera?.takePicture(null, null, mPicture)
//                } catch(e:Exception) {
//                    e.message
//                }
//
//            } else {
//                recordVideo!!.initPermission()
//            }
            if (isAuto) {
                if(!isCapture) {
                    if (lightSensor != null) {
                        lightSensor?.also { sensor ->
                            lightSensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL,
                            SensorManager.SENSOR_DELAY_UI)
                        }
                    }

                    isCapture = true
                    // Set button to stop
                    captureButton.setImageResource(R.drawable.ic_stop_recording)

                } else {
                    lightSensorManager!!.unregisterListener(this)
                    isCapture = false
                    captureButton.setImageResource(R.drawable.ic_camera1)
                }
            } else {
                takePhoto()
            }
        }

//        progressBarMin.progress = 0
//        progressBarMin.secondaryProgress = 30
//        progressBarMin.max = 30
        init()
    }

    fun init() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        viewFinder = findViewById(R.id.viewFinder)
        if (MyUtils.allPermissionsGranted(this)) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, MyUtils.REQUIRED_PERMISSIONS, MyUtils.REQUEST_CODE_PERMISSIONS
            )
        }

        // Load sound
//        sound.load(MediaActionSound.SHUTTER_CLICK)
    }


    override fun onResume() {
        super.onResume()
        startCamera()
        MyUtils.unlockScreen(this)
    }

    override fun onPause() {
        super.onPause()
//        releaseMediaRecorder() // if you are using MediaRecorder, release it first
//        releaseCamera() // release the camera immediately on pause event
    }

    private val updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime
            updatedTime = timeSwapBuff + timeInMilliseconds
            var secs = (updatedTime / 1000).toInt()
            val mins = secs / 60
            secs = secs % 60
            if (timerTextView != null) timerTextView!!.text =
                ("" + String.format("%02d", mins) + ":"
                        + String.format("%02d", secs))
            customHandler.postDelayed(this, 0)
        }
    }

    override fun onRecordVideo(uri: Uri?) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        recordVideo!!.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item)
    }

    fun autoFocus() {
        viewFinder?.afterMeasured {
            val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
                .createPoint(.5f, .5f)
            try {
                val autoFocusAction = FocusMeteringAction.Builder(
                    autoFocusPoint,
                    FocusMeteringAction.FLAG_AF
                ).apply {
                    //start auto-focusing after 2 seconds
                    setAutoCancelDuration(1, TimeUnit.SECONDS)
                }.build()
                camera?.cameraControl?.startFocusAndMetering(autoFocusAction)
            } catch (e: CameraInfoUnavailableException) {
                Log.d("ERROR", "cannot access camera", e)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun focusOnTouch() {
        viewFinder?.afterMeasured {
            viewFinder?.setOnTouchListener { _, event ->
                return@setOnTouchListener when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                            viewFinder?.width!!.toFloat(), viewFinder?.height!!.toFloat()
                        )
                        val autoFocusPoint = factory.createPoint(event.x, event.y)
                        try {
                            camera?.cameraControl!!.startFocusAndMetering(
                                FocusMeteringAction.Builder(
                                    autoFocusPoint,
                                    FocusMeteringAction.FLAG_AF
                                ).apply {
                                    //focus only when the user tap the preview
                                    disableAutoCancel()
                                }.build()
                            )
                        } catch (e: CameraInfoUnavailableException) {
                            Log.d("ERROR", "cannot access camera", e)
                        }
                        true
                    }
                    else -> false // Unhandled event.
                }
            }
        }
    }

    private fun calculateFocusArea(x: Float, y: Float): Rect {
        val focusAreaSize = 300
        val left: Int = clamp(
            java.lang.Float.valueOf(x / mPreview!!.getWidth() * 2000 - 1000).toInt(),
            focusAreaSize
        )
        val top: Int = clamp(
            java.lang.Float.valueOf(y / mPreview!!.getHeight() * 2000 - 1000).toInt(),
            focusAreaSize
        )
        return Rect(left, top, left + focusAreaSize, top + focusAreaSize)
    }

    private fun clamp(touchCoordinateInCameraReper: Int, focusAreaSize: Int): Int {
        val result: Int
        result = if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                1000 - focusAreaSize / 2
            } else {
                -1000 + focusAreaSize / 2
            }
        } else {
            touchCoordinateInCameraReper - focusAreaSize / 2
        }
        return result
    }

    //    /**
//     * We need a display listener for orientation changes that do not trigger a configuration
//     * change, for example if we choose to override config change in manifest or for 180-degree
//     * orientation changes.
//     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = let { view ->
            if (displayId == this@CameraActivity.displayId) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    imageCapture?.targetRotation = display!!.rotation
                    imageAnalyzer?.targetRotation = display!!.rotation
                }
            }
        } ?: Unit
    }

    private fun takePhoto() {
//
//        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(Date())
        name = "IMG_$timeStamp"

        imageCapture?.let { imageCapture ->

            // Create output file to hold the image
            val photoFile = createFile(mediaStorageDir!!, name, PHOTO_EXTENSION)

            // Setup image capture metadata
            val metadata = ImageCapture.Metadata().apply {

                // Mirror image when using the front camera
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            enabledDone()

            // Setup image capture listener which is triggered after photo has been taken
            imageCapture.takePicture(
                outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.cause?.message}", exc)
                        exc.cause?.message?.also {
                            runOnUiThread {
                                if (it.contains("ENOSPC")) {
                                    Toast.makeText(this@CameraActivity, "Storage is full", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        Log.d(TAG, "Photo capture succeeded: $savedUri")
                        mNoOfPhoto++
//                        progressBarMin.progress = mNoOfPhoto
                        enabledDone()
                        runOnUiThread {
                            tvNumberImage?.text = mNoOfPhoto.toString()

//                            sound.play(MediaActionSound.SHUTTER_CLICK)

//                            if(savedUri != null) {
//                                Glide.with(this@CameraActivity)
//                                    .load(savedUri)
//                                    .centerCrop()
//                                    .into(imvPreview!!)
//                            }

                            if (photoFile.exists()) {
                                val myBitmap: Bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath())
                                imvPreview!!.setImageBitmap(MyUtils.rotateBitmap(myBitmap, 90F))
                            }
                        }

                    }
                })
        }
    }


    fun enabledDone() {
        runOnUiThread {
            if (mNoOfPhoto == 2) {
                imgAfter?.visibility = View.VISIBLE
            }
            if(mNoOfPhoto == 40 && !isDialogShow) {
                tvDone?.visibility = View.VISIBLE
                isDialogShow = true
                DialogFactory.dialogNoticeScan(
                    this,
                    object : DialogFactory.Companion.DialogListener.ScanObject {
                        override fun continueScan() {
                        }

                        override fun uploadObject() {
                            val intent = Intent(this@CameraActivity, ListImageActivity::class.java)
                            intent.putExtra("pathFile", mediaStorageDir?.path)
                            startActivity(intent)
                            finish()
                        }
                    })
            }
        }
    }

    private fun startCamera() {
//        cameraExecutor = Executors.newSingleThreadExecutor()

        // Every time the orientation of device changes, update rotation for use cases
        displayManager.registerDisplayListener(displayListener, null)

        //Initialize WindowManager to retrieve display metrics
        windowManager = WindowManager(this)
        // Wait for the views to be properly laid out
        viewFinder?.post {

            // Keep track of the display in which this view is attached
            displayId = viewFinder?.display!!.displayId

            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener(Runnable {

                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Select lensFacing depending on the available cameras
                lensFacing = when {
                    hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                    hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                    else -> throw IllegalStateException("Back and front camera are unavailable")
                }

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = windowManager.getCurrentWindowMetrics().bounds
        Log.d(TAG, "Screen metrics: ${metrics.width()} x ${metrics.height()}")

        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = viewFinder?.display!!.rotation

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
//            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    // Values returned from our analyzer are passed to the attached listener
                    // We log image analysis results here - you should do something useful
                    // instead!
                    Log.d(TAG, "Average luminosity: $luma")
                })
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer)

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(viewFinder?.surfaceProvider)
            observeCameraState(camera?.cameraInfo!!)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
        autoFocus()
        focusOnTouch()
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        sound.release()
    }

    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(this) { cameraState ->
            run {
                when (cameraState.type) {
                    CameraState.Type.PENDING_OPEN -> {
                        // Ask the user to close other camera apps
                    }
                    CameraState.Type.OPENING -> {
                        // Show the Camera UI
                    }
                    CameraState.Type.OPEN -> {
                        // Setup Camera resources and begin processing
                    }
                    CameraState.Type.CLOSING -> {
                        // Close camera UI
                    }
                    CameraState.Type.CLOSED -> {
                        // Free camera resources
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                        // Make sure to setup the use cases properly
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                        // Close the camera or ask user to close another camera app that's using the
                        // camera
                    }
                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
                    }
                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                        // Ask the user to enable the device's cameras
                    }
                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                        // Ask the user to reboot the device to restore camera function
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
                    }
                }
            }
        }
    }

    /**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     */

    private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listeners.forEach { it(luma) }

            image.close()
        }
    }

    companion object {
        private const val TAG = "CameraActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, format + extension)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        recordVideo!!.onRequestPermissionsResult(requestCode, grantResults)
        if (requestCode == MyUtils.REQUEST_CODE_PERMISSIONS) {
            if (MyUtils.allPermissionsGranted(this)) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentValueX: Float = event?.values!!.get(0)
        val currentValueY: Float = event?.values!!.get(1)
        if (Math.abs(currentValueSensorX - currentValueX) > 1.5 ||
            Math.abs(currentValueSensorY - currentValueY) > 1.5) {
                if (checkCapture == 0) {
                    takePhoto()
                }
                if (checkCapture == 2) {
                    checkCapture = 0
                    takePhoto()
                }
            checkCapture++
            currentValueSensorX = currentValueX
            currentValueSensorY = currentValueY
        }
        Log.e("onSensorChanged", currentValueX.toString()+" "+currentValueY.toString())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.e("onSensorChanged1", accuracy.toString())
    }

    override fun onStart() {
        super.onStart()
        if (isCapture && lightSensor != null) {
            lightSensor?.also { sensor ->
                lightSensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        lightSensorManager!!.unregisterListener(this)
    }
}