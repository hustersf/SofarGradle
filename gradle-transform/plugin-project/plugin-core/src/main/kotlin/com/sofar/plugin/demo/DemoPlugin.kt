package com.sofar.plugin.demo

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class DemoPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    if (project.plugins.hasPlugin(AppPlugin::class.java)) {
      println("--------------apply DemoPlugin----------------")
      var appExtension = project.extensions.findByType(AppExtension::class.java)
      appExtension?.registerTransform(DemoTransform(project))
    }
  }

}