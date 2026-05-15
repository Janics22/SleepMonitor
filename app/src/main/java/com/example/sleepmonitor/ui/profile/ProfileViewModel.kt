package com.example.sleepmonitor.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmonitor.data.local.entities.UserEntity
import com.example.sleepmonitor.data.repository.AuthRepository
import com.example.sleepmonitor.data.repository.Result
import com.example.sleepmonitor.ui.utils.SessionManager
import kotlinx.coroutines.launch

sealed class ProfileState {
    data object Loading : ProfileState()
    data class Ready(
        val user: UserEntity?,
        val isSaving: Boolean = false,
        val message: String? = null,
        val error: String? = null
    ) : ProfileState()
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableLiveData<ProfileState>(ProfileState.Loading)
    val state: LiveData<ProfileState> = _state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val userId = sessionManager.readSessionSnapshot().userId
            val user = userId?.let { authRepository.getUserById(it) }
            _state.postValue(ProfileState.Ready(user))
        }
    }

    fun saveProfile(
        username: String,
        peso: Int?,
        altura: Int?,
        sexo: String?,
        pais: String?,
        fechaNacimiento: Long?
    ) {
        viewModelScope.launch {
            val snapshot = sessionManager.readSessionSnapshot()
            val userId = snapshot.userId
            if (userId.isNullOrBlank()) {
                _state.value = ProfileState.Ready(user = null, error = "Sesion no valida")
                return@launch
            }

            val current = (_state.value as? ProfileState.Ready)?.user
            _state.value = ProfileState.Ready(user = current, isSaving = true)

            when (
                val result = authRepository.updateProfile(
                    userId = userId,
                    username = username.trim(),
                    peso = peso,
                    altura = altura,
                    sexo = sexo?.trim()?.takeIf { it.isNotBlank() },
                    pais = pais?.trim()?.takeIf { it.isNotBlank() },
                    fechaNacimiento = fechaNacimiento
                )
            ) {
                is Result.Success -> {
                    val token = snapshot.token.orEmpty()
                    sessionManager.saveSessionAsync(result.data.userId, token, result.data.username)
                    _state.value = ProfileState.Ready(
                        user = result.data,
                        message = "Perfil actualizado"
                    )
                }

                is Result.Error -> {
                    _state.value = ProfileState.Ready(
                        user = current,
                        error = result.message
                    )
                }
            }
        }
    }
}
