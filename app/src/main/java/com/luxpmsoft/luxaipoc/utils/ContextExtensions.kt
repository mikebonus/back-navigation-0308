package com.luxpmsoft.luxaipoc.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.luxpmsoft.luxaipoc.R
import java.io.File

fun Context.getFileName(uri: Uri): String? = when (uri.scheme) {
    ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
    else -> uri.path?.let(::File)?.name
}

private fun Context.getContentFileName(uri: Uri): String? = runCatching {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
    }
}.getOrNull()

fun Context.openMailApp() {
    val emailIntent = Intent(
        Intent.ACTION_SENDTO, Uri.parse(getString(R.string.common_mail_to))
    )
    startActivity(Intent(emailIntent))
}
