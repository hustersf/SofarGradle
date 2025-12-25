package com.sofar.plugin.demo

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.Plugin
import org.gradle.api.Project

class MyArtifactPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

    androidComponents.onVariants { variant ->
      // 1. 注册我们的生成任务
      val taskProvider = project.tasks.register(
        "${variant.name}GenerateMyClass",
        GenerateClassTask::class.java
      )

      // 2. 使用 ScopedArtifact 挂载
      // ScopedArtifacts.Scope.PROJECT 表示生成的类仅属于当前模块
      // 如果你想处理包含依赖库的所有类，可以使用 ALL
      variant.artifacts
        .forScope(ScopedArtifacts.Scope.PROJECT) // 作用域：当前项目
        .use(taskProvider)
        .toAppend(ScopedArtifact.CLASSES, GenerateClassTask::outputDir)
    }
  }
}