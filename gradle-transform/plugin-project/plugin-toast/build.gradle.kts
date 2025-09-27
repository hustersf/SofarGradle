plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

dependencies {
  compileOnly(libs.android.gradle.plugin)
  implementation(gradleKotlinDsl())

  implementation(project(":plugin-core"))
}

gradlePlugin {
  plugins {
    create("ToastPlugin") {
      id = "com.sofar.plugin.toast"
      implementationClass = "com.sofar.plugin.toast.ToastPlugin"
    }
  }
}