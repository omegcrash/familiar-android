package com.omegcrash.familiar

import com.omegcrash.familiar.data.FamiliarClient
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamiliarClientTest {

    @Test
    fun `isHealthy returns false when server is not running`() = runTest {
        val client = FamiliarClient(baseUrl = "http://127.0.0.1:19999")
        assertFalse(client.isHealthy())
    }

    @Test
    fun `chat returns failure when server is not running`() = runTest {
        val client = FamiliarClient(baseUrl = "http://127.0.0.1:19999")
        val result = client.chat("hello")
        assertTrue(result.isFailure)
    }

    @Test
    fun `getStatus returns failure when server is not running`() = runTest {
        val client = FamiliarClient(baseUrl = "http://127.0.0.1:19999")
        val result = client.getStatus()
        assertTrue(result.isFailure)
    }
}
