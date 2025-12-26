package com.sofar.plugin.trycatch

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input

interface TryCatchParams : InstrumentationParameters {
  @get:Input
  val targetClassMethod: MapProperty<String, List<String>>

  @get:Input
  val exceptionHandler: MapProperty<String, String>
}