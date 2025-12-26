package com.sofar.plugin.trycatch

import org.gradle.api.provider.MapProperty

interface TryCatchExtension {
  /**
   * 目标类名与方法名列表的映射
   * 格式: ["className": ["methodA", "methodB"]]
   */
  val targetClassMethod: MapProperty<String, List<String>>

  /**
   * 异常处理器的类名与方法名
   * 格式: ["HandlerClass": "StaticMethodName"]
   */
  val exceptionHandler: MapProperty<String, String>
}