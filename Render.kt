package com..

import android.content.Context
//import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
//import android.view.autofill.AutofillManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_render.*
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.session.FullScreenFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.SessionUseCases
import java.net.Proxy
import java.net.ProxySelector
import java.net.URI
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

@AndroidEntryPoint
class Render : AppCompatActivity() {
    @Inject lateinit var engine: Engine
    @Inject lateinit var store: BrowserStore
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var session: Session

    private lateinit var sessionUseCases: SessionUseCases
    private lateinit var sessionFeature: SessionFeature
    private lateinit var feature: FullScreenFeature
    private lateinit var downloadsFeature: DownloadsFeature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_render)
        val intent = intent
        val url = intent.getStringExtra("url")
        supportActionBar?.hide()

        window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT



        sessionUseCases = SessionUseCases(store, sessionManager)
        val downloadUseCases = DownloadsUseCases(store)

        sessionFeature = SessionFeature(
                store,
                sessionUseCases.goBack,
                findViewById<View>(R.id.engineView) as EngineView
        )

        feature = FullScreenFeature(
                store,
                sessionUseCases,
                fullScreenChanged = ::fullScreenChanged
        )





        val session_size = sessionManager.sessions.size
        if (session_size < 1){
            sessionManager.add(
                session
            )
        }

        session.register(object : Session.Observer {
            override fun onProgress(session: Session, progress: Int){
                if (progress == 100){
                    if (progressBar_render.visibility == View.VISIBLE){
                        progressBar_render.visibility = View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
                } else {
                    if (progressBar_render.visibility == View.GONE){
                        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        progressBar_render.visibility = View.VISIBLE
                    }
                }
            }
        })




        downloadsFeature = DownloadsFeature(
                applicationContext,
                store,
                downloadUseCases,
                onNeedToRequestPermissions = { permissions ->
                    requestPermissions(permissions, 1)
                },
                downloadManager = FetchDownloadManager(
                        applicationContext,
                        store,
                        DownloadService::class
                ),
                fragmentManager = supportFragmentManager
        )

        lifecycle.addObserver(sessionFeature)
        lifecycle.addObserver(feature)
        lifecycle.addObserver(downloadsFeature)

    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array< String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        downloadsFeature.onPermissionsResult(permissions, grantResults)
    }

    private fun fullScreenChanged(enabled: Boolean) {
        if (enabled){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }


    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        if (name == EngineView::class.java.name) {
            return engine.createView(context, attrs).asView()
        }
        return super.onCreateView(name, context, attrs)
    }


    

}