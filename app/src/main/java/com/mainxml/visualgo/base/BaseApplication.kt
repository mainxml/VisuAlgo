package com.mainxml.visualgo.base

import android.app.Application
import android.content.Context

/**
 * @author zcp
 */
class BaseApplication : Application() {

    companion object {
        private lateinit var instance: Application

        fun get() = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}