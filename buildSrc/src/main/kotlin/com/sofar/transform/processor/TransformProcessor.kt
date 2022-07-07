package com.sofar.transform.processor

import com.android.build.api.transform.TransformInvocation
import org.gradle.api.Project

/**
 * Transform标准流程
 */
open class TransformProcessor(
  private val name: String,
  private val project: Project,
  private val action: ProcessAction,
) {

  private val mFileProcessor: FileProcessor = FileProcessor(action)
  private val mJarProcessor: JarProcessor = JarProcessor(action)

  fun transform(transformInvocation: TransformInvocation) {

    //管理输出路径
    var outputProvider = transformInvocation.outputProvider
    var isIncremental = transformInvocation.isIncremental
    if (!isIncremental) {
      //不是增量编译则删除之前的所有文件
      outputProvider.deleteAll()
    }
    println("Transform $name isIncremental=$isIncremental")
    transformInvocation.inputs.forEach {
      it.directoryInputs.forEach { directoryInput ->
        mFileProcessor.processDirectoryInput(transformInvocation, directoryInput)
      }
      it.jarInputs.forEach { jarInput ->
        mJarProcessor.processJarInput(transformInvocation, jarInput)
      }
    }
  }
}