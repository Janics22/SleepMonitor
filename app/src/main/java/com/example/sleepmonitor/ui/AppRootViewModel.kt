package com.example.sleepmonitor.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmonitor.data.remote.BackendSyncService
import com.example.sleepmonitor.ui.utils.SessionManager
import kotlinx.coroutines.launch

data class AppRootState(
    val isReady: Boolean = false,
    val isLoggedIn: Boolean = false,
    val username: String = ""
)

class AppRootViewModel(
    private val sessionManager: SessionManager,
    private val backendSyncService: BackendSyncService
) : ViewModel() {

    private val _state = MutableLiveData(AppRootState())
    val state: LiveData<AppRootState> = _state

    init {
        refreshSession(showLoading = true)
    }

    fun refreshSession(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading) {
                _state.value = _state.value?.copy(isReady = false) ?: AppRootState()
            }

            runCatching {
                sessionManager.warmUp()
                sessionManager.readSessionSnapshot()
            }.onSuccess { snapshot ->
                _state.value = AppRootState(
                    isReady = true,
                    isLoggedIn = !snapshot.token.isNullOrBlank(),
                    username = snapshot.username.orEmpty()
                )

                snapshot.userId?.let { userId ->
                    launch {
                        backendSyncService.pullUserSnapshot(userId)
                    }
                }
            }.onFailure { error ->
                Log.e("AppRootViewModel", "No se pudo restaurar la sesion al iniciar", error)
                _state.value = AppRootState(isReady = true)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSessionAsync()
            _state.value = AppRootState(isReady = true)
        }
    }
}
