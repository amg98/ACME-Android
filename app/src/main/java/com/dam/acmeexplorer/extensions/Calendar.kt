package com.dam.acmeexplorer.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Calendar.formatted(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(time)

fun Date.formatted(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this)
