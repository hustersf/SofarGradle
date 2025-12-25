package com.sofar.plugin.router

object RouterConfig {
  // KSP 生成类的固定后缀
  const val GENERATED_CLASS_SUFFIX = "__Router__"

  // 目标注入类和方法的信息
  const val TARGET_CLASS_NAME = "com.sofar.router.Router"
  const val TARGET_METHOD_NAME = "loadRouterMap"
  const val TARGET_METHOD_DESC = "()V"

  // 注入调用的方法名（xxx_Router_.load()）
  const val INJECT_METHOD_NAME = "load"
  const val INJECT_METHOD_DESC = "()V"

  const val SERVICE_FILE_NAME = "META-INF/services/router/service_info.list"
}