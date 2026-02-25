package com.omegcrash.familiar.service

sealed class ServiceState {
    data object Idle : ServiceState()
    data object Starting : ServiceState()
    data class Running(val port: Int) : ServiceState()
    data class Error(val message: String) : ServiceState()
    data object Stopped : ServiceState()
}
