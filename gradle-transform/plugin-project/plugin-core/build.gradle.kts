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

  implementation(libs.commons.io)
  implementation(libs.guava)
  implementation(libs.asm)
  implementation(libs.asm.commons)
}

gradlePlugin {
  plugins {
    create("DemoPlugin") {
      id = "com.sofar.plugin.demo"
      implementationClass = "com.sofar.plugin.demo.DemoPlugin"
    }
  }
}