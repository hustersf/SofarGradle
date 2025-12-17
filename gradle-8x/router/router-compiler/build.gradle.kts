plugins {
  `java-library`
}

dependencies {
  implementation(project(":router:router-annotation"))
  implementation("com.squareup:javapoet:1.10.0")
  implementation("com.google.auto.service:auto-service:1.0-rc4")
  annotationProcessor("com.google.auto.service:auto-service:1.0-rc4")
}