package com.sofar.plugin.demo

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.objectweb.asm.ClassVisitor

abstract class DemoAsmClassVisitorFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {

  // 创建具体的 ClassVisitor
  override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
    return object : DemoClassVisitor(nextClassVisitor) {}
  }

  // 过滤类：决定哪些类需要被插桩
  override fun isInstrumentable(classData: ClassData): Boolean {
    // 示例：只处理以 com.example 开头的类，排除资源类
    return classData.className.startsWith("com.example") &&
        !classData.className.endsWith(".R") &&
        !classData.className.contains(".R$")
  }
}