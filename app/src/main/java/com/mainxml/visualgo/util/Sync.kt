package com.mainxml.visualgo.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author zcp
 */
class Sync {

    private var suspend = true

    fun test() {
        CoroutineScope(Dispatchers.Default).launch {
            println("@@ 1")
            suspend()
            println("@@ 2")
            suspend()
            println("@@ 3")
        }
    }

    private suspend fun suspend() {
        suspendCoroutine {
            CoroutineScope(Dispatchers.Default).launch {
                while (suspend) {
                    /* no-op */
                }
                suspend = true
                it.resume(Unit)
            }
        }
    }

    fun resume() {
        suspend = false
    }
}