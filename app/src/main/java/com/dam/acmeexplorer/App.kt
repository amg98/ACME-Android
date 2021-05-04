package com.dam.acmeexplorer

import android.app.Application
import com.dam.acmeexplorer.providers.FirebaseTravelProvider
import com.dam.acmeexplorer.providers.MockTravelProvider
import com.dam.acmeexplorer.providers.TravelProvider
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

val appModule = module {
    single<TravelProvider> { FirebaseTravelProvider(get(), get()) }
    single(named("UserTravels")) { mutableMapOf<String, Boolean>() }
    single { FirebaseAuth.getInstance() }
    single { GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
            .requestEmail()
            .build() }
    single(named("GitHubProvider")) { OAuthProvider.newBuilder("github.com").build() }
    single { Firebase.storage }
    single { Firebase.firestore }

    viewModel { MainViewModel(get()) }
    viewModel { TravelListViewModel(get(), get(named("UserTravels"))) }
    viewModel { TravelDetailViewModel(get(named("UserTravels"))) }
    viewModel { FilterParamsViewModel() }
    viewModel { SelectedTravelsViewModel(get(), get(named("UserTravels"))) }
    viewModel { LoginViewModel(get(), get(named("GitHubProvider"))) }
    viewModel { RegisterViewModel(get()) }
    viewModel { NewTravelViewModel(get(), get()) }
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
