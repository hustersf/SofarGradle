package com.sofar.plugin.trycatch

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class TryCatchPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    if (project.plugins.hasPlugin(AppPlugin::class.java)) {
      val tryCatchExtension = project.extensions.create("trycatch", TryCatchExtension::class.java)
      var appExtension = project.extensions.findByType(AppExtension::class.java)
      appExtension?.registerTransform(TryCatchTransform(project, tryCatchExtension))
    }
  }

}