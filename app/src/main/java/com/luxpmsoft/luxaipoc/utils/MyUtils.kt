package com.luxpmsoft.luxaipoc.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.HttpException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.math.Vector3
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.BuildConfig
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.api.ConstantAPI
import com.luxpmsoft.luxaipoc.model.Country
import com.luxpmsoft.luxaipoc.model.Coutry_code_Response
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.defect_detect.ImageFrameModel
import com.luxpmsoft.luxaipoc.widget.TextViewFonts
import java.io.*
import java.nio.ByteBuffer
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.sqrt


class MyUtils {
    companion object {
        fun setStatusBarTransparentFlagBlack(context: Activity) {
            val decorView: View = context.window.decorView
            decorView.setOnApplyWindowInsetsListener { v, insets ->
                val defaultInsets: WindowInsets = v.onApplyWindowInsets(insets)
                defaultInsets.replaceSystemWindowInsets(
                    defaultInsets.systemWindowInsetLeft,
                    0,
                    defaultInsets.systemWindowInsetRight,
                    defaultInsets.systemWindowInsetBottom
                )
            }
            ViewCompat.requestApplyInsets(decorView)
            context.window.statusBarColor =
                ContextCompat.getColor(context, android.R.color.transparent)
        }

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context, name: String): File {
            val appContext = context.applicationContext

//            Environment.getExternalStoragePublicDirectory()
            val mediaDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                appContext.resources.getString(R.string.app_name).replace(" ", "") + "_" + name
            ).apply { mkdirs() }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }

        fun getOutputSceneDirectory(context: Context, name: String): File {
            val appContext = context.applicationContext
            val mediaDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                appContext.resources.getString(R.string.app_name).replace(" ", "") + "_" + name
            ).apply { mkdirs() }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }

        fun getOutputFile(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir =
                context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.apply { mkdir() }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }

        @Throws(IOException::class)
        fun zipFile(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {
            if (!fileName.contains("images")) {
                if (fileToZip.isHidden) {
                    return
                }
                if (fileToZip.isDirectory) {
                    if (fileName.endsWith("/")) {
                        zipOut.putNextEntry(ZipEntry(fileName))
                        zipOut.closeEntry()
                    } else {
                        zipOut.putNextEntry(ZipEntry("$fileName/"))
                        zipOut.closeEntry()
                    }
                    val children = fileToZip.listFiles()
                    for (childFile in children) {
                        if (!childFile.name.contains("images")) {
                            zipFile(childFile, fileName + "/" + childFile.name, zipOut)
                        }
                    }
                    return
                }
                val fis = FileInputStream(fileToZip)
                val zipEntry = ZipEntry(fileName)
                zipOut.putNextEntry(zipEntry)
                val bytes = ByteArray(1024)
                var length: Int
                while (fis.read(bytes).also { length = it } >= 0) {
                    zipOut.write(bytes, 0, length)
                }
                fis.close()
            }
        }

        @Throws(IOException::class)
        fun readFileToBytes(filePath: String?): ByteArray {
            val file = File(filePath)
            val bytes = ByteArray(file.length().toInt())
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)

                fis.read(bytes)
            } finally {
                fis?.close()
            }

            return bytes
        }

        fun Password_Validation(passwordString: String?): Boolean {
            if (null == passwordString || passwordString.length == 0) {
                return false
            }
            val passwordPattern: Pattern = Pattern
                .compile(
                    "^(?=.*[0-9])"
                            + "(?=.*[a-z])(?=.*[A-Z])"
                            + "(?=.*[!@#$%^&*+=])"
                            + "(?=\\S+$).{8,16}$"
                )
            val passwordMatcher: Matcher = passwordPattern.matcher(passwordString)
            return passwordMatcher.matches()
        }

        fun isPasswordValid(passwordString: String, pattern: String): Boolean {
            val regex = Regex(pattern)
            return regex.containsMatchIn(passwordString)
        }

        fun isEmailValid(emailString: String?): Boolean {
            if (null == emailString || emailString.length == 0) {
                return false
            }
            val emailPattern: Pattern = Pattern
                .compile(
                    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
                )
            val emailMatcher: Matcher = emailPattern.matcher(emailString)
            return emailMatcher.matches()
        }

        fun Name_Validation(namestring: String?): Boolean {
            if (null == namestring || namestring.length == 0) {
                return false
            }
            return true
        }

        fun showProgress(activity: Activity, view: View) {
            activity.runOnUiThread {
                view.visibility = View.VISIBLE
            }
        }

        fun hideProgress(activity: Activity, view: View) {
            activity.runOnUiThread {
                view.visibility = View.GONE
            }
        }

        fun visibleView(activity: Activity, view: View) {
            activity.runOnUiThread {
                view.visibility = View.VISIBLE
            }
        }

        fun hideView(activity: Activity, view: View) {
            activity.runOnUiThread {
                view.visibility = View.INVISIBLE
            }
        }

        fun openLink(activity: Activity, link: String) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            activity.startActivity(browserIntent)
        }

        const val REQUEST_CODE_PERMISSIONS = 10

        val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()


        fun allPermissionsGranted(baseContext: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }

        @SuppressLint("MissingPermission")
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivity =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivity != null) {
                val info = connectivity.allNetworkInfo
                if (info != null) {
                    for (i in info.indices) {
                        Log.i("Class", info[i].state.toString())
                        if (info[i].state == NetworkInfo.State.CONNECTED) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        fun dpToPx(dp: Float): Float {
            return dp * Resources.getSystem().displayMetrics.density
        }

        fun GetMimeType(context: Activity, uriImage: Uri?): String? {
            var strMimeType: String? = null
            val cursor: Cursor? = context.contentResolver.query(
                uriImage!!, arrayOf(MediaStore.MediaColumns.MIME_TYPE),
                null, null, null
            )
            if (cursor != null && cursor.moveToNext()) {
                strMimeType = cursor.getString(0)
            }
            return strMimeType
        }

        fun inSampleSize(mImagePath: String?): Int {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(mImagePath, options)
            val imageHeight = options.outHeight
            val imageWidth = options.outWidth
            Log.e("Width:", "$imageWidth, Height:$imageHeight")
            return if (imageWidth < 1000) {
                0
            } else 2
        }

        fun getCountriesList(activity: Activity):
                ArrayList<Country> {
            var countries: ArrayList<Country> = ArrayList()
            val countriesResponse: Coutry_code_Response = Gson().fromJson(
                readFileCountry(activity),
                Coutry_code_Response::class.java
            )
            for (item in countriesResponse.data) {
                countries.add(Country(item.name, item.callingCode))
            }
            return countries
        }

        fun readFileCountry(activity: Activity): String? {
            var inputStream: InputStream? = null
            var text: String? = ""
            try {
                inputStream = activity.assets.open("country.txt")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                text = String(buffer)
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return text
        }

        fun toastError(activity: Activity, error: ErrorModel?) {
            if (error != null) {
                Toast.makeText(
                    activity,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    activity,
                    "Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun getCurrentDateTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDate = sdf.format(Date())
            return currentDate
        }

        fun convertDatetimeToDate(inputDateStr: String): Date {
            val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            return inputFormat.parse(inputDateStr)
        }

        fun convertDateTimeISO(inputDateStr: String): String {
            val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(inputDateStr)
            df.timeZone = TimeZone.getDefault()
            return df.format(date)
        }

        fun convertDateTime(inputDateStr: String): String {
            val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val df = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(inputDateStr)
            df.timeZone = TimeZone.getDefault()
            return df.format(date)
        }

        fun convertDateTimeHH(inputDateStr: String): String {
            val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val df = SimpleDateFormat("HH:mm aa, MMM d, yyyy", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(inputDateStr)
            df.timeZone = TimeZone.getDefault()
            return df.format(date)
        }

        fun convertToLocalTime(inputDateStr: String): String? {
            val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val df = SimpleDateFormat("MMM dd, yyyy HH:mm a", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(inputDateStr)
            df.timeZone = TimeZone.getDefault()
            return df.format(date)
        }

        fun convertToLocalTime1(inputDateStr: String): String? {
            val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val df = SimpleDateFormat("MMM dd, yyyy HH:mm a", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(inputDateStr)
            df.timeZone = TimeZone.getDefault()
            return df.format(date)
        }

        fun convertToLocalTimeHHmm(inputDateStr: String): String? {
            val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val df = SimpleDateFormat("HH:mm a", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(inputDateStr)
            df.timeZone = TimeZone.getDefault()
            return df.format(date)
        }

        fun compareTwoDate(date: String, currentDate: String): Int {
            try {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                val date1: Date = simpleDateFormat.parse(date)
                val date2: Date = simpleDateFormat.parse(currentDate)
                return date1.compareTo(date2)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return 0
        }

        fun rotateBitmap(source: Bitmap, angle: Float): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }

        fun loadAvatar(
            activity: Activity,
            token: String?,
            profileImageKey: String?,
            avatarUser: ImageView,
            drawable: Drawable?
        ) {
            try {
                val glideUrl = GlideUrl(
                    BuildConfig.URL_OA3D + ConstantAPI.USER_IMAGE + "?profileImageKey=" +
                            profileImageKey,
                    LazyHeaders.Builder()
                        .addHeader("x-access-token", token!!)
                        .build()
                )

                val requestOptions = RequestOptions()
                    .error(drawable)

                Glide.with(activity)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(glideUrl)
                    .centerCrop()
                    .into(avatarUser)
            } catch (e: Exception) {
                e.message
            }
        }

        fun loadImage(
            activity: Activity,
            token: String?,
            url: String?,
            avatarUser: ImageView,
            drawable: Drawable
        ) {
            try {
                val glideUrl = GlideUrl(
                    url,
                    LazyHeaders.Builder()
                        .addHeader("x-access-token", token!!)
                        .build()
                )

                val requestOptions = RequestOptions()
                    .error(drawable)

                Glide.with(activity)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(glideUrl)
                    .centerCrop()
                    .into(avatarUser)
            } catch (e: Exception) {
                e.message
            }
        }

        fun loadReconstructionImage(
            activity: Activity,
            token: String?,
            thumbnailImageKey: String?,
            avatarUser: ImageView,
            drawable: Drawable
        ) {
            try {
                Glide.with(activity)
                    .load(BuildConfig.URL_IMAGE + thumbnailImageKey)
                    .centerCrop()
                    .error(drawable)
                    .into(avatarUser)
            } catch (e: Exception) {
                e.message
            } catch (e: FileNotFoundException) {
                e.message
            } catch (e: HttpException) {
                e.message
            }
        }

        private fun getHtmlModelViewer(urlString: String): String {
            return """
        <html>
        <head>
        <script type="module" src="https://unpkg.com/@google/model-viewer"></script>
        <script src="https://unpkg.com/focus-visible/dist/focus-visible.js" defer></script>
        <link rel="stylesheet" href="https://unpkg.com/tachyons@4.10.0/css/tachyons.min.css"/>
        </head>

        <model-viewer src="${urlString}" ar-modes=\"webxr\"
                    placeholder="\(placeHolderImage)"
                      alt="3D model" auto-rotate camera-controls
                        shadow-intensity="1" exposure="2"
                      ar magic-leap unstable-webxr>
        </model-viewer>
        <style>
        model-viewer {
            position: absolute;
            width: 100%;
            height: 100%;
            margin: 0 auto;
            background-color: linear-gradient(#ffffff, #ada996);
        }
        </style>
        </body>
        </html>
        """
        }

        private fun getHtmlModelViewer1(html: String? = null): String {
            var url = ""
//            for (reconstruction in reconstructions!!) {
//                url = url.plus("""loader.load( '${BuildConfig.URL_OA3D + "reconstruction/model/" + reconstruction.reconstructionID}', function ( gltf ) {
//                      const model = gltf.scene;
//                      model.position.set(${reconstruction.cadfilemodel?.position?.x},${reconstruction.cadfilemodel?.position?.y},${reconstruction.cadfilemodel?.position?.z});
//                      scene.add( model );
//
//                      animate();
//
//                    });""")
//            }
            return """
              <html>
                <head>
                  <title>OpenAir3D Viewer</title>
                  <style>
                    body {
                      margin: 0;
                    }
                  </style>
                  <script src="https://cdn.jsdelivr.net/npm/three/build/three.min.js"></script>
                  <script src="https://cdn.jsdelivr.net/npm/three/examples/js/loaders/GLTFLoader.min.js"></script>
                  <script src="https://cdn.jsdelivr.net/npm/three/examples/js/controls/OrbitControls.min.js"></script>
                  <script src="https://cdn.jsdelivr.net/npm/three/examples/js/environments/RoomEnvironment.min.js"></script>
                </head>
                <body>
                  <script>
        
                  let camera, controls, scene, renderer;
        
        
                    scene = new THREE.Scene();
                    camera = new THREE.PerspectiveCamera( 40, window.innerWidth / window.innerHeight, 1, 100 );
                    camera.position.set( -2,1,-4 );
                    const light = new THREE.AmbientLight( 0x404040 ); // soft white light
                    scene.add( light );
        
                    renderer = new THREE.WebGLRenderer( { antialias: true, alpha: true } );
                    renderer.setPixelRatio( window.devicePixelRatio );
                    renderer.setSize( window.innerWidth, window.innerHeight );
                    renderer.toneMapping = THREE.ACESFilmicToneMapping;
                    renderer.toneMappingExposure = 1;
                    renderer.outputEncoding = THREE.sRGBEncoding;
                    document.body.appendChild( renderer.domElement );
        
                    const pmremGenerator = new THREE.PMREMGenerator( renderer );
                    scene.background = new THREE.Color( 0xeeeeee );
                    scene.environment = pmremGenerator.fromScene( new THREE.RoomEnvironment(), 0.04 ).texture;
        
                    const loader = new THREE.GLTFLoader();
        
                    ${url}
                    controls = new THREE.OrbitControls( camera, renderer.domElement );
                    controls.target.set( 0, 0.5, 0 );
                    controls.minDistance = 1;
                    controls.maxDistance = 10;
                    controls.update();
                    animate();
                    window.addEventListener( 'resize', onWindowResize );
        
                  function onWindowResize() {
        
                    camera.aspect = window.innerWidth / window.innerHeight;
                    camera.updateProjectionMatrix();
        
                    renderer.setSize( window.innerWidth, window.innerHeight );
        
                  }
        
                  function animate() {
                      requestAnimationFrame( animate );
        
                      controls.update();
                      renderer.render( scene, camera );
                    };
        
                  </script>
                </body>
              </html>
            """
        }

        fun loadModel(
            activity: Activity,
            webView: WebView,
            reconstructionID: String?,
            html: String? = null
        ) {
            try {
                activity.runOnUiThread {
                    webView.webChromeClient = WebChromeClient()
                    webView.settings.javaScriptEnabled = true
                    webView.setWebViewClient(object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            if (webView.progress == 100) {
                                webView.visibility = View.VISIBLE
                            }
                        }
                    })
                    reconstructionID?.let {
                        val url = BuildConfig.URL_OA3D + "reconstruction/model/" + reconstructionID
                        val mv1 = "<html>\n" +
                                "    <head>\n" +
                                "    <script type=\"module\" src=\"https://unpkg.com/@google/model-viewer/dist/model-viewer.js\"></script>\n" +
                                "    <script nomodule src=\"https://unpkg.com/@google/model-viewer/dist/model-viewer-legacy.js\"></script>\n" +
                                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/tachyons@4.10.0/css/tachyons.min.css\"/>\n" +
                                "    </head>\n" +
                                "    <body class=\"w-100 sans-serif bg-white \">"

                        val mv2 = "<model-viewer ar-modes=\"webxr\" src=" + url +
                                "           alt=\"3D model\" auto-rotate camera-controls bg-white\n" +
                                "           ar magic-leap unstable-webxr>\n" +
                                "    </model-viewer>"
                        val mv3 = "<style>\n" +
                                "    body {\n" +
                                "      overflow:hidden;\n" +
                                "      font-family: -apple-system, BlinkMacSystemFont,avenir next,avenir,helvetica neue,helvetica,ubuntu,roboto,noto,segoe ui,arial,sans-serif;\n" +
                                "    }\n" +
                                "    model-viewer{\n" +
                                "      position: absolute;\n" +
                                "      width:100vw;\n" +
                                "      height:100vh;\n" +
                                "      margin: 0 auto;\n" +
                                "      border-radius: 12px;\n" +
                                "    }\n" +
                                "    </style>\n" +
                                "    </body>\n" +
                                "    </html>"
                        if (html != null) {
                            webView.loadDataWithBaseURL(url, html, "text/html", "UTF-8", null)
                        } else {
                            webView.loadDataWithBaseURL(
                                url,
                                getHtmlModelViewer(url),
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getDistance(p1: Vector3, p2: Vector3): String {
            val dx = p1.x - p2.x
            val dy = p1.y - p2.y
            val dz = p1.z - p2.z
            return DecimalFormat("##.##").format(sqrt((dx * dx + dz * dz + dy * dy).toDouble()))
        }

        fun getTextGood(context: Activity): String? {
            var text = context.getString(R.string.good_morning)
            val c = Calendar.getInstance()
            val timeOfDay = c[Calendar.HOUR_OF_DAY]

            if (timeOfDay >= 0 && timeOfDay < 12) {
                text = context.getString(R.string.good_morning)
            } else if (timeOfDay >= 12 && timeOfDay < 16) {
                text = context.getString(R.string.good_afternoon)
            } else if (timeOfDay >= 16 && timeOfDay < 21) {
                text = context.getString(R.string.good_evening)
            } else if (timeOfDay >= 21 && timeOfDay < 24) {
                text = context.getString(R.string.good_night)
            }

            return text
        }

        fun unlockScreen(activity: Activity) {
            val params = activity.window.attributes
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//        params.screenBrightness = 0F
            activity.window.attributes = params
        }

        fun transitionAnimation(activity: Activity) {
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        fun startSettingExternal(activity: Activity) {
            try {
                val intent =
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(
                        String.format(
                            "package:%s",
                            activity.packageName
                        )
                    )
                activity.startActivityForResult(intent, 2296)
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                activity.startActivityForResult(intent, 2296)
            }
        }

        fun hideKeyboard(activity: Activity?) {
            try {
                val imm =
                    activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                val focusedView = activity.currentFocus
                if (focusedView != null) {
                    imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var trustAllCerts: Array<TrustManager> = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                chain: Array<X509Certificate?>?,
                authType: String?
            ) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(
                chain: Array<X509Certificate?>?,
                authType: String?
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                val acceptedIssuers: Array<X509Certificate?>?
                return arrayOf()
            }
        })

        var myHostnameVerifier: HostnameVerifier = object : HostnameVerifier {
            override fun verify(hostname: String?, session: SSLSession?): Boolean {
                return true
            }
        }

        fun unzipFile(resource: FileInputStream?, folder: String?, zipName: String?): String {
            val zipIs = ZipInputStream(resource)
            var ze: ZipEntry? = null
            while (zipIs.nextEntry.also { ze = it } != null) {
//                val fout: FileOutputStream =
                if (ze!!.name.toString().contains("mean_beta_tpose_measurements.json")) {
                    val fileFolder = folder + "/" + zipName + "_" + ze!!.name.replace("/", "_")
                    val fout = FileOutputStream(fileFolder)

                    val buffer = ByteArray(1024)
                    var length = 0

                    while (zipIs.read(buffer).also { length = it } > 0) {
                        fout.write(buffer, 0, length)
                    }
                    zipIs.closeEntry()
                    fout.close()

                    try {

                        val inputStream: InputStream = File(fileFolder).inputStream()
                        if (inputStream != null) {
                            val inputStreamReader = InputStreamReader(inputStream)
                            val bufferedReader = BufferedReader(inputStreamReader)
                            var receiveString: String? = ""
                            val stringBuilder = StringBuilder()
                            while (bufferedReader.readLine().also { receiveString = it } != null) {
                                stringBuilder.append(receiveString)
                            }
                            inputStream.close()
                            return stringBuilder.toString()
                            Log.e("FOLDER", stringBuilder.toString())
                        }
                    } catch (e: FileNotFoundException) {
                        Log.e("login activity", "File not found: $e")
                    } catch (e: IOException) {
                        Log.e("login activity", "Can not read file: $e")
                    }
                }
            }
            zipIs.close()
            return ""
        }

        fun createSession(activity: Activity): Session? {
            var session: Session? = null
            try {
                // Creates the ARCore session.
                session = Session( /* context= */activity)
            } catch (e: UnavailableArcoreNotInstalledException) {
            } catch (e: UnavailableUserDeclinedInstallationException) {
            } catch (e: UnavailableApkTooOldException) {

            } catch (e: UnavailableSdkTooOldException) {
            } catch (e: UnavailableDeviceNotCompatibleException) {

            } catch (e: Exception) {
            }

            return session
        }

        fun bitMapToString(bitmap: Bitmap): String {
            val byte = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byte)
            val b = byte.toByteArray()
            return Base64.encodeToString(b, Base64.DEFAULT)
        }

        fun convertViewToBitmap(activity: Activity, name: String): Drawable? {
            val inflatedFrame: View = activity.layoutInflater.inflate(R.layout.item_user, null)
            val frameLayout = inflatedFrame.findViewById(R.id.user) as FrameLayout
            val tvName = inflatedFrame.findViewById(R.id.tvName) as TextViewFonts
            tvName.text = name
            frameLayout.isDrawingCacheEnabled = true
            frameLayout.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            frameLayout.layout(0, 0, frameLayout.measuredWidth, frameLayout.measuredHeight)
            frameLayout.buildDrawingCache(true)
            return BitmapDrawable(activity.getResources(), frameLayout.drawingCache)
        }

        fun getFreeSize(fileSize: Int): Boolean {
            var usedSize = 0L
            try {
                val stat = StatFs(Environment.getExternalStorageDirectory().path)
                val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
                usedSize = bytesAvailable / (1024 * 1024)
                if (fileSize + 20 < usedSize) {
                    return true
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return false
        }

        fun deleteFileGltf() {
            val folder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString()
            )
            for (file in folder.listFiles()) {
                val filename = file.name.toLowerCase()
                if (filename.endsWith(".gltf")) {
                    file.delete()
                }
            }
        }

        fun saveImageToFile(
            bmp: Bitmap?,
            file: File,
            isPNG: Boolean = false,
            quality: Int = 100
        ): File? {
            if (bmp == null) {
                return null
            }
            try {
                val fos = FileOutputStream(file)
                if (isPNG) {
                    bmp.compress(Bitmap.CompressFormat.PNG, quality, fos)
                } else {
                    bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                }
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return file
        }

        private fun reverseBuf(buf: ByteBuffer, width: Int, height: Int) {
            val ts = System.currentTimeMillis()
            var i = 0
            val tmp = ByteArray(width * 4)
            while (i++ < height / 2) {
                buf[tmp]
                System.arraycopy(
                    buf.array(),
                    buf.limit() - buf.position(),
                    buf.array(),
                    buf.position() - width * 4,
                    width * 4
                )
                System.arraycopy(tmp, 0, buf.array(), buf.limit() - buf.position(), width * 4)
            }
            buf.rewind()
        }

        /**
         * Get bitmap from ByteBuffer
         */
        fun fromBufferToBitmap(buffer: ByteBuffer, width: Int, height: Int): Bitmap? {
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            reverseBuf(buffer, width, height)
            result.copyPixelsFromBuffer(buffer)
            val transformMatrix = Matrix()
            val outputBitmap = Bitmap.createBitmap(
                result,
                0,
                0,
                result.width,
                result.height,
                transformMatrix,
                false
            )
            outputBitmap.density = DisplayMetrics.DENSITY_DEFAULT
            return outputBitmap
        }

        fun unpackZip(path: String, pathZip: String): ArrayList<ImageFrameModel> {
            var list: ArrayList<ImageFrameModel> = ArrayList()

            val `is`: InputStream
            val zis: ZipInputStream
            try {
                `is` = FileInputStream(pathZip)
                zis = ZipInputStream(BufferedInputStream(`is`))
                var ze: ZipEntry
                try {
                    while (zis.nextEntry.also { ze = it } != null) {
                        val baos = ByteArrayOutputStream()
                        val buffer = ByteArray(4096)
                        var count: Int
                        val filename = ze.name.replace("archive/", "")
                        val file = File(path, filename)
                        if (!file.exists()) {
                            file.createNewFile()
                        }
                        val fout = FileOutputStream(file)

                        // reading and writing
                        while (zis.read(buffer).also { count = it } != -1) {
                            baos.write(buffer, 0, count)
                            val bytes = baos.toByteArray()
                            fout.write(bytes)
                            baos.reset()
                        }
                        list.add(ImageFrameModel(file.path, false))
                        fout.close()
                        zis.closeEntry()
                    }
                    zis.close()
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return list
            }
            return list
        }

        fun loadDefectImage(
            activity: Activity,
            thumbnailImageKey: String?,
            avatarUser: ImageView,
            drawable: Drawable
        ) {
            try {
                Glide.with(activity)
                    .load(BuildConfig.URL_DEFECT + thumbnailImageKey)
                    .centerCrop()
                    .error(drawable)
                    .into(avatarUser)
            } catch (e: Exception) {
                e.message
            } catch (e: FileNotFoundException) {
                e.message
            } catch (e: HttpException) {
                e.message
            }
        }

        fun convertDateTime1(inputDateStr: String): String {
            val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val df = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(inputDateStr)
            df.timeZone = TimeZone.getDefault()
            return df.format(date)
        }

        fun getOutputDefectDirectory(context: Context, name: String): File {
            val appContext = context.applicationContext
            val mediaDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DCIM),
                name
            ).apply { mkdirs() }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }

        fun setLayoutSize(view: View, action: Boolean) {
            val params: ViewGroup.LayoutParams = view.layoutParams
            if (action) {
                params.height = 90
                params.width = 90
            } else {
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
                params.width = WindowManager.LayoutParams.WRAP_CONTENT
            }
            view.layoutParams = params

        }

        fun createEmailMask(email: String): String {
            val pattern = """^([^@]{1})([^@]+)([^@]{1}@)""".toRegex()
            return email.replace(pattern) {
                it.groupValues[1] + "*".repeat(it.groupValues[2].length) + it.groupValues[3]
            }
        }
    }
}

sealed class PasswordValidationPattern(val pattern: String) {
    object AtLeastOneDigit : PasswordValidationPattern("[0-9]")
    object SpecialCharacter : PasswordValidationPattern("[!@#$%^&*+=]")
    object MinEightCharacter : PasswordValidationPattern("(?=\\S+$).{8,16}$")
    object AtLeastOneUppercase : PasswordValidationPattern("[A-Z]")
}

sealed class SubscriptionType(val type: String) {
    object Organization : SubscriptionType("Organization")
    object Individual : SubscriptionType("Individual")
}

sealed class AccountType(val type: String) {
    object Organization : AccountType("organization")
    object JoinRequest : AccountType("request")
    object Individual : AccountType("individual")
}
