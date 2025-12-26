package com.sofar.plugin.trycatch

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class TryCatchClassVisitor(
  nextVisitor: ClassVisitor,
  private val methods: List<String>?,
  private val params: TryCatchParams
) : ClassVisitor(Opcodes.ASM9, nextVisitor) {

  override fun visitMethod(
    access: Int, name: String, descriptor: String,
    signature: String?, exceptions: Array<out String>?
  ): MethodVisitor {
    val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
    // 如果方法名在配置列表中，则应用 TryCatch 适配器
    if (methods?.contains(name) == true) {
      println("TryCatchPlugin >> 正在插桩方法: $name ($descriptor)")
      return TryCatchMethodAdapter(api, mv, access, name, descriptor, params)
    }
    return mv
  }
}