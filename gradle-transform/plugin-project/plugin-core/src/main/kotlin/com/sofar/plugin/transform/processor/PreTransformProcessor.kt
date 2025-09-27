package com.sofar.plugin.transform.processor

import com.android.build.api.transform.Status
import com.sofar.plugin.transform.FileEntity
import org.gradle.api.Project
import java.io.InputStream
import java.io.OutputStream

class PreTransformProcessor(
  private val name: String,
  private val project: Project,
  private val action: PreProcessAction,
) : TransformProcessor(name, project, action = object : ProcessAction {
  override fun processFile(
    status: Status,
    fileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ): Boolean {
    action.preProcessFile(status, fileEntity, input, output)
    return false
  }
})