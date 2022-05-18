package com.sofar.transform.processor

import com.sofar.transform.FileEntity
import java.io.InputStream
import java.io.OutputStream

interface ProcessAction {

  fun processFile(
    inputFileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ): Boolean

}