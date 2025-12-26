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

  implementation(libs.asm)
  implementation(libs.asm.commons)
}

gradlePlugin {
  plugins {
    create("TryCatchPlugin") {
      id = "com.sofar.plugin.trycatch"
      implementationClass = "com.sofar.plugin.trycatch.TryCatchPlugin"
    }
  }
}