package com.example.sleepmonitor.ui.recommendations

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.example.sleepmonitor.data.local.entities.RecommendationEntity
import com.example.sleepmonitor.data.repository.RecommendationRepository
import com.example.sleepmonitor.ui.utils.SessionManager
import kotlinx.coroutines.Dispatchers

class RecommendationsViewModel(
    private val repository: RecommendationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val recommendations: LiveData<List<RecommendationEntity>> =
        liveData(context = Dispatchers.IO) {
            val userId = sessionManager.readSessionSnapshot().userId
            if (userId.isNullOrBlank()) {
                emit(emptyList())
            } else {
                emitSource(repository.getRecommendationsFlow(userId).asLiveData())
            }
        }

    val showGeneric: LiveData<Boolean> = liveData(context = Dispatchers.IO) {
        val userId = sessionManager.readSessionSnapshot().userId
        if (userId.isNullOrBlank()) {
            emit(true)
        } else {
            emit(!repository.hasRecommendations(userId))
        }
    }

    fun getGenericRecommendations(): List<Pair<String, String>> {
        return repository.getGenericRecommendations()
    }
}
