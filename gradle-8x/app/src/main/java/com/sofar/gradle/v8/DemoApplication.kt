package com.sofar.gradle.v8

import android.app.Application
import com.sofar.router.Router

class DemoApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    Router.get().init()
  }
}