package com.example.smartcloset_frontend.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
/**
 * コンテンツURI (ギャラリー / カメラ) の画像を
 * アプリ内部ストレージ内の "ItemImgs" フォルダにコピーして保存する。
 *
 * @return 保存されたファイルの絶対パス。失敗したら null。
 */
fun saveImageToLocalItemFolder(
    context: Context,
    sourceUri: Uri
): String? {
    return try {
        // /data/data/<package>/files/ItemImgs
        val dir = File(context.filesDir, "ItemImgs").apply {
            if (!exists()) {
                mkdirs()
            }
        }

        // ファイル名はお好みで。ここではタイムスタンプ＋拡張子にしておく
        val fileName = "item_${System.currentTimeMillis()}.jpg"
        val destFile = File(dir, fileName)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        destFile.absolutePath  // これをサーバに送る
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}