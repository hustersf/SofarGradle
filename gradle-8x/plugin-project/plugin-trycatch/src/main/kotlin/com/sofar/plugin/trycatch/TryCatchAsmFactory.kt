package com.sofar.plugin.trycatch

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.ClassVisitor

abstract class TryCatchAsmFactory : AsmClassVisitorFactory<TryCatchParams> {
  override fun createClassVisitor(
    classContext: ClassContext,
    nextClassVisitor: ClassVisitor
  ): ClassVisitor {
    val className = classContext.currentClassData.className
    // 获取当前类需要插桩的方法列表
    val methodsToInstrument = parameters.get().targetClassMethod.get()[className]
    println("TryCatchPlugin > 正在修改类: $className")
    return TryCatchClassVisitor(nextClassVisitor, methodsToInstrument, parameters.get())
  }

  override fun isInstrumentable(classData: ClassData): Boolean {
    val isTarget = parameters.get().targetClassMethod.get().containsKey(classData.className)
    // 只有匹配成功时才打印，避免由于全局扫描产生过多无意义日志
    if (isTarget) {
      println("TryCatchPlugin > 命中目标类: ${classData.className}")
    }
    return isTarget
  }
}