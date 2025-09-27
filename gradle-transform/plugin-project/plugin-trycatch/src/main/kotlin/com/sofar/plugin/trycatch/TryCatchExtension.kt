package com.sofar.plugin.trycatch

open class TryCatchExtension {

  lateinit var targetClassMethod: Map<String, List<String>>

  var exceptionHandler: Map<String, String>? = null

  override fun toString(): String {
    return "TryCatchExtension{targetClassMethod=$targetClassMethod " +
        "exceptionHandler=$exceptionHandler}"
  }

}