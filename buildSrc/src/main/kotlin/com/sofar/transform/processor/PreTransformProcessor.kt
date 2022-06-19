package com.sofar.transform.processor

import com.sofar.transform.FileEntity
import org.gradle.api.Project
import java.io.InputStream
import java.io.OutputStream

class PreTransformProcessor(
  private val name: String,
  private val project: Project,
  private val action: PreProcessAction,
) : TransformProcessor(name, project, action = object : ProcessAction {
  override fun processFile(
    inputFileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ): Boolean {
    action.preProcessFile(inputFileEntity, input, output)
    return false
  }
})