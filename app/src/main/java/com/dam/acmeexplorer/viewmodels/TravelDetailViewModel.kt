package com.dam.acmeexplorer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private const val SELECT_TEXT = "Seleccionar"
private const val BUY_TEXT = "Comprar"
private const val DELETE_TEXT = "Eliminar"

class TravelDetailViewModel(private val userTravels: MutableMap<String, Boolean>) : ViewModel() {

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _actionButtonText = MutableLiveData(SELECT_TEXT)
    val actionButtonText: LiveData<String> get() = _actionButtonText

    fun updateSelectButton(travelID: String, buyEnabled: Boolean) {
        if(buyEnabled) {
            _actionButtonText.value = BUY_TEXT
            return
        }

        if(userTravels.contains(travelID)) {
            _actionButtonText.value = DELETE_TEXT
        } else {
            _actionButtonText.value = SELECT_TEXT
        }

    }

    fun onActionButton(travelID: String, buyEnabled: Boolean) {
        if(buyEnabled) {
            _toastMessage.value = "Ha llegado el momento de la compra"
            return
        }

        if(userTravels.contains(travelID)) {
            userTravels.remove(travelID)
            _actionButtonText.value = SELECT_TEXT
        } else {
            userTravels[travelID] = true
            _actionButtonText.value = DELETE_TEXT
        }
    }
}
