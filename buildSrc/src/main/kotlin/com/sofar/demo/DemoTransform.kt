package com.sofar.demo

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils

/**
 * Transform模板代码
 */
class DemoTransform : Transform() {

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

  /**
   * 处理字节码逻辑
   *
   * 可利用 asm 对输入的class文件进行字节码修改，然后复制到输出目录中
   */
  override fun transform(transformInvocation: TransformInvocation?) {
    super.transform(transformInvocation)
    println("DemoTransform start")
    var start = System.currentTimeMillis()

    //管理输出路径
    var outputProvider = transformInvocation?.outputProvider

    transformInvocation?.inputs?.forEach {
      it.directoryInputs.forEach { a ->
        processDirectoryInput(a, outputProvider)
      }
      it.jarInputs.forEach { b ->
        processJarInput(b, outputProvider)
      }
    }
    println("DemoTransform end cost time=${System.currentTimeMillis() - start}ms")
  }

  private fun processJarInput(jarInput: JarInput, outputProvider: TransformOutputProvider?) {
    if (outputProvider == null) {
      return
    }

    println(jarInput.toString())
    var destFile = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes,
        jarInput.scopes, Format.JAR)
    println("copy dest=${destFile.absolutePath}")
    FileUtils.copyFile(jarInput.file, destFile)
  }

  private fun processDirectoryInput(directoryInput: DirectoryInput, outputProvider: TransformOutputProvider?) {
    if (outputProvider == null) {
      return
    }

    println(directoryInput.toString())
    var destFile = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes,
        directoryInput.scopes, Format.DIRECTORY)
    println("copy dest=${destFile.absolutePath}")
    FileUtils.copyDirectory(directoryInput.file, destFile)
  }

}