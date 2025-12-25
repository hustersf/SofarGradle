package com.sofar.plugin.router

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.objectweb.asm.*
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

abstract class RouterTransformTask : DefaultTask() {
  @get:InputFiles
  abstract val allJars: ListProperty<RegularFile>

  @get:InputFiles
  abstract val allDirs: ListProperty<Directory>

  @get:OutputFile
  abstract val outputJar: RegularFileProperty

  @TaskAction
  fun transform() {
    val routerClasses = mutableSetOf<String>()
    val allInputs = mutableListOf<File>()
    val addedEntries = mutableSetOf<String>()

    // 收集所有输入源
    allJars.get().forEach { allInputs.add(it.asFile) }
    allDirs.get().forEach { allInputs.add(it.asFile) }

    // 索引文件的相对路径
    val path = RouterConfig.SERVICE_FILE_NAME
    println("RouterTransformTask: 开始通过索引文件提取类信息...")
    // --- 第一阶段：提取索引中的类 ---
    allInputs.forEach { file ->
      if (file.isFile && file.extension == "jar") {
        JarFile(file).use { jar ->
          // O(1) 级别定位索引文件
          jar.getJarEntry(path)?.let { entry ->
            jar.getInputStream(entry).bufferedReader().useLines { lines ->
              lines.filter { it.isNotBlank() }.forEach { className ->
                println("RouterTransformTask: [！！！命中目标] JAR [${file.name}] -> $className")
                routerClasses.add(className)
              }
            }
          }
        }
      } else if (file.isDirectory) {
        val idxFile = File(file, path)
        if (idxFile.exists()) {
          idxFile.useLines { lines ->
            lines.filter { it.isNotBlank() }.forEach { className ->
              // 打印目录中发现的每一个路由类
              println("RouterTransformTask: [！！！命中目标] 目录 [${file.name}] -> $className")
              routerClasses.add(className)
            }
          }
        }
      }
    }

    println("RouterTransformTask: 提取完成，共找到 ${routerClasses.size} 个待注入类。开始注入...")

    // --- 第二阶段：执行字节码注入并重新打包 ---
    JarOutputStream(FileOutputStream(outputJar.get().asFile)).use { jos ->
      allInputs.forEach { file ->
        if (file.extension == "jar") {
          JarFile(file).use { jar ->
            jar.entries().asSequence().forEach { entry ->
              // 核心修正：过滤掉重复的目录条目和重复文件，防止 duplicate entry 报错
              if (!entry.isDirectory && !addedEntries.contains(entry.name)) {
                // 排除清单文件，防止重复冲突
                if (entry.name == "META-INF/MANIFEST.MF") return@forEach

                jos.putNextEntry(JarEntry(entry.name))
                val className = formatClassName(entry.name)
                if (className == RouterConfig.TARGET_CLASS_NAME) {
                  println("RouterTransformTask: [正在注入目标类] JAR -> ${entry.name}")
                  val bytes = jar.getInputStream(entry).readBytes()
                  jos.write(modifyClass(bytes, routerClasses))
                } else {
                  // 非目标类，直接流拷贝，性能最高
                  jar.getInputStream(entry).copyTo(jos)
                }
                jos.closeEntry()
                addedEntries.add(entry.name)
              }
            }
          }
        } else if (file.isDirectory) {
          file.walkTopDown().filter { it.isFile }.forEach { classFile ->
            val entryName = classFile.absolutePath
              .substringAfter(file.absolutePath + File.separator)
              .replace(File.separator, "/")

            if (!addedEntries.contains(entryName)) {
              jos.putNextEntry(JarEntry(entryName))
              val className = formatClassName(entryName)
              if (className == RouterConfig.TARGET_CLASS_NAME) {
                println("RouterTransformTask: [正在注入目标类] 目录 -> $entryName")
                jos.write(modifyClass(classFile.readBytes(), routerClasses))
              } else {
                classFile.inputStream().use { it.copyTo(jos) }
              }
              jos.closeEntry()
              addedEntries.add(entryName)
            }
          }
        }
      }
    }
    println("RouterTransformTask: 任务执行成功，产物已输出至: ${outputJar.get().asFile.absolutePath}")
  }

  private fun formatClassName(entryName: String): String {
    return entryName.removeSuffix(".class")
      .replace('/', '.')  // 处理 JAR 路径和 Linux 路径
      .replace('\\', '.') // 处理 Windows 路径
  }

  private fun modifyClass(srcByte: ByteArray, routerClasses: Set<String>): ByteArray {
    val reader = ClassReader(srcByte)
    // 使用 COMPUTE_FRAMES 自动计算栈图，最稳妥
    val writer = ClassWriter(reader, ClassWriter.COMPUTE_FRAMES)
    val visitor = object : ClassVisitor(Opcodes.ASM9, writer) {
      override fun visitMethod(
        access: Int, name: String?, descriptor: String?,
        signature: String?, exceptions: Array<out String>?
      ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        // 匹配目标方法
        if (name == RouterConfig.TARGET_METHOD_NAME && descriptor == RouterConfig.TARGET_METHOD_DESC) {
          return object : MethodVisitor(Opcodes.ASM9, mv) {
            override fun visitInsn(opcode: Int) {
              // 在 RETURN 指令前注入静态调用
              if (opcode == Opcodes.RETURN) {
                routerClasses.forEach { className ->
                  val asmName = className.replace(".", "/")
                  // 调用 xxx_Router_.load()
                  visitMethodInsn(Opcodes.INVOKESTATIC, asmName, RouterConfig.INJECT_METHOD_NAME,
                    RouterConfig.INJECT_METHOD_DESC, false)
                }
              }
              super.visitInsn(opcode)
            }
          }

        }
        return mv
      }
    }
    reader.accept(visitor, ClassReader.EXPAND_FRAMES)
    return writer.toByteArray()
  }
}