package com.mainxml.visualgo.util

import com.mainxml.visualgo.animation.LazyAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author zcp
 */
class Sync {

    private var continuationQueue: Queue<Continuation<Unit>> = LinkedList()

    /**
     * sync.execute {
     *     lazyAnimator.invoke().apply {
     *         playingAnimator = this
     *         addListener(self)
     *     }
     * }
     * @param block LazyAnimator
     */
    fun execute(block: LazyAnimator) {
        CoroutineScope(Dispatchers.Default).launch {
            suspend()
            withContext(Dispatchers.Main) {
                block().start()
            }
        }
    }

    private suspend fun suspend() {
        suspendCoroutine {
            continuationQueue.offer(it)
        }
    }

    fun resume() {
        continuationQueue.poll()?.resume(Unit)
    }
}