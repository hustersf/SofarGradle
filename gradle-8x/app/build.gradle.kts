plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  id("com.sofar.plugin.demo.artifact")
  id("com.sofar.plugin.router")
  id("com.sofar.plugin.trycatch")
}

trycatch {
  targetClassMethod.set(
    mapOf(
      "androidx.work.impl.utils.ForceStopRunnable" to listOf("run"),
      "com.sofar.gradle.v8.trycatch.CrashA" to listOf("run", "run2", "run3"),
      "com.sofar.gradle.v8.trycatch.CrashB" to listOf("run", "run2", "run3")
    )
  )

  exceptionHandler.set(mapOf(
    "com.sofar.gradle.v8.trycatch.TryCatchExceptionUtil" to "defaultExceptionHandler"
  ))
}

android {
  namespace = "com.sofar.gradle.v8"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.sofar.gradle.v8"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
}

dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)

  implementation(project(":feature:account"))
  implementation(project(":feature:share"))
  implementation(project(":feature-api:account-api"))
  implementation(project(":feature-api:share-api"))

  implementation(project(":router:router-api"))
  implementation(project(":router:router-annotation"))
}