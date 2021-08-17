package com.snapvault.infinity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
//import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Button
//import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fingerprintjs.android.fingerprint.Configuration
import com.fingerprintjs.android.fingerprint.Fingerprinter
import com.fingerprintjs.android.fingerprint.FingerprinterFactory
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.scottyab.rootbeer.RootBeer
import okhttp3.*
import okhttp3.FormBody
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Method
import java.net.Proxy
import java.net.ProxySelector
import java.net.URI
import java.security.MessageDigest
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    init {
        System.loadLibrary("api-keys")
    }
    external fun getServer(token: String) : String
    external fun getVersion() : String
    external fun getBaseSig() : String
    external fun getRandomBase() : String

    private var app_version = getVersion()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        proxy_check()
        app_tamper_check()
        val device_valid = (!adb_status() && !apk_debuggable() && display_flag() && !device_root() && !checkEmulator())


        if (midtrek()?.let { getServer(it) } != "null"){
            if (internetCheck(this) && device_valid && !isproxy(this)){
                val update_check = Thread(Runnable {
                    val update_check_body: RequestBody = FormBody.Builder()
                        .add("version", app_version)
                        .build()
                    var obj: JSONObject? = null
                    try {
                        obj = send_post_request(update_check_body, "check_update")
                        if (obj != null) {
                            if (obj.getBoolean("update_status")) {
                                val finalObj: JSONObject = obj
                                runOnUiThread {
                                    if (!isFinishing) {
                                        val update_dialog = AlertDialog.Builder(this@MainActivity)
                                            .setTitle("New Update !!!")
                                            .setMessage("Please update to latest version of Infinity.")
                                            .setCancelable(false)
                                            .setPositiveButton("Update", null)
                                            .show()
                                        val update_btn: Button = update_dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                        update_btn.setOnClickListener { v ->
                                            var intent: Intent? = null
                                            try {
                                                intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalObj.getString("update_url")))
                                            } catch (e: JSONException) {
                                                e.printStackTrace()
                                            }
                                            startActivity(intent)
                                            update_dialog.show()
                                        }
                                    }
                                }
                            } else {
                                val fingerprinter: Fingerprinter = FingerprinterFactory
                                    .getInstance(applicationContext, Configuration(2))

                                fingerprinter.getDeviceId { result ->
                                    if (internetCheck(this) && device_valid && !isproxy(this)) {
                                        // ANDROID ID - 1

                                        // ANDROID ID - 2

                                        // hardware ID - 1

                                        // hardware ID - 2

                                        val receive_token_thread = Thread(Runnable {

                                            // ANDROID ID - 3

                                            } catch (e: UnsupportedSchemeException) {
                                                e.printStackTrace()
                                            } catch (e: JSONException) {
                                                e.printStackTrace()
                                            } catch (e: IOException) {
                                                e.printStackTrace()
                                            }
                                        })
                                        receive_token_thread.start()
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                })
                update_check.start()
            }
        } else {
            alertbox_template("App Unstable", "Error Code : 009 Please reinstall the app from the admin via telegram @Infinitysvbot")
        }
    }



    @SuppressLint("PackageManagerGetSignatures")
    fun midtrek(): String {
        val signatureList: List<String>
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val sig = packageManager.getPackageInfo("com.snapvault.infinity", PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
            signatureList = if (sig.hasMultipleSigners()) {
                // Send all with apkContentsSigners
                sig.apkContentsSigners.map {
                    val digest = MessageDigest.getInstance(getBaseSig())
                    digest.update(it.toByteArray())
                    bytesToHex(digest.digest())
                }
            } else {
                // Send one with signingCertificateHistory
                sig.signingCertificateHistory.map {
                    val digest = MessageDigest.getInstance(getBaseSig())
                    digest.update(it.toByteArray())
                    bytesToHex(digest.digest())
                }
            }
            return signatureList[0]
        } else {
            val sig = this.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
            signatureList = sig.map {
                val digest = MessageDigest.getInstance(getBaseSig())
                digest.update(it.toByteArray())
                bytesToHex(digest.digest())
            }
            return signatureList[0]
        }
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
        val hexChars = CharArray(bytes.size * 2)
        var v: Int
        for (j in bytes.indices) {
            v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun wisp(): String {
        val wisp_ndk = getServer(midtrek());
        val time = Calendar.getInstance();
        val daytoday = time.get(Calendar.DAY_OF_MONTH);
        val random_server = (daytoday % 2) + 1 ;
        val wisp_array = wisp_ndk.split(".");
        val wisp_url = wisp_array[0] + random_server + "." + wisp_array[1] + "." +wisp_array[2];
        return wisp_ndk //wisp_url
    }



    


    fun safetynet_device(nonce_string: String, api_key: String?) {
        val charset = Charsets.UTF_8;
        val nonce = nonce_string.toByteArray(charset);

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            if (api_key != null) {
                SafetyNet.getClient(this).attest(nonce, api_key)
                    .addOnSuccessListener(this) {
                        
                            if (obj != null) {
                                if (java.lang.Boolean.parseBoolean(obj.getString("status"))) {
                                    try {
                                        var url = obj.getString("redirect_url")
                                        val intent = Intent(this@MainActivity, Render::class.java)
                                        intent.putExtra("url", url)
                                        startActivity(intent)
                                        finish()
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    alertbox_template(
                                        "Server Error", "Err Code : 008 Contact the ADMIN via telegram @Infinitysvbot. Reason : " + obj.getString("reason")
                                    )
                                }
                            } else {
                                alertbox_template("Server Error", "Err Code : 005 Contact the ADMIN via telegram @Infinitysvbot")
                            }
                        })
                        signedAttestation_thread.start()
                    }
                    .addOnFailureListener(this) { e ->
                        if (e is ApiException) {
                            val apiException = e as ApiException
                            alertbox_template(
                                "Server Error",
                                "Err Code : 007 Contact the ADMIN via telegram @Infinitysvbot"
                            )
                        } else {
                            alertbox_template(
                                "Server Error",
                                "Err Code : 007 Contact the ADMIN via telegram @Infinitysvbot"
                            )
                        }
                    }
            }
        } else {
            alertbox_template(
                "Server Error",
                "Err Code : 006 Contact the ADMIN via telegram @Infinitysvbot"
            )
        }
    }


    


    @Throws(IOException::class, JSONException::class)
    fun send_post_request(formBody: RequestBody?, url_post: String): JSONObject? {
        return try {
            // must add new servers
            val request: Request? = formBody?.let {
                Request.Builder()
                    .url(wisp() + url_post)
                    .post(it)
                    .build()
            }
            val call: Call = client.newCall(request!!)
            val response: Response = call.execute()
            if (response.code == 200){
                JSONObject(response.peekBody(Long.MAX_VALUE).string())
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    @SuppressLint("HardwareIds")
    fun getSerialNumber(): String? {
        var serialNumber: String?
        try {
            val c = Class.forName("android.os.SystemProperties")
            val get: Method = c.getMethod("get", String::class.java)
            serialNumber = get.invoke(c, "gsm.sn1") as String?
            if (serialNumber == "")
                serialNumber = get.invoke(c, "ril.serialnumber") as String?
            if (serialNumber == "")
                serialNumber = get.invoke(c, "ro.serialno") as String?
            if (serialNumber == "")
                serialNumber = get.invoke(c, "sys.serialnumber") as String?
            if (serialNumber == "")
                serialNumber = Build.SERIAL
            if (serialNumber == Build.UNKNOWN) serialNumber = null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            serialNumber = null
        }
        return serialNumber
    }

    fun alertbox_template(title: String?, body: String?) {
        val thread = Thread(Runnable {
            runOnUiThread {
                if (!isFinishing) {
                    val dialog: AlertDialog = AlertDialog.Builder(this@MainActivity)
                        .setTitle(title)
                        .setMessage(body)
                        .setCancelable(false)
                        .setPositiveButton("", null)
                        .show()
                    dialog.show()
                }
            }
        })
        thread.start()
    }


    fun adb_status(): Boolean {

    }

    fun device_root(): Boolean {

    }

    fun apk_debuggable(): Boolean {

    }

    fun display_flag(): Boolean {

    }

    fun checkEmulator(): Boolean {
        
    }



    fun internetCheck(context : Context): Boolean {
       
    }


    fun app_tamper_check() {
   
    }



    fun proxy_check() {

    }


    fun isproxy(context: Context): Boolean {

    }

}