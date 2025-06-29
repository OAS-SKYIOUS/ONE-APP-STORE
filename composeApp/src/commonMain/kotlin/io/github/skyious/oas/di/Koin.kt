package io.github.skyious.oas.di

import io.github.skyious.oas.fdroid.data.repository.FdroidRepositoryImpl
import io.github.skyious.oas.fdroid.domain.repository.FdroidRepository
import io.github.skyious.oas.fdroid.domain.usecase.GetApps
import io.github.skyious.oas.fdroid.domain.usecase.GetRepositories
import io.github.skyious.oas.fdroid.domain.usecase.UpdateRepository
import io.github.skyious.oas.fdroid.presentation.viewmodel.FdroidViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule())
    }

fun appModule() = listOf(networkModule, repositoryModule, useCaseModule, viewModelModule)

val viewModelModule: Module = module {
    factory { FdroidViewModel(get(), get(), get()) }
}

val useCaseModule: Module = module {
    factory { GetApps(get()) }
    factory { GetRepositories(get()) }
    factory { UpdateRepository(get()) }
}

val repositoryModule: Module = module {
    single<FdroidRepository> { FdroidRepositoryImpl(get()) }
}

val networkModule: Module = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}
