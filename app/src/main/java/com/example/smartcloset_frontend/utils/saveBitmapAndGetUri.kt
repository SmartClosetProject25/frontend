package com.example.smartcloset_frontend.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri {
    val file = File(
        context.cacheDir,
        "item_${System.currentTimeMillis()}.jpeg"
    )
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    return file.toUri()
}

/**
 * カメラ撮影用の一時ファイルUriを作成
 * TakePicture()で高解像度の写真を撮影するために使用
 */
fun createImageFileUri(context: Context): Uri? {
    return try {
        val imageFile = File(
            context.getExternalFilesDir(null),
            "photo_${System.currentTimeMillis()}.jpg"
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    } catch (e: Exception) {
        null
    }
}
