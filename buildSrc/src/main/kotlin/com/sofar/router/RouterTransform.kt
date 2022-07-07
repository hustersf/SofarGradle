package com.sofar.router

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.sofar.transform.FileEntity
import com.sofar.transform.ParallelTransform
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.HashSet

class RouterTransform(project: Project) : ParallelTransform(project) {

  private val serviceClasses: MutableSet<String> = Collections.synchronizedSet(HashSet<String>())

  override fun getName(): String {
    return "router"
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

  override fun needPreProcess(): Boolean {
    return true
  }

  override fun afterTransform(transformInvocation: TransformInvocation) {
    super.afterTransform(transformInvocation)
    println("RouterTransform afterTransform")
  }

  override fun preProcessFile(
    status: Status,
    fileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ) {
    super.preProcessFile(status, fileEntity, input, output)
    if (input == null) {
      return
    }
    if (fileEntity.relativePath.endsWith(ROUTER_SERVICE_INFO_FILE_NAME)) {
      println("RouterTransform 读取配置文件:${fileEntity.relativePath}")
      var content = String(input.readAllBytes(), Charsets.UTF_8)
      var strs = content.split("\n")
      strs.forEach {
        if (it.isNotEmpty()) {
          println("RouterTransform 收集:${it}")
          serviceClasses.add(it)
        }
      }
    }
  }

  override fun processFile(
    status: Status,
    fileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ): Boolean {
    if (input == null || output == null) {
      return false
    }

    if (fileEntity.relativePath.endsWith(".class")) {
      var className = fileEntity.relativePath.replace("/", ".")
      if (SERVICE_INIT_CLASS_NAME == className.removeSuffix(".class")) {
        println("RouterTransform:" + fileEntity.relativePath + "  from=" + fileEntity.fromPath)
        val bytes: ByteArray = targetClassToByteArray(input)
        output.write(bytes)
        return true
      }
    }
    return false
  }

  private fun targetClassToByteArray(inputStream: InputStream): ByteArray {
    //创建ClassReader，传入class字节码的输入流
    var classReader = ClassReader(inputStream)
    //创建ClassWriter，绑定classReader
    var classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    //创建自定义的ClassVisitor，并绑定classWriter
    var classVisitor = RouterVisitor(serviceClasses, classWriter)
    //接受一个实现了 ClassVisitor接口的对象实例作为参数，然后依次调用 ClassVisitor接口的各个方法
    classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
    return classWriter.toByteArray()
  }

}