package com.dam.acmeexplorer.exceptions

import android.content.Context

class AlertException(private val messageID: Int): Exception() {
    fun asString(context: Context): String = context.getString(messageID)
}
