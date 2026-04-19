package com.example.sleepmonitor.ui.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionManager(context: Context) {

    private val appContext = context.applicationContext

    @Volatile
    private var cachedPrefs: SharedPreferences? = null

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "session_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_ONBOARDING_SEEN = "onboarding_seen"
        private const val KEY_ACTIVE_SESSION_ID = "active_session_id"
        private const val KEY_ACTIVE_SESSION_USER_ID = "active_session_user_id"
        private const val KEY_ACTIVE_SESSION_STARTED_AT = "active_session_started_at"
        private const val KEY_ACTIVE_SESSION_ALARM_START = "active_session_alarm_start"
        private const val KEY_ACTIVE_SESSION_ALARM_END = "active_session_alarm_end"
        private const val KEY_ACTIVE_SESSION_SAMPLE_INTERVAL = "active_session_sample_interval"
    }

    data class SessionSnapshot(
        val userId: String?,
        val token: String?,
        val username: String?
    )

    data class ActiveSleepSession(
        val sessionId: String,
        val userId: String,
        val startedAt: Long,
        val alarmWindowStart: String,
        val alarmWindowEnd: String,
        val sampleIntervalMs: Long
    )

    private fun prefs(): SharedPreferences = cachedPrefs ?: synchronized(this) {
        cachedPrefs ?: createPrefs().also { cachedPrefs = it }
    }

    private fun createPrefs(): SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            appContext,
            "sleep_monitor_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse { error ->
        Log.w(
            "SessionManager",
            "No se pudo inicializar EncryptedSharedPreferences. Se usa almacenamiento local sin cifrado.",
            error
        )
        appContext.getSharedPreferences("sleep_monitor_prefs_fallback", Context.MODE_PRIVATE)
    }

    suspend fun warmUp() = withContext(Dispatchers.IO) {
        prefs()
    }

    suspend fun readSessionSnapshot(): SessionSnapshot = withContext(Dispatchers.IO) {
        SessionSnapshot(
            userId = getUserId(),
            token = getToken(),
            username = getUsername()
        )
    }

    suspend fun saveSessionAsync(userId: String, token: String, username: String) =
        withContext(Dispatchers.IO) {
            saveSession(userId, token, username)
        }

    suspend fun setOnboardingSeenAsync() = withContext(Dispatchers.IO) {
        setOnboardingSeen()
    }

    suspend fun hasSeenOnboardingAsync(): Boolean = withContext(Dispatchers.IO) {
        hasSeenOnboarding()
    }

    suspend fun saveActiveSleepSessionAsync(
        sessionId: String,
        userId: String,
        startedAt: Long,
        alarmWindowStart: String,
        alarmWindowEnd: String,
        sampleIntervalMs: Long
    ) = withContext(Dispatchers.IO) {
        saveActiveSleepSession(
            sessionId = sessionId,
            userId = userId,
            startedAt = startedAt,
            alarmWindowStart = alarmWindowStart,
            alarmWindowEnd = alarmWindowEnd,
            sampleIntervalMs = sampleIntervalMs
        )
    }

    suspend fun getActiveSleepSessionAsync(): ActiveSleepSession? = withContext(Dispatchers.IO) {
        getActiveSleepSession()
    }

    suspend fun clearActiveSleepSessionAsync() = withContext(Dispatchers.IO) {
        clearActiveSleepSession()
    }

    suspend fun clearSessionAsync() = withContext(Dispatchers.IO) {
        clearSession()
    }

    fun saveSession(userId: String, token: String, username: String) {
        prefs().edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_TOKEN, token)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getUserId(): String? = prefs().getString(KEY_USER_ID, null)

    fun getToken(): String? = prefs().getString(KEY_TOKEN, null)

    fun getUsername(): String? = prefs().getString(KEY_USERNAME, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun hasSeenOnboarding(): Boolean = prefs().getBoolean(KEY_ONBOARDING_SEEN, false)

    fun setOnboardingSeen() {
        prefs().edit().putBoolean(KEY_ONBOARDING_SEEN, true).apply()
    }

    fun saveActiveSleepSession(
        sessionId: String,
        userId: String,
        startedAt: Long,
        alarmWindowStart: String,
        alarmWindowEnd: String,
        sampleIntervalMs: Long
    ) {
        prefs().edit()
            .putString(KEY_ACTIVE_SESSION_ID, sessionId)
            .putString(KEY_ACTIVE_SESSION_USER_ID, userId)
            .putLong(KEY_ACTIVE_SESSION_STARTED_AT, startedAt)
            .putString(KEY_ACTIVE_SESSION_ALARM_START, alarmWindowStart)
            .putString(KEY_ACTIVE_SESSION_ALARM_END, alarmWindowEnd)
            .putLong(KEY_ACTIVE_SESSION_SAMPLE_INTERVAL, sampleIntervalMs)
            .apply()
    }

    fun getActiveSleepSession(): ActiveSleepSession? {
        val prefs = prefs()
        val sessionId = prefs.getString(KEY_ACTIVE_SESSION_ID, null) ?: return null
        val userId = prefs.getString(KEY_ACTIVE_SESSION_USER_ID, null) ?: return null
        val startedAt = prefs.getLong(KEY_ACTIVE_SESSION_STARTED_AT, 0L)
        val alarmStart = prefs.getString(KEY_ACTIVE_SESSION_ALARM_START, null) ?: return null
        val alarmEnd = prefs.getString(KEY_ACTIVE_SESSION_ALARM_END, null) ?: return null
        val sampleInterval = prefs.getLong(KEY_ACTIVE_SESSION_SAMPLE_INTERVAL, 10_000L)
        return ActiveSleepSession(
            sessionId = sessionId,
            userId = userId,
            startedAt = startedAt,
            alarmWindowStart = alarmStart,
            alarmWindowEnd = alarmEnd,
            sampleIntervalMs = sampleInterval
        )
    }

    fun clearActiveSleepSession() {
        prefs().edit()
            .remove(KEY_ACTIVE_SESSION_ID)
            .remove(KEY_ACTIVE_SESSION_USER_ID)
            .remove(KEY_ACTIVE_SESSION_STARTED_AT)
            .remove(KEY_ACTIVE_SESSION_ALARM_START)
            .remove(KEY_ACTIVE_SESSION_ALARM_END)
            .remove(KEY_ACTIVE_SESSION_SAMPLE_INTERVAL)
            .apply()
    }

    fun clearSession() {
        prefs().edit()
            .remove(KEY_USER_ID)
            .remove(KEY_TOKEN)
            .remove(KEY_USERNAME)
            .remove(KEY_ACTIVE_SESSION_ID)
            .remove(KEY_ACTIVE_SESSION_USER_ID)
            .remove(KEY_ACTIVE_SESSION_STARTED_AT)
            .remove(KEY_ACTIVE_SESSION_ALARM_START)
            .remove(KEY_ACTIVE_SESSION_ALARM_END)
            .remove(KEY_ACTIVE_SESSION_SAMPLE_INTERVAL)
            .apply()
    }
}
