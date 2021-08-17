package com..

import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.downloads.AbstractFetchDownloadService
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import mozilla.components.browser.session.Session

@AndroidEntryPoint
class DownloadService : AbstractFetchDownloadService() {

    @Inject override lateinit var store: BrowserStore
    @Inject override lateinit var httpClient: Client

}
