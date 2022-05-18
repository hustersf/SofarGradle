package com.sofar.transform

import java.io.File

/**
 * @see name 文件名，例如：TextView.class 或 ic_launcher.png
 * @see relativePath 文件相对路径，例如：androidx/lifecycle/extensions/R$dimen.class
 * @see fromPath: String  文件来源，例如：/Users/xxx/Android/xxx/app/build/intermediates/javac/googleDebug/classes
 * @see isJarInput: Boolean  区别 dirInput和jarInput
 */
data class FileEntity(
  val relativePath: String,
  val fromPath: String,
  val isJarInput: Boolean,
) {
  val name by lazy { File(relativePath).name }
}
