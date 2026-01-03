package com.example.smartcloset_frontend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.data.TodayPlanData
import com.example.smartcloset_frontend.data.repository.TodayPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SuggestionViewModel : ViewModel() {

    private val repository = TodayPlanRepository()

    private val _isSendingPlan = MutableStateFlow(false)
    val isSendingPlan: StateFlow<Boolean> = _isSendingPlan

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage

    private val _proposals = MutableStateFlow<List<Proposal>>(emptyList())
    val proposals: StateFlow<List<Proposal>> = _proposals

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    private val _generatedImage = MutableStateFlow<String?>(null)
    val generatedImage: StateFlow<String?> = _generatedImage

    private val _navigateToGenerate = MutableStateFlow(false)
    val navigateToGenerate: StateFlow<Boolean> = _navigateToGenerate

    private val _selectedProposal = MutableStateFlow<Proposal?>(null)
    val selectedProposal: StateFlow<Proposal?> = _selectedProposal

    private val _todayPlan = MutableStateFlow<TodayPlanData?>(null)
    val todayPlan: StateFlow<TodayPlanData?> = _todayPlan

    private val _coordinateId = MutableStateFlow<Int?>(null)
    val coordinateId: StateFlow<Int?> = _coordinateId

    fun sendTodayPlan(todayPlanData: TodayPlanData) {
        viewModelScope.launch {
            _isSendingPlan.value = true
            _proposals.value = emptyList() // Clear previous proposals
            _todayPlan.value = todayPlanData // 今日の予定を保存
            try {
                val response = repository.sendTodayPlan(todayPlanData)
                if (response.isSuccessful) {
                    val proposals = response.body()?.proposals
                    if (!proposals.isNullOrEmpty()) {
                        _proposals.value = proposals
                        _toastMessage.value = "おすすめのコーディネートを取得しました"
                        Log.d("SuggestionViewModel", "sendTodayPlan successful: ${response.body()}")
                    } else {
                        _toastMessage.value = "おすすめのコーディネートが見つかりませんでした"
                        Log.d("SuggestionViewModel", "sendTodayPlan successful but no proposals: ${response.body()}")
                    }
                } else {
                    _toastMessage.value = "エラーが発生しました"
                    Log.e("SuggestionViewModel", "sendTodayPlan failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _toastMessage.value = "通信エラーが発生しました"
                Log.e("SuggestionViewModel", "sendTodayPlan failed with exception", e)
            } finally {
                _isSendingPlan.value = false
            }
        }
    }

    fun generateImage(imagePaths: List<String>, modelImageBase64: String? = null, modelTemplate: String? = null, proposal: Proposal? = null, coordinateId: Int? = null) {
        // 選択されたProposalを保存
        proposal?.let { _selectedProposal.value = it }
        // Coordinate IDを保存
        coordinateId?.let { _coordinateId.value = it }
        
        viewModelScope.launch {
            _isGeneratingImage.value = true
            Log.d("SuggestionViewModel", "Sending image paths to generate image: $imagePaths")
            Log.d("SuggestionViewModel", "Model image base64 length: ${modelImageBase64?.length ?: 0}")
            Log.d("SuggestionViewModel", "Model template: $modelTemplate")
            Log.d("SuggestionViewModel", "Coordinate ID: $coordinateId")
            try {
                val response = repository.generateImage(imagePaths, modelImageBase64, modelTemplate, coordinateId)
                if (response.isSuccessful) {
                    val imageResponse = response.body()
                    if (imageResponse?.status == "success") {
                        // image_url_fullを優先的に使用、なければimage_urlを使用
                        val imageUrl = imageResponse.image_url_full 
                            ?: imageResponse.image_url 
                            ?: imageResponse.image  // 後方互換性のため
                        
                        if (imageUrl != null) {
                            _generatedImage.value = imageUrl
                            _toastMessage.value = "画像を生成しました"
                            _navigateToGenerate.value = true // 画面遷移をトリガー
                            Log.d("SuggestionViewModel", "Image URL received: $imageUrl")
                        } else {
                            val errorMessage = imageResponse.message ?: "画像URLが取得できませんでした"
                            _toastMessage.value = errorMessage
                            Log.e("SuggestionViewModel", "generateImage failed: $errorMessage")
                        }
                    } else {
                        val errorMessage = imageResponse?.message ?: "画像生成に失敗しました"
                        _toastMessage.value = errorMessage
                        Log.e("SuggestionViewModel", "generateImage failed: $errorMessage")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _toastMessage.value = "画像生成に失敗しました: $errorBody"
                    Log.e("SuggestionViewModel", "generateImage failed with error: $errorBody")
                }
            } catch (e: Exception) {
                _toastMessage.value = "通信エラーが発生しました"
                Log.e("SuggestionViewModel", "generateImage failed with exception", e)
            } finally {
                _isGeneratingImage.value = false
            }
        }
    }

    fun onToastShown() {
        _toastMessage.value = null
    }

    fun onGenerateScreenNavigated() {
        _navigateToGenerate.value = false
    }
}
