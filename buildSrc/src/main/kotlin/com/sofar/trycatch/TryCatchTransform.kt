package com.sofar.trycatch

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.gradle.internal.pipeline.TransformManager
import com.sofar.transform.FileEntity
import com.sofar.transform.ParallelTransform
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassReader.EXPAND_FRAMES
import org.objectweb.asm.ClassWriter
import java.io.InputStream
import java.io.OutputStream

class TryCatchTransform(project: Project, var extension: TryCatchExtension) :
  ParallelTransform(project) {

  override fun getName(): String {
    return "try-catch"
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

  override fun processFile(
    status: Status,
    fileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ): Boolean {
    if (status == Status.REMOVED) {
      return false
    }

    var className = fileEntity.relativePath.replace("/", ".")
    if (isTargetClass(className)) {
      println("TryCatchTransform processFile className=$className")
      val bytes: ByteArray = targetClassToByteArray(input!!)
      output!!.write(bytes)
      return true
    }
    return false
  }

  private fun targetClassToByteArray(inputStream: InputStream): ByteArray {
    //创建ClassReader，传入class字节码的输入流
    var classReader = ClassReader(inputStream)
    //创建ClassWriter，绑定classReader
    var classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    //创建自定义的ClassVisitor，并绑定classWriter
    var classVisitor = TryCatchVisitor(extension, classWriter)
    //接受一个实现了 ClassVisitor接口的对象实例作为参数，然后依次调用 ClassVisitor接口的各个方法
    classReader.accept(classVisitor, EXPAND_FRAMES)
    return classWriter.toByteArray()
  }

  private fun isTargetClass(fullQualifiedClassName: String): Boolean {
    return extension.targetClassMethod.containsKey(fullQualifiedClassName.replace(".class", ""))
  }

}