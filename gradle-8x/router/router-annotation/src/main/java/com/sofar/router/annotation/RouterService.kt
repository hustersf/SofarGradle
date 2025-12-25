package com.sofar.router.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY) // 对应 Java 的 CLASS 级别
annotation class RouterService(
  /**
   * 指定接口类型
   */
  val interfaces: KClass<*>,

  /**
   * 获取 Service 实例时，是否是单例
   */
  val singleton: Boolean = false
)