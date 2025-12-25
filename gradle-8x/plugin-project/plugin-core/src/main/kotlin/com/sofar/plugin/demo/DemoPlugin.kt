package com.sofar.plugin.demo

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project


class DemoPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    // 获取 AndroidComponents 扩展
    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

    // 在变体创建时挂钩
    androidComponents.onVariants { variant ->
      // 注册插桩工厂
      variant.instrumentation.transformClassesWith(
        DemoAsmClassVisitorFactory::class.java,
        InstrumentationScope.ALL // ALL 表示包含第三方库，PROJECT 仅限当前模块
      ) {}

      // 设置 ASM 帧计算模式（通常选这个，虽然慢一点但最稳）
      variant.instrumentation.setAsmFramesComputationMode(
        FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
      )
    }
  }
}