package com.omegcrash.familiar.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import com.omegcrash.familiar.data.PreferencesStore
import com.omegcrash.familiar.notifications.NotificationHelper
import com.omegcrash.familiar.python.PythonBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class FamiliarService : Service() {

    companion object {
        private val _state = MutableStateFlow<ServiceState>(ServiceState.Idle)
        val state: StateFlow<ServiceState> = _state.asStateFlow()
        private const val NOTIFICATION_ID = 1
        var dashboardApiKey: String = ""
            private set
        var baseUrl: String = "http://127.0.0.1:5000"
            private set
        var isRemoteMode: Boolean = false
            private set
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null
    private var pythonThread: Thread? = null

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationHelper.buildServiceNotification(this)
        startForeground(NOTIFICATION_ID, notification)

        if (_state.value !is ServiceState.Running && _state.value !is ServiceState.Starting) {
            startPython()
        }

        return START_STICKY
    }

    private fun startPython() {
        _state.value = ServiceState.Starting

        scope.launch {
            try {
                val prefs = PreferencesStore(this@FamiliarService)
                val mode = prefs.connectionMode.first()

                if (mode == "remote") {
                    // Remote mode: skip Python/Chaquopy, use user-provided server
                    isRemoteMode = true
                    baseUrl = prefs.serverUrl.first().trimEnd('/')
                    dashboardApiKey = prefs.remoteApiKey.first()
                    _state.value = ServiceState.Running(port = 0)
                    return@launch
                }

                // Local mode: start Python via Chaquopy
                isRemoteMode = false
                baseUrl = "http://127.0.0.1:5000"
                val envVars = prefs.getEnvVars().first()
                val dataDir = filesDir.resolve(".familiar").absolutePath

                pythonThread = Thread({
                    try {
                        PythonBridge.start(this@FamiliarService, dataDir, envVars)
                    } catch (e: Exception) {
                        _state.value = ServiceState.Error(e.message ?: "Python crashed")
                    }
                }, "familiar-python")

                pythonThread?.start()

                // Poll for dashboard API key file (written by Python before Flask binds)
                val keyFile = File(filesDir, ".familiar/.dashboard_key")
                var waited = 0L
                while (!keyFile.exists() && waited < 10_000) {
                    Thread.sleep(500)
                    waited += 500
                }
                if (keyFile.exists()) {
                    dashboardApiKey = keyFile.readText().trim()
                }

                // Give Flask a moment to bind, then mark running
                if (waited < 3000) Thread.sleep(3000 - waited)
                if (_state.value is ServiceState.Starting) {
                    _state.value = ServiceState.Running(port = 5000)
                }
            } catch (e: Exception) {
                _state.value = ServiceState.Error(e.message ?: "Failed to start")
            }
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "familiar:service"
        ).apply { acquire() }
    }

    override fun onDestroy() {
        _state.value = ServiceState.Stopped
        pythonThread?.interrupt()
        wakeLock?.let { if (it.isHeld) it.release() }
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
