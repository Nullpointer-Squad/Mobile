package com.nullpointer.squad.util

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    fun default(): CoroutineDispatcher
    fun io(): CoroutineDispatcher
    fun main(): CoroutineDispatcher
}