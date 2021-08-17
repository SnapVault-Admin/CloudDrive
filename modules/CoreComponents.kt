package com.snapvault.infinity.modules

import android.app.Application
import com.snapvault.infinity.DownloadService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
//import kotlinx.coroutines.CoroutineScope
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
//import mozilla.components.browser.session.LegacySessionManager
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.engine.EngineMiddleware
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.downloads.DownloadMiddleware
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class CoreComponents {

    @Provides
    @Singleton
    fun provideEngine(application: Application) : Engine {
        return GeckoEngine(application)
    }

    @Provides
    @Singleton
    fun provideSession() : Session{
        return Session("about:blank", private = true )
    }

    @Provides
    @Singleton
    fun provideStore(application: Application , engine : Engine, session: Session) : BrowserStore {
        return BrowserStore(middleware =
        listOf(DownloadMiddleware(application.applicationContext, DownloadService::class.java))
                + EngineMiddleware.create(engine, sessionLookup = { session })
        )
    }

    @Provides
    @Singleton
    fun providesSessionManager(engine: Engine, store: BrowserStore):SessionManager{
        return SessionManager(engine, store)
    }

    @Provides
    @Singleton
    fun providesClient(application: Application): Client {
        return GeckoViewFetchClient(application)
    }


}

