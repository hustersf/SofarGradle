package com.sofar.transform.processor

import com.android.build.api.transform.Status
import com.sofar.transform.FileEntity
import java.io.InputStream
import java.io.OutputStream

interface ProcessAction {

  fun processFile(
    status: Status,
    fileEntity: FileEntity,
    input: InputStream?,
    output: OutputStream?,
  ): Boolean

}