package com.sofar.plugin.toast

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ToastVisitor(private val libName: String, classWriter: ClassWriter) :
  ClassVisitor(Opcodes.ASM7, classWriter) {

  private var className: String? = null

  override fun visit(
    version: Int,
    access: Int,
    name: String?,
    signature: String?,
    superName: String?,
    interfaces: Array<out String>?,
  ) {
    super.visit(version, access, name, signature, superName, interfaces)
    className = name
  }

  override fun visitMethod(
    access: Int,
    name: String?,
    descriptor: String?,
    signature: String?,
    exceptions: Array<out String>?,
  ): MethodVisitor {
    var mv: MethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
    if (className.equals("com/sofar/gradle/SafeToast")) {
      return mv
    }
    return CommonMethodVisitor(mv, name)
  }

  private inner class CommonMethodVisitor(mv: MethodVisitor, val methodName: String?) :
    MethodVisitor(Opcodes.ASM6, mv) {

    override fun visitMethodInsn(
      opcode: Int,
      owner: String?,
      name: String?,
      descriptor: String?,
      isInterface: Boolean,
    ) {
      if (opcode == Opcodes.INVOKESTATIC && "android/widget/Toast" == owner && "makeText" == name) {
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
          "com/sofar/gradle/SafeToast",
          "makeToast",
          descriptor,
          isInterface)
        println("[Toast] replace make toast call from $className.$methodName in $libName")
      } else if (opcode == Opcodes.INVOKEVIRTUAL && owner == "android/widget/Toast" && name == "show") {
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
          "com/sofar/gradle/SafeToast",
          "showToastContent",
          "(Landroid/widget/Toast;)V",
          isInterface)
        println("[Toast] replace show toast call from $className.$methodName in $libName")
      } else {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
      }
    }
  }

}