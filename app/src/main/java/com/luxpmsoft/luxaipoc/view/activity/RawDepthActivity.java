/*
 * Copyright 2021 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luxpmsoft.luxaipoc.view.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.location.Location;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.CameraIntrinsics;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.ImageMetadata;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.MetadataNotFoundException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.RecordingFailedException;
import com.luxpmsoft.luxaipoc.R;
import com.luxpmsoft.luxaipoc.common.helpers.CameraPermissionHelper;
import com.luxpmsoft.luxaipoc.common.helpers.DisplayRotationHelper;
import com.luxpmsoft.luxaipoc.common.helpers.FullScreenHelper;
import com.luxpmsoft.luxaipoc.common.helpers.SnackbarHelper;
import com.luxpmsoft.luxaipoc.common.helpers.TrackingStateHelper;
import com.luxpmsoft.luxaipoc.common.rendering.BackgroundRenderer;
import com.luxpmsoft.luxaipoc.common.rendering.DepthRenderer;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.luxpmsoft.luxaipoc.model.Device;
import com.luxpmsoft.luxaipoc.model.Odometry;
import com.luxpmsoft.luxaipoc.model.SessionInfo;
import com.luxpmsoft.luxaipoc.model.Stream;
import com.luxpmsoft.luxaipoc.rawdepth.DepthData;
import com.luxpmsoft.luxaipoc.utils.MyUtils;
import com.luxpmsoft.luxaipoc.widget.CurrentLocation;
import com.tapadoo.alerter.Alerter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore Raw Depth API. The application will show 3D point-cloud data of the environment.
 */
public class RawDepthActivity extends AppCompatActivity implements GLSurfaceView.Renderer, CurrentLocation.OnLocationResolved {
  private static final String TAG = RawDepthActivity.class.getSimpleName();

  // Rendering. The Renderers are created here, and initialized when the GL surface is created.
  private GLSurfaceView surfaceView;

  private boolean installRequested;
  private boolean isRecording = false;

  private Session session;
  private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
  private DisplayRotationHelper displayRotationHelper;

  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private final DepthRenderer depthRenderer = new DepthRenderer();

  private ImageButton btnRecord;
  private ImageView icBack;
  private TextView tvTime;

  private String mFolderName;
  private String width = "", height="";
  private File mDepthFile;
  private File mConfidenceFile;
  private File mImageDirectory;

  private FileOutputStream mFosDepthData;
  private FileOutputStream mFosConfidenceData;

  private Integer mRawImgWidth = 0;
  private Integer mRawImgHeight = 0;
  private Integer mNoFrame = 0;
  private Integer mRecordingSecond = 0;

  private List<Odometry> mListOdometry;
  private Handler customHandler = new Handler();
  private CameraConfig cameraConfig = null;
  private boolean isLimit = false;
  CurrentLocation currentLocation = null;
  FusedLocationProviderClient mFusedLocationClient = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    surfaceView = findViewById(R.id.surfaceview);
    displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

