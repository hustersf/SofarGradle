package com.sofar.transform

import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.sofar.transform.processor.ProcessAction
import com.sofar.transform.processor.TransformProcessor
import org.gradle.api.Project

/**
 * 支持并行的 Transform
 * 同时封装了 Transform 的标准流程
 */
abstract class ParallelTransform(private val project: Project) : Transform(), ProcessAction {

  override fun transform(transformInvocation: TransformInvocation?) {
    super.transform(transformInvocation)

    val processor = TransformProcessor(name, project, this)
    processor.transform(transformInvocation)
  }

}