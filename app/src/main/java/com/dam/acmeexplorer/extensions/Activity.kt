package com.dam.acmeexplorer.extensions

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.activities.MainActivity
import java.util.*

fun Activity.showDatePickerDialog(cal: Calendar, editText: EditText, minDate: Calendar?, onDate: (Int, Int, Int) -> String) {
    val dialog = DatePickerDialog(this, { _, year, month, day ->
        editText.setText(onDate(day, month, year))
    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    dialog.datePicker.minDate = minDate?.timeInMillis ?: 0
    dialog.show()
}

fun Activity.showMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.showYesNoDialog(message: String, onYes: () -> Unit) {
    AlertDialog.Builder(this).setMessage(message)
            .setPositiveButton(R.string.yes) { _, _ -> onYes() }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
}

fun Activity.getLocationPermission(requestID: Int, onWasGranted: () -> Unit) {

    val permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray()

    if(ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
            showYesNoDialog(getString(R.string.locationPermissionText)) {
                ActivityCompat.requestPermissions(this, permissions, requestID)
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, requestID)
        }
    } else {
        onWasGranted()
    }
}

fun Activity.showFileChooser(launcher: ActivityResultLauncher<Intent>, message: String, mimeType: String) {

    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = mimeType
    intent.addCategory(Intent.CATEGORY_OPENABLE)

    try {
        launcher.launch(Intent.createChooser(intent, message))
    } catch (ex: ActivityNotFoundException) {
        Toast.makeText(this, getString(R.string.noExplorerFound), Toast.LENGTH_SHORT).show()
    }
}
