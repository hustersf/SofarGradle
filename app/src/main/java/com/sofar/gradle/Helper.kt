package com.sofar.gradle

fun String.appendCapitalized(word1: String, word2: String
): String {
  val sb = StringBuilder(length + word1.length + word2.length)
  sb.append(this)
  sb.append(word1)
  sb.append(word2)
  return sb.toString()
}



