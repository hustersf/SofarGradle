package com.sofar.toast

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager

class ToastTransform : Transform() {

  /**
   * 名字，Transform 会生成一个gradle任务
   */
  override fun getName(): String {
    return "toast"
  }

  /**
   * 输入文件类型
   */
  override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
    return TransformManager.CONTENT_CLASS
  }

  /**
   * 输入文件范围
   */
  override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
    return TransformManager.SCOPE_FULL_PROJECT
  }

  /**
   * 是否支持增量编译
   */
  override fun isIncremental(): Boolean {
    return true
  }

  /**
   * TransformInput
   * 输入文件集合，包含 DirectoryInput 集合与 JarInput 集合。
   * DirectoryInput 代表源码方式参与编译的项目
   * JarInput 代表jar包方式参与编译的项目
   *
   * TransformOutputProvider
   * 用来获取输出目录，我们要将操作后的文件复制到输出目录中
   *
   * 可利用 asm 对输入的class文件进行字节码修改，然后复制到输出目录中
   */
  override fun transform(transformInvocation: TransformInvocation?) {
    super.transform(transformInvocation)
    var start = System.currentTimeMillis()
    println("ToastTransform start")
    transformInvocation?.inputs?.forEach {

      it.directoryInputs.forEach { a ->
        println(a.toString())
      }

      it.jarInputs.forEach { b ->
        println(b.toString())
      }

    }
    println("ToastTransform end cost time=${System.currentTimeMillis() - start}")
  }

}