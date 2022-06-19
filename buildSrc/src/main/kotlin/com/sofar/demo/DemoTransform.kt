package com.sofar.demo

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.internal.pipeline.TransformManager
import com.sofar.router.ROUTER_SERVICE_INFO_FILE_NAME
import com.sofar.router.SERVICE_INIT_CLASS_NAME
import com.sofar.transform.FileEntity
import com.sofar.transform.ParallelTransform
import org.gradle.api.Project
import java.io.InputStream
import java.io.OutputStream

/**
 * Transform模板代码
 */
class DemoTransform(project: Project) : ParallelTransform(project) {

  /**
   * 名字，Transform 会生成一个gradle任务
   */
  override fun getName(): String {
    return "demo"
  }

  /**
   * 输入文件类型
   */
  override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
    return TransformManager.CONTENT_CLASS
  }

  /**
   * 输入文件范围
   */
  override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
    return TransformManager.SCOPE_FULL_PROJECT
  }

  /**
   * 是否支持增量编译
   */
  override fun isIncremental(): Boolean {
    return true
  }

  override fun processFile(
    inputFileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ): Boolean {
    if (inputFileEntity.relativePath.endsWith(ROUTER_SERVICE_INFO_FILE_NAME)) {
      println("DemoTransform:" + inputFileEntity.relativePath+ "  from="+inputFileEntity.fromPath)
    }

    if (inputFileEntity.relativePath.endsWith(".class")) {
      var className = inputFileEntity.relativePath.replace("/", ".")
      if (SERVICE_INIT_CLASS_NAME.equals(className.removeSuffix(".class"))) {
        println("RouterTransform:" + inputFileEntity.relativePath + "  from=" + inputFileEntity.fromPath)
      }
    }
    return false
  }

}