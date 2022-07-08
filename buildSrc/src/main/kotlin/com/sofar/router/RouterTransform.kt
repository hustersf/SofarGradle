package com.sofar.router

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.sofar.transform.FileEntity
import com.sofar.transform.ParallelTransform
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class RouterTransform(project: Project) : ParallelTransform(project) {

  private val addClasses: MutableSet<String> = Collections.synchronizedSet(HashSet<String>())
  private val deleteClasses: MutableSet<String> = Collections.synchronizedSet(HashSet<String>())

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

    var outputDir = transformInvocation.outputProvider.getContentLocation(
      name,
      TransformManager.CONTENT_CLASS,
      TransformManager.PROJECT_ONLY,
      Format.DIRECTORY
    )
    generateServiceInitClass(outputDir.absolutePath)
  }

  override fun preProcessFile(
    status: Status,
    fileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ) {
    super.preProcessFile(status, fileEntity, input, output)
    if (status == Status.REMOVED) {
      var className = fileEntity.relativePath.removeSuffix(".class")
      if (className.endsWith(Const.SERVICE_IMPL_SUFFIX)) {
        println("RouterTransform delete:${className}")
        deleteClasses.add(className)
      }
      return
    }

    if (input == null) {
      return
    }
    if (fileEntity.relativePath.endsWith(Const.ROUTER_SERVICE_INFO_FILE_NAME)) {
      println("RouterTransform 读取配置文件:${fileEntity.relativePath}")
      var content = String(input.readAllBytes(), Charsets.UTF_8)
      var strs = content.split("\n")
      strs.forEach {
        if (it.isNotEmpty()) {
          println("RouterTransform 收集:${it}")
          addClasses.add(it)
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
    return false
  }

  private fun generateServiceInitClass(directory: String) {
    if (addClasses.isEmpty() && deleteClasses.isEmpty()) {
      println("RouterTransform no class")
      return
    }
    val className = Const.SERVICE_LOADER_INIT.replace('.', '/')
    val dest = File(directory, "$className.class")
    if (!dest.exists()) {
      val start = System.currentTimeMillis()
      println("RouterTransform start generate class $className from=$directory")
      val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
      classWriter.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, className,
        null, "java/lang/Object", null)
      val cv: ClassVisitor = object : ClassVisitor(Opcodes.ASM7, classWriter) {}

      val mv = cv.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, Const.INIT_METHOD,
        "()V", null, null)
      mv.visitCode()
      mv.visitInsn(Opcodes.RETURN)
      mv.visitEnd()

      cv.visitEnd()

      dest.parentFile.mkdirs()
      FileOutputStream(dest).write(classWriter.toByteArray())

      println("RouterTransform generate class cost ${System.currentTimeMillis() - start}ms")
    }

    val bytes: ByteArray = modifyClass(dest)
    FileOutputStream(dest).write(bytes)
  }

  private fun modifyClass(dest: File): ByteArray {
    //创建ClassReader，传入class字节码的输入流
    var classReader = ClassReader(FileInputStream(dest))
    //创建ClassWriter，绑定classReader
    var classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
    //创建自定义的ClassVisitor，并绑定classWriter
    var classVisitor = RouterVisitor(addClasses, deleteClasses, classWriter)
    //接受一个实现了 ClassVisitor接口的对象实例作为参数，然后依次调用 ClassVisitor接口的各个方法
    classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
    return classWriter.toByteArray()
  }

}