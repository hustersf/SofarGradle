package com.sofar.plugin.router

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class RouterPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

    androidComponents.onVariants { variant ->
      val transformTask = project.tasks.register<RouterTransformTask>("${variant.name}RouterTransform")

      // 核心修正：使用 toTransform 代替 toGet
      // 这种方式会将 Task 插入到编译流水线的中间，绝对不会产生循环依赖
      variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
        .use(transformTask)
        .toTransform(
          ScopedArtifact.CLASSES,
          RouterTransformTask::allJars,
          RouterTransformTask::allDirs,
          RouterTransformTask::outputJar
        )
    }
  }
}