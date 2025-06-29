package io.github.skyious.oas.fdroid.di

import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.skyious.oas.fdroid.data.repository.FdroidRepositoryImpl
import io.github.skyious.oas.fdroid.domain.repository.FdroidRepository
import io.github.skyious.oas.fdroid.domain.usecase.GetApps
import io.github.skyious.oas.fdroid.domain.usecase.GetRepositories
import io.github.skyious.oas.fdroid.domain.usecase.UpdateRepository
import io.github.skyious.oas.fdroid.presentation.viewmodel.FdroidViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.internal.platform.android.AndroidSocketAdapter.Companion.factory
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val fdroidModule = module {
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    single<FdroidRepository> { FdroidRepositoryImpl(get()) }

    factory { GetApps(get()) }
    factory { GetRepositories(get()) }
    factory { UpdateRepository(get()) }

    viewModel { FdroidViewModel(get(), get(), get()) }
}
