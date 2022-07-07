package com.sofar.transform

import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.sofar.transform.processor.PreProcessAction
import com.sofar.transform.processor.PreTransformProcessor
import com.sofar.transform.processor.ProcessAction
import com.sofar.transform.processor.TransformProcessor
import org.gradle.api.Project
import java.io.InputStream
import java.io.OutputStream

/**
 * 支持并行的 Transform
 * 同时封装了 Transform 的标准流程
 */
abstract class ParallelTransform(private val project: Project) : Transform(), ProcessAction,
  PreProcessAction {

  override fun transform(transformInvocation: TransformInvocation?) {
    super.transform(transformInvocation)
    if (transformInvocation == null) {
      println("Transform $name transformInvocation null")
      return
    }

    println("Transform $name start")
    var start = System.currentTimeMillis()
    if (needPreProcess()) {
      println("Transform $name start preProcess")
      var s1 = System.currentTimeMillis()
      val preProcessor = PreTransformProcessor(name, project, this)
      preProcessor.transform(transformInvocation)
      println("Transform $name preProcess cost time=${System.currentTimeMillis() - s1}ms")
    }

    println("Transform $name start process")
    var s2 = System.currentTimeMillis()
    val processor = TransformProcessor(name, project, this)
    processor.transform(transformInvocation)
    println("Transform $name process cost time=${System.currentTimeMillis() - s2}ms")

    afterTransform(transformInvocation)

    println("Transform $name end cost time=${System.currentTimeMillis() - start}ms")
  }

  open fun needPreProcess(): Boolean {
    return false
  }


  override fun preProcessFile(
    status: Status,
    fileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ) {

  }

  open fun afterTransform(transformInvocation: TransformInvocation) {

  }

}