package com.sofar.gradle.v8

import android.app.Application
import com.sofar.router.Router

class DemoApplication : Application() {

  companion object {
    lateinit var app: Application
  }

  override fun onCreate() {
    super.onCreate()
    app = this
    Router.get().init()
  }
}