package com.sofar.transform.processor

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformInvocation
import com.google.common.collect.FluentIterable
import com.google.common.io.Files
import com.sofar.transform.FileEntity
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FileProcessor(private val action: ProcessAction) {

  fun processDirectoryInput(
    transformInvocation: TransformInvocation,
    directoryInput: DirectoryInput,
  ) {
    var outputDir = transformInvocation.outputProvider.getContentLocation(
      directoryInput.name,
      directoryInput.contentTypes,
      directoryInput.scopes,
      Format.DIRECTORY
    )
    FileUtils.forceMkdir(outputDir)

    if (transformInvocation.isIncremental) {
      var inputDirPath = directoryInput.file.absolutePath
      var outputDirPath = outputDir.absolutePath
      val fileStatusMap = directoryInput.changedFiles
      fileStatusMap.forEach { changedFile ->
        val status: Status = changedFile.value
        val inputFile: File = changedFile.key
        val outputFilePath = inputFile.absolutePath.replace(inputDirPath, outputDirPath)
        val outputFile = File(outputFilePath)
        when (status) {
          Status.NOTCHANGED -> {
          }
          Status.REMOVED -> if (outputFile.exists()) {
            outputFile.delete()
          }
          Status.ADDED, Status.CHANGED -> {
            try {
              FileUtils.touch(outputFile)
            } catch (e: IOException) {
              //maybe mkdirs fail for some strange reason, try again.
              Files.createParentDirs(outputFile)
            }
            transformSingleFile(inputFile, outputFile, inputDirPath)
          }
        }
      }
    } else {
      transformDirectory(directoryInput.file, outputDir)
    }
  }

  private fun transformDirectory(inputDir: File, outputDir: File) {
    val inputDirPath = inputDir.absolutePath
    val outputDirPath = outputDir.absolutePath
    if (inputDir.isDirectory) {
      var fileList = FluentIterable.from<File>(Files.fileTraverser().depthFirstPreOrder(inputDir))
        .filter(Files.isFile())
      for (file in fileList) {
        val filePath = file.absolutePath
        val outputFile = File(filePath.replace(inputDirPath, outputDirPath))
        transformSingleFile(file, outputFile, inputDirPath)
      }
    }
  }

  private fun transformSingleFile(inputFile: File, outputFile: File, inputDir: String) {
    var filePath = inputFile.absolutePath.replace(inputDir, "")
    if (filePath.startsWith("/")) {
      filePath = filePath.substring(1)
    }
    val fileEntity = FileEntity(filePath, inputDir, false)
    if (!outputFile.parentFile.exists()) {
      outputFile.parentFile.mkdirs()
    }
    val inputStream = FileInputStream(inputFile)
    val outputStream = FileOutputStream(outputFile)
    if (!action.processFile(fileEntity, inputStream, outputStream)) {
      FileUtils.copyFile(inputFile, outputFile)
    }
    inputStream.close()
    outputStream.close()
  }
}