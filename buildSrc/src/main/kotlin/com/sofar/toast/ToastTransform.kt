package com.sofar.toast

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager

class ToastTransform : Transform() {

  override fun getName(): String {
    return "toast"
  }

  override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
    return TransformManager.CONTENT_CLASS
  }

  override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
    return TransformManager.SCOPE_FULL_PROJECT
  }

  override fun isIncremental(): Boolean {
    return true
  }

  override fun transform(transformInvocation: TransformInvocation?) {
    super.transform(transformInvocation)
    println("ToastTransform start")
    var start = System.currentTimeMillis()
    transformInvocation?.inputs?.forEach {

      it.directoryInputs.forEach { a ->
        println(a.toString())
      }

      it.jarInputs.forEach { b ->
        println(b.toString())
      }

    }
    println("ToastTransform end cost time=${System.currentTimeMillis() - start}ms")
  }

}