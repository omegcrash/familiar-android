package com.omegcrash.familiar.python

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

object PythonBridge {

    private var initialized = false

    @Synchronized
    fun ensureInitialized(context: Context) {
        if (!initialized && !Python.isStarted()) {
            Python.start(AndroidPlatform(context))
            initialized = true
        }
    }

    fun start(context: Context, dataDir: String, envVars: Map<String, String>) {
        ensureInitialized(context)
        val py = Python.getInstance()
        val module = py.getModule("start_familiar")
        val pyEnvVars = py.builtins.callAttr("dict")
        for ((k, v) in envVars) {
            pyEnvVars.callAttr("__setitem__", k, v)
        }
        module.callAttr("start", dataDir, pyEnvVars)
    }

    fun stop(context: Context) {
        ensureInitialized(context)
        val py = Python.getInstance()
        val module = py.getModule("start_familiar")
        try {
            module.callAttr("stop")
        } catch (_: Exception) {
            // Python may already be stopped
        }
    }
}
