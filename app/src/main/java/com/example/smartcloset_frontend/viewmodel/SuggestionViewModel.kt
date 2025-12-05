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

    private val _proposals = MutableStateFlow<List<Proposal>>(emptyList())
    val proposals: StateFlow<List<Proposal>> = _proposals

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    fun sendTodayPlan(todayPlanData: TodayPlanData) {
        viewModelScope.launch {
            _isSendingPlan.value = true
            _proposals.value = emptyList() // Clear previous proposals
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

    fun onToastShown() {
        _toastMessage.value = null
    }
}
