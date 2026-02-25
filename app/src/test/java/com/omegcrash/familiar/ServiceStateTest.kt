package com.omegcrash.familiar

import com.omegcrash.familiar.service.ServiceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceStateTest {

    @Test
    fun `idle is the default state`() {
        val state: ServiceState = ServiceState.Idle
        assertTrue(state is ServiceState.Idle)
    }

    @Test
    fun `running state carries port`() {
        val state = ServiceState.Running(port = 5000)
        assertEquals(5000, state.port)
    }

    @Test
    fun `error state carries message`() {
        val state = ServiceState.Error("Python crashed")
        assertEquals("Python crashed", state.message)
    }

    @Test
    fun `all states are distinct`() {
        val states = listOf(
            ServiceState.Idle,
            ServiceState.Starting,
            ServiceState.Running(5000),
            ServiceState.Error("err"),
            ServiceState.Stopped,
        )
        assertEquals(5, states.map { it::class }.distinct().size)
    }
}
