package com.sofar.transform

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
      return
    }

    if (needPreProcess()) {
      val preProcessor = PreTransformProcessor(name, project, this)
      preProcessor.transform(transformInvocation)
    }

    val processor = TransformProcessor(name, project, this)
    processor.transform(transformInvocation)

    afterTransform(transformInvocation)
  }

  open fun needPreProcess(): Boolean {
    return false
  }


  override fun preProcessFile(
    inputFileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ) {

  }

  open fun afterTransform(transformInvocation: TransformInvocation) {

  }

}