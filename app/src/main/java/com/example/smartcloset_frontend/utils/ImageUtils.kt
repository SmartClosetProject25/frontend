package com.example.smartcloset_frontend.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImageUtils {
    /**
     * Bitmapをbase64エンコードされた文字列に変換
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 80): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Uriから画像を読み込んでbase64エンコードされた文字列に変換
     */
    fun uriToBase64(context: Context, uri: Uri, quality: Int = 80): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap?.let { bitmapToBase64(it, quality) }
        } catch (e: IOException) {
            null
        }
    }
}

