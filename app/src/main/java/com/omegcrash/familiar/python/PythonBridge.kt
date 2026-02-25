package com.omegcrash.familiar.python

import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

object PythonBridge {

    private var initialized = false

    @Synchronized
    fun ensureInitialized() {
        if (!initialized && !Python.isStarted()) {
            Python.start(AndroidPlatform())
            initialized = true
        }
    }

    fun start(dataDir: String, envVars: Map<String, String>) {
        ensureInitialized()
        val py = Python.getInstance()
        val module = py.getModule("start_familiar")
        val pyEnvVars = py.builtins.callAttr("dict")
        for ((k, v) in envVars) {
            pyEnvVars.callAttr("__setitem__", k, v)
        }
        module.callAttr("start", dataDir, pyEnvVars)
    }

    fun stop() {
        ensureInitialized()
        val py = Python.getInstance()
        val module = py.getModule("start_familiar")
        try {
            module.callAttr("stop")
        } catch (_: Exception) {
            // Python may already be stopped
        }
    }
}
