package com.sofar.toast

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class ToastPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    if (project.plugins.hasPlugin(AppPlugin::class.java)) {
      var appExtension = project.extensions.findByType(AppExtension::class.java)
      appExtension?.registerTransform(ToastTransform(project))
    }
  }

}