package com.example.smartcloset_frontend.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
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
     * 高解像度で読み込むように修正
     * EXIFのOrientationタグを考慮して画像を回転
     */
    fun uriToBase64(context: Context, uri: Uri, quality: Int = 80): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            // 高解像度で読み込むためのオプションを設定
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = 1 // サンプリングを無効化してフル解像度で読み込む
                inPreferredConfig = Bitmap.Config.ARGB_8888 // 高品質な色深度
            }
            var bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            // EXIFのOrientationタグを読み取って画像を回転
            bitmap = bitmap?.let { correctImageOrientation(context, uri, it) }
            
            bitmap?.let { bitmapToBase64(it, quality) }
        } catch (e: IOException) {
            null
        }
    }

    /**
     * EXIFのOrientationタグに基づいて画像を回転
     */
    private fun correctImageOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()
            
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(270f)
                    matrix.postScale(-1f, 1f)
                }
                else -> return bitmap // 回転不要
            }
            
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            // EXIF読み取りに失敗した場合は元のbitmapを返す
            bitmap
        }
    }
}

