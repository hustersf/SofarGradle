package com.sofar.plugin.image

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest

class CheckImageRepeatPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    val hasAppPlugin = project.plugins.hasPlugin("com.android.application")
    val variants = if (hasAppPlugin) {
      (project.property("android") as AppExtension).applicationVariants
    } else {
      (project.property("android") as LibraryExtension).libraryVariants
    }

    project.afterEvaluate {
      variants.all { variant ->
        var variantName = variant.name
        var buildType = variant.buildType.name
        if (buildType.equals("Debug", true)) {
          println("$variantName:$buildType ==> 开始检查重复图片...")
          val startTs = System.currentTimeMillis()
          checkDrawableRepeat(it, variant)
          println("checkDrawableRepeat 用时: ${(System.currentTimeMillis() - startTs) * 1.0f / 1000f}s")
        }
      }
    }
  }

  private fun checkDrawableRepeat(project: Project, variant: BaseVariant) {
    val dSet = project.configurations.map {
      it.dependencies
    }
    val dependencies = mutableSetOf<ProjectDependency>()
    for (dependencySet in dSet) {
      dependencies.addAll(dependencySet.filterIsInstance<ProjectDependency>())
    }

    println("${project.group} Dependency size:  ${dependencies.size}")
    val filterMap = mutableMapOf<String, String>()

    val resFileSet = variant.sourceSets.map {
      it.resDirectories
    }.toMutableList()
    for (collection in resFileSet) {
      for (file in collection) {
        checkResFile(file, filterMap)
      }
    }

    //  Dependency:
    for (moduleDependency in dependencies) {
      if (moduleDependency.name.contains("test")) {
        continue
      }

      println("dependency name:${moduleDependency.name} group:${moduleDependency.group}")
      val dir = ":${
        moduleDependency.group?.substring(project.group.toString().length)
          ?.replace(".", ":")
      }:${moduleDependency.name}"
      println(project.rootProject.project(dir).projectDir.path)
      val resFile = File("${project.rootProject.project(dir).projectDir.path}/src/main/res")
      checkResFile(resFile, filterMap)
    }
    filterMap.clear()
  }

  private fun checkResFile(resFile: File, filterMap: MutableMap<String, String>) {
    if (resFile.exists()) {
      resFile.listFiles()?.let {
        for (file in it) {
          if (file.isDirectory && file.name.contains("dpi")) {
            file.listFiles()?.let { subFiles ->
              for (image in subFiles) {
                val md5 = generateMD5(image)
                if (filterMap.containsKey(md5)) {
                  println("重复资源应该下沉！: ${image.path}    和   ${filterMap[md5]}")
                } else {
                  filterMap[md5] = image.path
                }
              }
            }
          }
        }
      }
    }
  }

  private fun generateMD5(file: File): String {
    val digest = MessageDigest.getInstance("MD5")
    BufferedInputStream(FileInputStream(file)).apply {
      val buffer = ByteArray(8192)
      var read = 0
      while ((this.read(buffer).also { read = it }) > 0) {
        digest.update(buffer, 0, read)
      }
    }
    val md5sum = digest.digest()
    val bigInt = BigInteger(1, md5sum)

    return bigInt.toString(16).padStart(32, '0')
  }
}