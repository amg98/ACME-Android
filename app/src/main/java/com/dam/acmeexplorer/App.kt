package com.dam.acmeexplorer

import android.app.Application
import com.dam.acmeexplorer.api.OpenWeatherService
import com.dam.acmeexplorer.repositories.FilterRepository
import com.dam.acmeexplorer.repositories.TravelRepository
import com.dam.acmeexplorer.viewmodels.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val USER_TRAVELS = "UserTravels"
const val GITHUB_PROVIDER = "GitHubProvider"

val appModule = module {

    single(named(USER_TRAVELS)) { mutableMapOf<String, Boolean>() }
    single { FirebaseAuth.getInstance() }
    single { Firebase.storage }
    single { Firebase.firestore }
    single<OpenWeatherService> { Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherService::class.java) }

    factory { FilterRepository() }
    factory { TravelRepository(get(), get(), get()) }
    factory { GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
            .requestEmail()
            .build() }
    factory(named(GITHUB_PROVIDER)) { OAuthProvider.newBuilder("github.com").build() }

    viewModel { MainViewModel(get(), get(named(USER_TRAVELS))) }
    viewModel { TravelListViewModel(get(), get(named(USER_TRAVELS)), get()) }
    viewModel { TravelDetailViewModel(get(named(USER_TRAVELS))) }
    viewModel { FilterParamsViewModel(get()) }
    viewModel { SelectedTravelsViewModel(get(), get(named(USER_TRAVELS))) }
    viewModel { LoginViewModel(get(), get(named(GITHUB_PROVIDER)), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { NewTravelViewModel(get()) }
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
    }
}
