package com.example.smartcloset_frontend.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri {
    val file = File(
        context.cacheDir,
        "item_${System.currentTimeMillis()}.png"
    )
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
    }
    return file.toUri()
}
