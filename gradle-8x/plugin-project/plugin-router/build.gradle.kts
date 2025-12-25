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
}

gradlePlugin {
  plugins {
    create("RouterPlugin") {
      id = "com.sofar.plugin.router"
      implementationClass = "com.sofar.plugin.router.RouterPlugin"
    }
  }
}