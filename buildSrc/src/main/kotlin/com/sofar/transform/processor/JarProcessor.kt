package com.sofar.transform.processor

import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformInvocation
import com.sofar.transform.FileEntity
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class JarProcessor(private val action: ProcessAction) {

  fun processJarInput(
    transformInvocation: TransformInvocation,
    jarInput: JarInput,
  ) {

    var status = jarInput.status
    var outputJar = transformInvocation.outputProvider.getContentLocation(
      jarInput.file.absolutePath, jarInput.contentTypes,
      jarInput.scopes, Format.JAR
    )

    if (transformInvocation.isIncremental) {
      when (status) {
        Status.NOTCHANGED -> {
        }
        Status.REMOVED -> if (outputJar.exists()) {
          FileUtils.forceDelete(outputJar)
        }
        Status.ADDED, Status.CHANGED -> transformJar(jarInput.file, outputJar)
      }
    } else {
      transformJar(jarInput.file, outputJar)
    }
  }

  private fun transformJar(inputJar: File, outputJar: File) {
    val inputZip = ZipFile(inputJar)
    val outputZip = ZipOutputStream(
      BufferedOutputStream(
        Files.newOutputStream(outputJar.toPath())
      )
    )
    val inEntries = inputZip.entries()
    while (inEntries.hasMoreElements()) {
      val entry = inEntries.nextElement()
      val inputStream: InputStream = BufferedInputStream(inputZip.getInputStream(entry))
      val outEntry = ZipEntry(entry.name)
      val fileEntity = FileEntity(entry.name, inputJar.absolutePath, true)
      val outputStream = ByteArrayOutputStream()
      var newEntryContent: ByteArray =
        if (action.processFile(fileEntity, inputStream, outputStream)) {
          outputStream.toByteArray()
        } else {
          IOUtils.toByteArray(inputStream)
        }
      inputStream.close()
      outputStream.close()

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

}