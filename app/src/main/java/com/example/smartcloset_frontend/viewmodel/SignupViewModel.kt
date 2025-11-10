package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.repository.SignupRepository
import com.example.smartcloset_frontend.data.SignUpData
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {
    private val repository = SignupRepository()
    fun signup(email: String, password: String, imageUrl: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val signupData = SignUpData(email = email, password = password, userImageUrl = imageUrl)
                val response = repository.signup(signupData)

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