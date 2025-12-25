package com.sofar.plugin.demo

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File

abstract class GenerateClassTask : DefaultTask() {

  @get:InputFiles
  abstract val allJars: ListProperty<RegularFile>

  @get:InputFiles
  abstract val allDirectories: ListProperty<Directory>

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @TaskAction
  fun taskAction() {
    val outDir = outputDir.get().asFile

    // 1. 定义类信息
    val className = "com/example/generated/MyGeneratedClass"

    // 2. 使用 ASM 生成字节码
    // ClassWriter.COMPUTE_FRAMES 会自动计算栈映射帧（Stack Map Frames）
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    // 定义类头: JDK 1.8, public, 类名, 无签名, 父类是 Object, 无接口
    cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null)

    // 定义一个静态常量字段: public static final String TAG = "GENERATED_CLASS"
    val fieldVisitor = cw.visitField(
      Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
      "TAG",
      "Ljava/lang/String;",
      null,
      "GENERATED_CLASS"
    )
    fieldVisitor.visitEnd()

    // 定义默认构造函数 <init>()
    val mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
    mv.visitCode()
    mv.visitVarInsn(Opcodes.ALOAD, 0) // 将 this 压入栈
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false) // 调用父类构造
    mv.visitInsn(Opcodes.RETURN)
    mv.visitMaxs(1, 1) // 设置栈和局部变量大小
    mv.visitEnd()

    cw.visitEnd()

    // 3. 将生成的字节码写入文件系统
    val classBytes = cw.toByteArray()
    val classFile = File(outDir, "$className.class")
    classFile.parentFile.mkdirs() // 自动创建 com/example/generated 目录
    classFile.writeBytes(classBytes)

    println("ASM: 已生成类 $className 到 ${classFile.absolutePath}")
  }
}