    // Set up renderer.
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);

    tvTime = findViewById(R.id.tvTime);
    btnRecord = findViewById(R.id.btn_record);
    icBack = findViewById(R.id.icBack);
    btnRecord.setOnClickListener(view -> {
      customHandler.postDelayed(updateTimerThread, 0);
      recordVideo();
    });

    installRequested = false;

    btnRecord.setVisibility(View.INVISIBLE);

    icBack.setOnClickListener(view -> {
      finish();
    });

    Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      width = bundle.getString("width");
      height = bundle.getString("height");
    } else {
      width = "640";
      height = "480";
    }
    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    currentLocation = new CurrentLocation(this, mFusedLocationClient, this);
    LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
            new IntentFilter("notification"));
  }

  private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      // Get extra data included in the Intent
      String title = intent.getStringExtra("title");
      String body = intent.getStringExtra("body");
      Alerter.create(RawDepthActivity.this)
       .setTitle(title)
              .setText(body)
              .setBackgroundColorRes(R.color.blue1)
              .setDuration(2000)
              .show();
    }
  };


  @Override
  protected void onResume() {
    super.onResume();

    if (session == null) {
      Exception exception = null;
      String message = null;
      try {
        switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
          case INSTALL_REQUESTED:
            installRequested = true;
            return;
          case INSTALLED:
            break;
        }

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
          CameraPermissionHelper.requestCameraPermission(this);
          return;
        }

        // Creates the ARCore session.
        session = new Session(/* context= */ this);
        if (!session.isDepthModeSupported(Config.DepthMode.RAW_DEPTH_ONLY)) {
          message =  "This device does not support the ARCore Raw Depth API. See" +
                          "https://developers.google.com/ar/devices for a list of devices that do.";
        }

      } catch (UnavailableArcoreNotInstalledException
          | UnavailableUserDeclinedInstallationException e) {
        message = "Please install ARCore";
        exception = e;
      } catch (UnavailableApkTooOldException e) {
        message = "Please update ARCore";
        exception = e;
      } catch (UnavailableSdkTooOldException e) {
        message = "Please update this app";
        exception = e;
      } catch (UnavailableDeviceNotCompatibleException e) {
        message = "This device does not support AR";
        exception = e;
      } catch (Exception e) {
        message = "Failed to create AR session";
        exception = e;
      }

      if (message != null) {
        messageSnackbarHelper.showError(this, message);
        Log.e(TAG, "Exception creating session", exception);
        return;
      }

      MyUtils.Companion.unlockScreen(this);
    }

    try {
      // Enable raw depth estimation and auto focus mode while ARCore is running.
      Config config = session.getConfig();
      config.setDepthMode(Config.DepthMode.RAW_DEPTH_ONLY);
      config.setFocusMode(Config.FocusMode.AUTO);
      session.configure(config);
      for (int i=0; i<session.getSupportedCameraConfigs().size(); i++) {
        if (String.valueOf(session.getSupportedCameraConfigs().get(i).getImageSize().getWidth()).equals(width) &&
                String.valueOf(session.getSupportedCameraConfigs().get(i).getImageSize().getHeight()).equals(height)) {
          session.setCameraConfig(session.getSupportedCameraConfigs().get(i));
          cameraConfig = session.getSupportedCameraConfigs().get(i);
        }
      }
      session.resume();
    } catch (CameraNotAvailableException e) {
      messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
      session = null;
      return;
    }

    // Note that order matters - see the note in onPause(), the reverse applies here.
    surfaceView.onResume();
    displayRotationHelper.onResume();
    messageSnackbarHelper.showMessage(this, "Waiting for depth data...");

    tvTime.setText("00:00");
  }

  @Override
  public void onPause() {
    super.onPause();
    if (session != null) {
      // Note that the order matters - GLSurfaceView is paused first so that it does not try
      // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
      // still call session.update() and get a SessionPausedException.
      displayRotationHelper.onPause();
      surfaceView.onPause();
      session.pause();
    }
  }

  public void startUpload(Location location) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.i(TAG, "Stop record video");
        customHandler.removeCallbacks(updateTimerThread);
        // Set button to record
        btnRecord.setImageResource(R.drawable.ic_camera1);

        // Stop recording
        try {
          isRecording = false;

          mFosDepthData.close();
          mFosConfidenceData.close();

          session.stopRecording();

        } catch (RecordingFailedException e) {
          e.printStackTrace();
          Log.e(TAG, "Failed to stop recording", e);
        } catch (Exception e) {
          e.printStackTrace();
          Log.e(TAG, "Exception", e);
        }

        buildSessionInfo();
        writeOdometryToFile();
        URI destination = MyUtils.Companion.getOutputSceneDirectory(RawDepthActivity.this, mFolderName).toURI();
        startUploading(destination.getPath(), location);
      }
    });
  }

  private Runnable updateTimerThread = new Runnable() {
    public void run() {

      mRecordingSecond++;

      int secs = mRecordingSecond % 60;
      int mins = mRecordingSecond / 60;
      secs = secs % 60;
      if (tvTime != null) {
        tvTime.setText("" + String.format("%02d", mins) + ":"
                + String.format("%02d", secs));
        customHandler.postDelayed(this, 1000);
      }

      if (mins == 5) {
        currentLocation.getLastLocation();
      }
    }
  };

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
    super.onRequestPermissionsResult(requestCode, permissions, results);
    if (!CameraPermissionHelper.hasCameraPermission(this)) {
      Toast.makeText(this, "Camera permission is needed to run this application",
              Toast.LENGTH_LONG).show();
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        CameraPermissionHelper.launchPermissionSettings(this);
      }
      finish();
    }

    currentLocation.onRequestPermissionsResult(requestCode, permissions, results);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
    try {
      // Create the texture and pass it to ARCore session to be filled during update().
      backgroundRenderer.createOnGlThread(/*context=*/ this);
      depthRenderer.createOnGlThread(/*context=*/ this);

    } catch (IOException e) {
      Log.e(TAG, "Failed to read an asset file", e);
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    displayRotationHelper.onSurfaceChanged(width, height);
    GLES20.glViewport(0, 0, width, height);
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    // Clear screen to notify driver it should not load any pixels from previous frame.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (session == null) {
      return;
    }
    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session);

    try {
      session.setCameraTextureName(backgroundRenderer.getTextureId());

      // Obtain the current frame from ARSession. When the configuration is set to
      // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
      // camera framerate.
      Frame frame = session.update();
      Camera camera = frame.getCamera();

      // If frame is ready, render camera preview image to the GL surface.
      backgroundRenderer.draw(frame);

      // Retrieve the depth data for this frame.
      // Depth image is in uint16, at GPU aspect ratio, in native orientation.
      Image depthImage = frame.acquireRawDepthImage();
      // Confidence image is in uint8, matching the depth image size.
      Image confidenceImage = frame.acquireRawDepthConfidenceImage();

      if (isRecording) {
        if (null != depthImage) {
          if (mRawImgWidth == 0) {
            mRawImgWidth = depthImage.getWidth();
            mRawImgHeight = depthImage.getHeight();
          }

          ByteBuffer depthData = depthImage.getPlanes()[0].getBuffer();
          ByteBuffer confidenceData = confidenceImage.getPlanes()[0].getBuffer();

          ByteBuffer buffer = depthData.order(ByteOrder.nativeOrder());
//          byte[] data = new byte[buffer.remaining()];
//          buffer.get(data);
          byte[] data = new byte[buffer.capacity()];
          while (buffer.hasRemaining()) {
            buffer.get(data);
          }
          mFosDepthData.write(data);

          ByteBuffer confidenceBuffer = confidenceData.order(ByteOrder.nativeOrder());
//          byte[] confidenceDataBytes = new byte[confidenceBuffer.remaining()];
//          confidenceBuffer.get(confidenceDataBytes);
          byte[] confidenceDataBytes = new byte[confidenceBuffer.capacity()];
          while (confidenceBuffer.hasRemaining()) {
            confidenceBuffer.get(confidenceDataBytes);
          }
          mFosConfidenceData.write(confidenceDataBytes);

          // Build odometry object
          buildOdometryObjectAndImage(depthImage, frame);
        }

      }

      FloatBuffer points = DepthData.create(frame, session.createAnchor(camera.getPose()), depthImage, confidenceImage);
      if (points == null) {
        return;
      }

      depthImage.close();
      confidenceImage.close();

      if (messageSnackbarHelper.isShowing() && points != null) {
        messageSnackbarHelper.hide(this);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            btnRecord.setVisibility(View.VISIBLE);
          }
        });
      }

      // If not tracking, show tracking failure reason instead.
      if (camera.getTrackingState() == TrackingState.PAUSED) {
        messageSnackbarHelper.showMessage(
            this, TrackingStateHelper.getTrackingFailureReasonString(camera));
        return;
      }

      // Visualize depth points.
      depthRenderer.update(points);
      depthRenderer.draw(camera);

    } catch (NotYetAvailableException e) {
      // This normally means that depth data is not available yet.
      // This is normal, so you don't have to spam the logcat with this.
//      Log.e(TAG, "NotYetAvailableException", e);
    } catch (Throwable t) {
      // Avoid crashing the application due to unhandled exceptions.
      Log.e(TAG, "Exception on the OpenGL thread", t);
    }
  }

  void writeCameraImageToFile(Image cameraImage) {
    //The camera image received is in YUV YCbCr Format.
    // Get buffers for each of the planes and use them to create a new bytearray defined by the size of all three buffers combined
//    ByteBuffer cameraPlaneY = cameraImage.getPlanes()[0].getBuffer();
//    ByteBuffer cameraPlaneU = cameraImage.getPlanes()[1].getBuffer();
//    ByteBuffer cameraPlaneV = cameraImage.getPlanes()[2].getBuffer();
//
////Use the buffers to create a new byteArray that
//    byte[] compositeByteArray = new byte[cameraPlaneY.capacity() + cameraPlaneU.capacity() + cameraPlaneV.capacity()];
//
//    cameraPlaneY.get(compositeByteArray, 0, cameraPlaneY.capacity());
//    cameraPlaneU.get(compositeByteArray, cameraPlaneY.capacity(), cameraPlaneU.capacity());
//    cameraPlaneV.get(compositeByteArray, cameraPlaneY.capacity() + cameraPlaneU.capacity(), cameraPlaneV.capacity());
//
//    YuvImage yuvImage = new YuvImage(compositeByteArray, ImageFormat.NV21, cameraImage.getWidth(), cameraImage.getHeight(), null);
//
//    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//    yuvImage.compressToJpeg(new Rect(0, 0, cameraImage.getWidth(), cameraImage.getHeight()), 100, byteArrayOutputStream);
//
//    byte[] byteForBitmap = byteArrayOutputStream.toByteArray();
//    Bitmap bitmap = BitmapFactory.decodeByteArray(byteForBitmap, 0, byteForBitmap.length);

    try {
      File jpgImageile = new File(mImageDirectory, "image" + mNoFrame + ".jpg");
      if (!jpgImageile.exists()) {
        jpgImageile.createNewFile();
      }
      FileOutputStream fosImageData = new FileOutputStream(jpgImageile);
      fosImageData.write(toJpegImage(cameraImage, 100));
      fosImageData.close();
    } catch (IOException e) {
      e.printStackTrace();
    }


//    val baOutputStream = ByteArrayOutputStream()
//    val yuvImage: YuvImage = YuvImage(compositeByteArray, ImageFormat.NV21, cameraImage.width, cameraImage.height, null)
//    yuvImage.compressToJpeg(Rect(0, 0, cameraImage.width, cameraImage.height), 75, baOutputStream)
//    val byteForBitmap = baOutputStream.toByteArray()
//    val bitmap = BitmapFactory.decodeByteArray(byteForBitmap, 0, byteForBitmap.size)
  }

  byte[] toJpegImage(Image image, int imageQuality) {
    if (image.getFormat() != ImageFormat.YUV_420_888) {
      throw new IllegalArgumentException("Invalid image format");
    }

    YuvImage yuvImage = toYuvImage(image);
    int width = image.getWidth();
    int height = image.getHeight();

    // Convert to jpeg
    byte[] jpegImage = null;
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      yuvImage.compressToJpeg(new Rect(0, 0, width, height), imageQuality, out);
      jpegImage = out.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return jpegImage;
  }

  YuvImage toYuvImage(Image image) {
    if (image.getFormat() != ImageFormat.YUV_420_888) {
      throw new IllegalArgumentException("Invalid image format");
    }

    int width = image.getWidth();
    int height = image.getHeight();

    // Order of U/V channel guaranteed, read more:
    // https://developer.android.com/reference/android/graphics/ImageFormat#YUV_420_888
    Image.Plane yPlane = image.getPlanes()[0];
    Image.Plane uPlane = image.getPlanes()[1];
    Image.Plane vPlane = image.getPlanes()[2];

    ByteBuffer yBuffer = yPlane.getBuffer();
    ByteBuffer uBuffer = uPlane.getBuffer();
    ByteBuffer vBuffer = vPlane.getBuffer();

    // Full size Y channel and quarter size U+V channels.
    int numPixels = (int) (width * height * 1.5f);
    byte[] nv21 = new byte[numPixels];
    int index = 0;

    // Copy Y channel.
    int yRowStride = yPlane.getRowStride();
    int yPixelStride = yPlane.getPixelStride();
    for(int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        nv21[index++] = yBuffer.get(y * yRowStride + x * yPixelStride);
      }
    }

    // Copy VU data; NV21 format is expected to have YYYYVU packaging.
    // The U/V planes are guaranteed to have the same row stride and pixel stride.
    int uvRowStride = uPlane.getRowStride();
    int uvPixelStride = uPlane.getPixelStride();
    int uvWidth = width / 2;
    int uvHeight = height / 2;

    for(int y = 0; y < uvHeight; ++y) {
      for (int x = 0; x < uvWidth; ++x) {
        int bufferIndex = (y * uvRowStride) + (x * uvPixelStride);
        // V channel.
        nv21[index++] = vBuffer.get(bufferIndex);
        // U channel.
        nv21[index++] = uBuffer.get(bufferIndex);
      }
    }
    return new YuvImage(
            nv21, ImageFormat.NV21, width, height, /* strides= */ null);
  }

  void recordVideo() {
    if (isRecording) {
      currentLocation.getLastLocation();
    } else {
      Log.i(TAG, "Start record video");

      // Reset seconds
      mRecordingSecond = 0;
      // Reset no frame
      mNoFrame = 0;
      // initialize list
      mListOdometry = new ArrayList<>();

      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
      mFolderName = "rawdepth" + sdf.format(new Date());
      Log.i(TAG, "folderName: " + mFolderName);
      File rawDepthDirectory = MyUtils.Companion.getOutputSceneDirectory(this, mFolderName);

      mImageDirectory = new File(rawDepthDirectory, "images");
      if (!mImageDirectory.exists()) {
        mImageDirectory.mkdir();
      }

//      Uri destination = Uri.fromFile(new File(rawDepthDirectory, "RGB.mp4"));
//      Log.i(TAG, "destination: " + destination.getPath());
//      RecordingConfig recordingConfig = new RecordingConfig(session)
//              .setMp4DatasetUri(destination)
//              .setAutoStopOnPause(true);

      try {
        initiateFiles();

//        session.startRecording(recordingConfig);
        isRecording = true;
      } catch (RecordingFailedException e) {
        e.printStackTrace();
        Log.e(TAG, "Failed to start recording", e);
      } catch (Exception e) {
        e.printStackTrace();
        Log.e(TAG, "Failed", e);
      }

      // Set button to stop
      btnRecord.setImageResource(R.drawable.ic_start);
    }

  }

  void startUploading(String pathRecord, Location location) {
    Intent intent = new Intent(this, ProgressUploadingActivity.class);
    intent.putExtra("pathFile", pathRecord);
    intent.putExtra("lat", location.getLatitude()+"");
    intent.putExtra("long", location.getLongitude()+"");
    startActivity(intent);
  }


  void buildOdometryObjectAndImage(Image depthImage, Frame frame) throws IOException, NotYetAvailableException {
    try {
      Camera camera = frame.getCamera();

      float[] modelMatrix = new float[16];
      Anchor cameraPoseAnchor = session.createAnchor(camera.getPose());
      cameraPoseAnchor.getPose().toMatrix(modelMatrix, 0);

      CameraIntrinsics cameraIntrinsics = camera.getImageIntrinsics();
      float[] principalPoint = cameraIntrinsics.getPrincipalPoint();
      int[] imageDimensions = cameraIntrinsics.getImageDimensions();
      float[] focusLength = cameraIntrinsics.getFocalLength();

//    Log.i(TAG, "principalPoint: " + principalPoint[0] + ", " +principalPoint[1] );
//    Log.i(TAG, "imageDimensions: " + imageDimensions[0] + ", " + imageDimensions[1]);
//    Log.i(TAG, "focusLength: " + focusLength[0] + ", " + focusLength[1]);
//    Log.i(TAG, "Timestamp: " + depthImage.getTimestamp());

      //build odometry
      Odometry odometry = new Odometry();
      odometry.setTimestamp(depthImage.getTimestamp());
      ArrayList<Float> transform = new ArrayList<>(modelMatrix.length);
      for(float file: modelMatrix) {
        transform.add(file);
      }
      odometry.setTransform(transform);

      // build intrinsics
      float[] modelIntrinsics = new float[9];
      modelIntrinsics[0] = focusLength[0];
      modelIntrinsics[1] = 0;
      modelIntrinsics[2] = principalPoint[0];
      modelIntrinsics[3] = 0;
      modelIntrinsics[4] = focusLength[1];
      modelIntrinsics[5] = principalPoint[1];
      modelIntrinsics[6] = 0;
      modelIntrinsics[7] = 0;
      modelIntrinsics[8] = 1;
      ArrayList<Float> intrinsics = new ArrayList<>(modelIntrinsics.length);
      for(float value: modelIntrinsics) {
        intrinsics.add(value);
      }
      odometry.setIntrinsics(intrinsics);

      try {
        ImageMetadata metadata = frame.getImageMetadata();
        // Get the exposure time metadata. Throws MetadataNotFoundException if it's not available.
        odometry.setExposureDuration(metadata.getLong(ImageMetadata.SENSOR_EXPOSURE_TIME));
      } catch (MetadataNotFoundException e) {
        e.printStackTrace();
      }

      mListOdometry.add(odometry);

      //build image
      mNoFrame++;

      Image cameraImage = frame.acquireCameraImage();
      writeCameraImageToFile(cameraImage);
      cameraImage.close();
    } catch (Exception e) {
      e.getMessage();
    }
  }

  void buildSessionInfo() {
    Log.i(TAG, "buildSessionInfo");
    Log.e("OKOK", mNoFrame.toString());
    Log.e("OKOK1", mListOdometry.size()+"");
    CameraConfigFilter filter = new CameraConfigFilter(session);
    // Return only camera configs that target 30 fps camera capture frame rate.
    EnumSet<CameraConfig.TargetFps> targetFps = filter.getTargetFps();
    // create an iterator on games
    Iterator<CameraConfig.TargetFps> iterate = targetFps.iterator();
    CameraConfig.TargetFps targetFps1 = null;
    while (iterate.hasNext()) {
      targetFps1 = iterate.next();
      break;
    }

    Integer frequency = null;
    if (CameraConfig.TargetFps.TARGET_FPS_30 == targetFps1) {
      frequency = 30;
    } else if (CameraConfig.TargetFps.TARGET_FPS_60 == targetFps1) {
      frequency = 60;
    }

    List<Stream> listStream = new ArrayList<>();

    // Setup Video Stream object
    List<Integer> videoResolution = new ArrayList<>();
    // By default, ARCore records the 640x480 (VGA) CPU image that's used for motion tracking as the primary video stream.
    // https://developers.google.com/ar/develop/recording-and-playback
    if (cameraConfig != null) {
      videoResolution.add(cameraConfig.getImageSize().getHeight());
      videoResolution.add(cameraConfig.getImageSize().getWidth());
    } else {
      videoResolution.add(480);
      videoResolution.add(640);
    }

    // mp4
    Stream streamMP4 = new Stream();
    streamMP4.setNumberOfFrames(mListOdometry.size());
    streamMP4.setId("color_back_1");
    streamMP4.setFileExtension("mp4");
    streamMP4.setType("color_camera");
    streamMP4.setResolution(videoResolution);
    streamMP4.setEncoding("H.264");
    streamMP4.setIntrinsics(mListOdometry.get(0).getIntrinsics());
    streamMP4.setFrequency(frequency);
    listStream.add(streamMP4);

    // depth.bin
    Stream depth = new Stream();
    depth.setNumberOfFrames(mListOdometry.size());
    depth.setId("depth_back_1");
    depth.setFileExtension("depth.bin");
    depth.setType("");
    List<Integer> imageResolution = new ArrayList<>();
    imageResolution.add(mRawImgHeight);
    imageResolution.add(mRawImgWidth);
    depth.setResolution(imageResolution);
    depth.setEncoding("float16_zlib");
    depth.setFrequency(frequency);
    listStream.add(depth);

    // confidence_map
    Stream confidenceMap = new Stream();
    confidenceMap.setNumberOfFrames(mListOdometry.size());
    confidenceMap.setId("confidence_map");
    confidenceMap.setFileExtension("confidence.bin");
    confidenceMap.setType("confidence_map");
    confidenceMap.setEncoding("uint8_zlib");
    confidenceMap.setFrequency(frequency);
    listStream.add(confidenceMap);

    // camera_info_color_back_1
    Stream cameraInfo = new Stream();
    cameraInfo.setNumberOfFrames(mListOdometry.size());
    cameraInfo.setId("camera_info_color_back_1");
    cameraInfo.setFileExtension("jsonl");
    cameraInfo.setType("odometry");
    cameraInfo.setEncoding("jsonl");
    cameraInfo.setFrequency(frequency);
    listStream.add(cameraInfo);

    String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    Device device = new Device();
    device.setId(androidId);
    device.setType(Build.MANUFACTURER + Build.MODEL);
    device.setName(Build.DEVICE);
    device.setOs("android");

    SessionInfo sessionInfo = new SessionInfo();
    sessionInfo.setStreams(listStream);
    sessionInfo.setDevice(device);
    sessionInfo.setNumberOfFiles(5);

    try {
      File sessionInfoFile = new File(MyUtils.Companion.getOutputSceneDirectory(this, mFolderName), "session_info.json");
      if (!sessionInfoFile.exists()) {
        sessionInfoFile.createNewFile();
      }
      FileOutputStream fosSessionInfoData = new FileOutputStream(sessionInfoFile);
      fosSessionInfoData.write(sessionInfo.toString().getBytes(StandardCharsets.UTF_8));
      fosSessionInfoData.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void initiateFiles() throws IOException {
    Log.i(TAG, "initiateFiles");

    // depth & confidence file
    mDepthFile = new File(MyUtils.Companion.getOutputSceneDirectory(this, mFolderName), "depth.bin");
    mConfidenceFile = new File(MyUtils.Companion.getOutputSceneDirectory(this, mFolderName), "confidence.bin");

    if (!mDepthFile.exists()) {
      mDepthFile.createNewFile();
      mFosDepthData = new FileOutputStream(mDepthFile, true);
    }

    if (!mConfidenceFile.exists()) {
      mConfidenceFile.createNewFile();
      mFosConfidenceData = new FileOutputStream(mConfidenceFile, true);
    }

  }

  private void writeOdometryToFile() {
    Log.d(TAG, "writeOdometryToFile");
    File odometryFile = new File(MyUtils.Companion.getOutputSceneDirectory(this, mFolderName), "odometry.jsonl");
    try {

      if (!odometryFile.exists()) {
        odometryFile.createNewFile();
      }
      FileOutputStream fosOdometryData = new FileOutputStream(odometryFile);
      for (Odometry odometry : mListOdometry) {
        fosOdometryData.write(odometry.toString().getBytes(StandardCharsets.UTF_8));
      }
      fosOdometryData.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onDestroy() {
    // Unregister since the activity is about to be closed.
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    super.onDestroy();

    if (customHandler != null) {
      customHandler.removeCallbacks(updateTimerThread);
    }
  }

  @Override
  public void onLocationResolved(@Nullable Location location) {
    startUpload(location);
  }
}
