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

class FamiliarService : Service() {

    companion object {
        private val _state = MutableStateFlow<ServiceState>(ServiceState.Idle)
        val state: StateFlow<ServiceState> = _state.asStateFlow()
        private const val NOTIFICATION_ID = 1
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
                val envVars = prefs.getEnvVars().first()
                val dataDir = filesDir.resolve(".familiar").absolutePath

                pythonThread = Thread({
                    try {
                        PythonBridge.start(dataDir, envVars)
                    } catch (e: Exception) {
                        _state.value = ServiceState.Error(e.message ?: "Python crashed")
                    }
                }, "familiar-python")

                pythonThread?.start()

                // Give Flask a moment to bind, then mark running
                Thread.sleep(3000)
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
