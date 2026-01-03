package com.example.smartcloset_frontend.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.data.repository.ServerConfigRepository
import com.example.smartcloset_frontend.network.ServerUrlHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ServerConfigUiState(
    val inputUrl: String = "",
    val effectiveUrl: String = BuildConfig.SERVER_URL,
    val message: String? = null
)

class ServerConfigViewModel(
    private val repo: ServerConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServerConfigUiState())
    val uiState: StateFlow<ServerConfigUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            val saved = repo.overrideUrlFlow.first()
            val effective = saved ?: BuildConfig.SERVER_URL
            _uiState.value = _uiState.value.copy(
                inputUrl = saved.orEmpty(),
                effectiveUrl = effective,
                message = null
            )
            // アプリ起動時に Holder も合わせる
            ServerUrlHolder.overrideBaseUrl = saved
        }
    }

    fun onInputChange(v: String) {
        _uiState.value = _uiState.value.copy(inputUrl = v, message = null)
    }

    fun apply() {
        viewModelScope.launch {
            val normalized = normalizeUrl(_uiState.value.inputUrl)
            // 保存 + メモリへ反映
            repo.setOverrideUrl(normalized)
            ServerUrlHolder.overrideBaseUrl = normalized

            val effective = normalized ?: BuildConfig.SERVER_URL
            _uiState.value = _uiState.value.copy(
                effectiveUrl = effective,
                message = if (normalized == null) "デフォルトに戻しました" else "上書きURLを適用しました"
            )
        }
    }

    fun resetToDefault() {
        _uiState.value = _uiState.value.copy(inputUrl = "")
        apply()
    }

    private fun normalizeUrl(raw: String): String? {
        val t = raw.trim()
        if (t.isBlank()) return null

        // scheme なしで入力されたときに補完（好みで http 固定）
        val withScheme = if (t.startsWith("http://") || t.startsWith("https://")) t else "http://$t"

        // RetrofitのbaseUrlと揃えるため末尾 / をつける
        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }
}
