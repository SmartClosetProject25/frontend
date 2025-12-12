package com.example.smartcloset_frontend.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.repository.AuthRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SignupViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    
    fun signup(
        email: String,
        password: String,
        imageUri: Uri,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val cr = context.contentResolver

                // URI からバイト列を取得
                val bytes = cr.openInputStream(imageUri)?.use { it.readBytes() }
                    ?: throw IOException("画像を読み込めませんでした")

                // ContentResolver から MIME type を取得
                val mediaType = cr.getType(imageUri)?.toMediaTypeOrNull()
                    ?: "image/*".toMediaTypeOrNull()

                val requestBody = bytes.toRequestBody(mediaType)
                val fileName = "selfie_${System.currentTimeMillis()}.jpg"
                val imagePart = MultipartBody.Part.createFormData("image", fileName, requestBody)

                val response = repository.signup(email, password, imagePart)

                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, "Signup failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                onResult(false, "An error occurred: ${e.message}")
            }
        }
    }
}