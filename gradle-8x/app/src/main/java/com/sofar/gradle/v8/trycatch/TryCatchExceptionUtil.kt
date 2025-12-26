package com.sofar.gradle.v8.trycatch

import android.widget.Toast
import com.sofar.gradle.v8.DemoApplication

class TryCatchExceptionUtil {
  companion object {
    /**
     * 必须加上 @JvmStatic 注解
     * 这样 Kotlin 编译器才会生成一个真正的 Java 静态方法供 ASM 调用
     */
    @JvmStatic
    fun defaultExceptionHandler(e: Exception) {
      Toast.makeText(DemoApplication.app,"try-catch 插件生效, 异常信息:${e.message}", Toast.LENGTH_SHORT).show()
    }
  }
}