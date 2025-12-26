package com.sofar.gradle.v8.trycatch

class CrashA {

  fun run() {
    1 / 0
  }

  fun run2(): Int {
    1 / 0
    return 1
  }

  fun run3(): Boolean {
    1 / 0
    return true
  }
}