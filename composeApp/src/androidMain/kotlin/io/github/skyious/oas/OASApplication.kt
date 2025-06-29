package io.github.skyious.oas

import android.app.Application
import io.github.skyious.oas.fdroid.di.fdroidModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class OASApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@OASApplication)
            androidLogger()
            modules(fdroidModule)
        }
    }
}
