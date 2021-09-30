package com.sofar.demo

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

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
   * 处理字节码标准逻辑
   *
   * 可利用 asm 对输入的class文件进行字节码修改，然后复制到输出目录中
   */
  override fun transform(transformInvocation: TransformInvocation?) {
    super.transform(transformInvocation)
    if (transformInvocation == null) {
      println("transformInvocation null")
      return
    }

    println("DemoTransform start")
    var start = System.currentTimeMillis()

    //管理输出路径
    var outputProvider = transformInvocation.outputProvider
    var isIncremental = transformInvocation.isIncremental
    if (!isIncremental) {
      //不是增量编译则删除之前的所有文件
      outputProvider.deleteAll()
    }

    println("DemoTransform isIncremental=$isIncremental")
    transformInvocation.inputs.forEach {
      it.directoryInputs.forEach { a ->
        processDirectoryInput(a, outputProvider, isIncremental)
      }
      it.jarInputs.parallelStream().forEach { b ->
        processJarInput(b, outputProvider, isIncremental)
      }
    }
    println("DemoTransform end cost time=${System.currentTimeMillis() - start}ms")
  }

  private fun processJarInput(
    jarInput: JarInput,
    outputProvider: TransformOutputProvider?,
    isIncremental: Boolean
  ) {
    if (outputProvider == null) {
      println("outputProvider null")
      return
    }

    var status = jarInput.status
    var dest = outputProvider.getContentLocation(
      jarInput.file.absolutePath, jarInput.contentTypes,
      jarInput.scopes, Format.JAR
    )
    if (isIncremental) {
      when (status) {
        Status.NOTCHANGED -> {
        }
        Status.REMOVED -> if (dest.exists()) {
          FileUtils.forceDelete(dest)
        }
        Status.ADDED, Status.CHANGED -> transformJar(jarInput.file, dest)
      }
    } else {
      transformJar(jarInput.file, dest)
    }
  }

  private fun transformJar(jarInputFile: File, dest: File) {
    println("拷贝文件 $dest -----")
    FileUtils.copyFile(jarInputFile, dest)
  }


  private fun processDirectoryInput(
    directoryInput: DirectoryInput,
    outputProvider: TransformOutputProvider?,
    isIncremental: Boolean
  ) {
    if (outputProvider == null) {
      println("outputProvider null")
      return
    }

    var dest = outputProvider.getContentLocation(
      directoryInput.name,
      directoryInput.contentTypes,
      directoryInput.scopes,
      Format.DIRECTORY
    )
    FileUtils.forceMkdir(dest)

    if (isIncremental) {
      var srcDirPath = directoryInput.file.absolutePath
      var destDirPath = dest.absolutePath
      val fileStatusMap = directoryInput.changedFiles
      fileStatusMap.forEach { changedFile ->
        val status: Status = changedFile.value
        val inputFile: File = changedFile.key
        val destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
        val destFile = File(destFilePath)
        when (status) {
          Status.NOTCHANGED -> {
          }
          Status.REMOVED -> if (destFile.exists()) {
            destFile.delete()
          }
          Status.ADDED, Status.CHANGED -> {
            try {
              FileUtils.touch(destFile)
            } catch (e: IOException) {
              //maybe mkdirs fail for some strange reason, try again.
              Files.createParentDirs(destFile)
            }
            transformSingleFile(inputFile, destFile)
          }
        }
      }
    } else {
      transformDirectory(directoryInput.file, dest)
    }
  }

  private fun transformSingleFile(inputFile: File, destFile: File) {
    println("拷贝单个文件 $destFile")
    FileUtils.copyFile(inputFile, destFile)
  }

  private fun transformDirectory(directoryInputFile: File, dest: File) {
    println("拷贝文件夹 $dest -----")
    FileUtils.copyDirectory(directoryInputFile, dest)
  }

}