package com.sofar.plugin.trycatch

import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 指定类中指定的方法包一层 try-catch
 * 场景: 三方 SDK 崩溃无法处理, 可以注入 try-catch 字节码, 确保程序不崩溃
 */
class TryCatchPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    // 创建 Extension
    val extension = project.extensions.create("trycatch", TryCatchExtension::class.java)

    // 获取 AGP 8.x 的组件扩展
    val androidComponents = project.extensions.findByType(AndroidComponentsExtension::class.java)

    androidComponents?.onVariants { variant ->
      // 注册 ASM 转换工厂
      variant.instrumentation.transformClassesWith(
        TryCatchAsmFactory::class.java,
        InstrumentationScope.ALL // 关键：ALL 范围包含项目代码和所有 Jar 依赖
      ) { params ->
        // 将配置属性关联到 Instrumentation 参数
        params.targetClassMethod.set(extension.targetClassMethod)
        params.exceptionHandler.set(extension.exceptionHandler)
      }

      // 设定 ASM 帧计算模式（自动计算栈帧，防止插入代码后校验失败）
      variant.instrumentation.setAsmFramesComputationMode(
        com.android.build.api.instrumentation.FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
      )
    }
  }
}