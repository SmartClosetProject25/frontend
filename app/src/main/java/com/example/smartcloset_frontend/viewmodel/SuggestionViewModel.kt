package com.example.smartcloset_frontend.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.data.TodayPlanData
import com.example.smartcloset_frontend.data.repository.TodayPlanRepository
import com.example.smartcloset_frontend.work.ImageGenerationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SuggestionViewModel : ViewModel() {

    private val repository = TodayPlanRepository()

    private val _isSendingPlan = MutableStateFlow(false)
    val isSendingPlan: StateFlow<Boolean> = _isSendingPlan

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage

    private val _proposals = MutableStateFlow<List<Proposal>>(emptyList())
    val proposals: StateFlow<List<Proposal>> = _proposals

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
                        Log.d("SuggestionViewModel", "sendTodayPlan successful: ${response.body()}")
                    } else {
                        Log.d("SuggestionViewModel", "sendTodayPlan successful but no proposals: ${response.body()}")
                    }
                } else {
                    Log.e("SuggestionViewModel", "sendTodayPlan failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("SuggestionViewModel", "sendTodayPlan failed with exception", e)
            } finally {
                _isSendingPlan.value = false
            }
        }
    }

    fun generateImage(
        context: Context,
        imagePaths: List<String>,
        modelImageBase64: String? = null,
        modelTemplate: String? = null,
        proposal: Proposal? = null,
        coordinateId: Int? = null,
        useBackgroundGeneration: Boolean = true
    ) {
        // 選択されたProposalを保存
        proposal?.let { _selectedProposal.value = it }
        // Coordinate IDを保存
        coordinateId?.let { _coordinateId.value = it }
        
        if (useBackgroundGeneration) {
            // バックグラウンド生成を使用（WorkManager）
            val workRequestId = UUID.randomUUID().toString()
            
            Log.d("SuggestionViewModel", "WorkManagerで画像生成を開始: workRequestId=$workRequestId, imagePaths=$imagePaths")
            
            val inputData = Data.Builder().apply {
                putStringArray(ImageGenerationWorker.KEY_IMAGE_PATHS, imagePaths.toTypedArray())
                modelImageBase64?.let { 
                    putString(ImageGenerationWorker.KEY_MODEL_IMAGE_BASE64, it)
                    Log.d("SuggestionViewModel", "modelImageBase64 length: ${it.length}")
                }
                modelTemplate?.let { 
                    putString(ImageGenerationWorker.KEY_MODEL_TEMPLATE, it)
                    Log.d("SuggestionViewModel", "modelTemplate: $it")
                }
                coordinateId?.let { 
                    putInt(ImageGenerationWorker.KEY_COORDINATE_ID, it)
                    Log.d("SuggestionViewModel", "coordinateId: $it")
                }
                putString(ImageGenerationWorker.KEY_WORK_REQUEST_ID, workRequestId)
            }.build()
            
            val workRequest = OneTimeWorkRequestBuilder<ImageGenerationWorker>()
                .setInputData(inputData)
                .build()
            
            try {
                WorkManager.getInstance(context.applicationContext).enqueue(workRequest)
                Log.d("SuggestionViewModel", "WorkManagerにリクエストを追加しました")
                
                // 生成中フラグをSharedPreferencesにも保存
                val sharedPreferences = context.applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("is_generating_image", true).apply()
                
                _isGeneratingImage.value = true
            } catch (e: Exception) {
                Log.e("SuggestionViewModel", "WorkManagerへのリクエスト追加に失敗", e)
                _isGeneratingImage.value = false
            }
        } else {
            // 従来の同期生成（フォアグラウンドのみ）
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
                                _navigateToGenerate.value = true // 画面遷移をトリガー
                                Log.d("SuggestionViewModel", "Image URL received: $imageUrl")
                            } else {
                                val errorMessage = imageResponse.message ?: "画像URLが取得できませんでした"
                                Log.e("SuggestionViewModel", "generateImage failed: $errorMessage")
                            }
                        } else {
                            val errorMessage = imageResponse?.message ?: "画像生成に失敗しました"
                            Log.e("SuggestionViewModel", "generateImage failed: $errorMessage")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("SuggestionViewModel", "generateImage failed with error: $errorBody")
                    }
                } catch (e: Exception) {
                    Log.e("SuggestionViewModel", "generateImage failed with exception", e)
                } finally {
                    _isGeneratingImage.value = false
                }
            }
        }
    }
    
    // 生成結果を設定する関数
    fun setGeneratedImageFromNotification(imageUrl: String) {
        _generatedImage.value = imageUrl
        _isGeneratingImage.value = false
        _navigateToGenerate.value = true
    }

    fun onGenerateScreenNavigated() {
        _navigateToGenerate.value = false
    }
    
    // 履歴から生成結果画面に移動するためのデータを設定
    fun setHistoryData(proposal: Proposal, coordinateId: Int?, generatedImagePath: String?) {
        _selectedProposal.value = proposal
        _coordinateId.value = coordinateId
        // 生成画像のパスをURLに変換
        generatedImagePath?.let { path ->
            if (path.startsWith("http://") || path.startsWith("https://")) {
                _generatedImage.value = path
            } else {
                // 相対パスの場合は、後でGeneratedResultScreenで処理される
                _generatedImage.value = path
            }
        } ?: run {
            _generatedImage.value = null
        }
    }
}
