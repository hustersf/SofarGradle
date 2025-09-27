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
    create("SplashResReplacePlugin") {
      id = "com.sofar.plugin.image.splash"
      implementationClass = "com.sofar.plugin.image.SplashResReplacePlugin"
    }
    create("CheckImageRepeatPlugin") {
      id = "com.sofar.plugin.image.checkrepeat"
      implementationClass = "com.sofar.plugin.image.CheckImageRepeatPlugin"
    }
  }
}