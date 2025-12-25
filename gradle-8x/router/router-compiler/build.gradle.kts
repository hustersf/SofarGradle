plugins {
  kotlin("jvm")
  `java-library`
  alias(libs.plugins.kotlin.kapt)
  alias(libs.plugins.google.ksp)
}

dependencies {
  implementation(project(":router:router-annotation"))
  implementation(libs.javapoet)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  // ksp 开发
  implementation(libs.ksp.api)
  compileOnly(libs.auto.service.annotations)
  kapt(libs.auto.service) //kapt版本
//  ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0") //ksp版本
}