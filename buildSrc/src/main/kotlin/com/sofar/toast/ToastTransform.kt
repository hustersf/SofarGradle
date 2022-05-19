package com.sofar.toast

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.internal.pipeline.TransformManager
import com.sofar.transform.FileEntity
import com.sofar.transform.ParallelTransform
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.InputStream
import java.io.OutputStream

class ToastTransform(project: Project) : ParallelTransform(project) {

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

  override fun processFile(
    inputFileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ): Boolean {
    if (inputFileEntity.relativePath.endsWith(".class")) {
      var classReader = ClassReader(input)
      var classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
      var classVisitor = ToastVisitor(inputFileEntity.fromPath, classWriter)
      classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
      val bytes: ByteArray = classWriter.toByteArray()
      output!!.write(bytes)
      return true
    }
    return false
  }

}