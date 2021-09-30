package com.sofar.trycatch

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
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassReader.EXPAND_FRAMES
import org.objectweb.asm.ClassWriter
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.attribute.FileTime
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class TryCatchTransform(var extension: TryCatchExtension) : Transform() {

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

  override fun transform(transformInvocation: TransformInvocation?) {
    super.transform(transformInvocation)
    if (transformInvocation == null) {
      println("transformInvocation null")
      return
    }

    println("TryCatchTransform start")
    var start = System.currentTimeMillis()

    //管理输出路径
    var outputProvider = transformInvocation.outputProvider
    var isIncremental = transformInvocation.isIncremental
    if (!isIncremental) {
      //不是增量编译则删除之前的所有文件
      outputProvider.deleteAll()
    }

    println("TryCatchTransform isIncremental=$isIncremental")
    transformInvocation.inputs.forEach {
      it.directoryInputs.forEach { a ->
        processDirectoryInput(a, outputProvider, isIncremental)
      }
      it.jarInputs.parallelStream().forEach { b ->
        processJarInput(b, outputProvider, isIncremental)
      }
    }
    println("TryCatchTransform end cost time=${System.currentTimeMillis() - start}ms")
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

  private fun transformJar(inputJar: File, outputJar: File) {
    val inputZip = ZipFile(inputJar)
    val outputZip = ZipOutputStream(
      BufferedOutputStream(
        java.nio.file.Files.newOutputStream(outputJar.toPath())
      )
    )
    val inEntries = inputZip.entries()
    while (inEntries.hasMoreElements()) {
      val entry = inEntries.nextElement()
      val originalFile: InputStream = BufferedInputStream(inputZip.getInputStream(entry))
      val outEntry = ZipEntry(entry.name)
      var newEntryContent: ByteArray = if (isTargetClass(outEntry.name.replace("/", "."))) {
        println("target class=${outEntry.name} outputJar=${outputJar.absolutePath}")
        targetClassToByteArray(originalFile)
      } else {
        IOUtils.toByteArray(originalFile)
      }
      val crc32 = CRC32()
      crc32.update(newEntryContent)
      outEntry.crc = crc32.value
      outEntry.method = ZipEntry.STORED
      outEntry.size = newEntryContent.size.toLong()
      outEntry.compressedSize = newEntryContent.size.toLong()
      outEntry.lastAccessTime = FileTime.fromMillis(0)
      outEntry.lastModifiedTime = FileTime.fromMillis(0)
      outEntry.creationTime = FileTime.fromMillis(0)
      outputZip.putNextEntry(outEntry)
      outputZip.write(newEntryContent)
      outputZip.closeEntry()
    }
    outputZip.flush()
    outputZip.close()
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
            transformSingleFile(inputFile, destFile, srcDirPath)
          }
        }
      }
    } else {
      transformDirectory(directoryInput.file, dest)
    }
  }

  private fun transformSingleFile(inputFile: File, outputFile: File, inputDir: String) {
    writeClassToFile(inputFile, outputFile, inputDir)
  }

  private fun transformDirectory(inputDir: File, outputDir: File) {
    val inputDirPath = inputDir.absolutePath
    val outputDirPath = outputDir.absolutePath
    if (inputDir.isDirectory) {
      var fileList = com.android.utils.FileUtils.getAllFiles(inputDir)
      for (file in fileList) {
        val filePath = file.absolutePath
        val outputFile = File(filePath.replace(inputDirPath, outputDirPath))
        writeClassToFile(file, outputFile, inputDirPath)
      }
    }
  }

  private fun writeClassToFile(inputFile: File, outputFile: File, inputDir: String) {
    var srcDir = inputDir
    if (!srcDir.endsWith(File.separator)) {
      srcDir += File.separator
    }

    var className = inputFile.absolutePath.replace(srcDir, "").replace("/", ".")
    if (isTargetClass(className)) {
      println("target class=$className  outputFile=${outputFile.absolutePath}")
      val inputStream: InputStream = FileInputStream(inputFile)
      val bytes: ByteArray = targetClassToByteArray(inputStream)
      val fos = FileOutputStream(outputFile)
      fos.write(bytes)
      fos.close()
      inputStream.close()
    } else {
      FileUtils.copyFile(inputFile, outputFile)
    }
  }

  private fun targetClassToByteArray(inputStream: InputStream): ByteArray {
    //创建ClassReader，传入class字节码的输入流
    var classReader = ClassReader(inputStream)
    //创建ClassWriter，绑定classReader
    var classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
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