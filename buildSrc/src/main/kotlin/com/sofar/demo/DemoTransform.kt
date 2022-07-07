package com.sofar.demo

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.gradle.internal.pipeline.TransformManager
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

  override fun needPreProcess(): Boolean {
    return false
  }

  override fun preProcessFile(
    status: Status,
    fileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ) {
    super.preProcessFile(status, fileEntity, input, output)
    println("DemoTransform: preProcessFile status=$status file=${fileEntity.relativePath} " +
        "isJar=${fileEntity.isJarInput}")
  }

  override fun processFile(
    status: Status,
    fileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ): Boolean {
    println("DemoTransform: processFile status=$status file=${fileEntity.relativePath} " +
        "isJar=${fileEntity.isJarInput}")
    return false
  }

}