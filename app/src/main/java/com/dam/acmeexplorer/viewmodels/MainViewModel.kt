package com.dam.acmeexplorer.viewmodels

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.activities.SelectedTravelsActivity
import com.dam.acmeexplorer.activities.TravelListActivity
import com.dam.acmeexplorer.models.MenuEntry
import com.google.firebase.auth.FirebaseAuth

class MainViewModel(private val auth: FirebaseAuth) : ViewModel() {

    val menuEntries = listOf(
        MenuEntry(R.drawable.travel, "Lista de viajes"),
        MenuEntry(R.drawable.target, "Mis viajes")
    )

    fun logout() {
        auth.signOut()
    }

    fun getIntentForMenuItem(context: Context, position: Int): Intent {
        return if(position == 0) {
            Intent(context, TravelListActivity::class.java)
        } else {
            Intent(context, SelectedTravelsActivity::class.java)
        }
    }
}
