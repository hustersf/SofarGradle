package com.sofar.plugin.trycatch

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class TryCatchVisitor(var extension: TryCatchExtension, classWriter: ClassWriter) :
  ClassVisitor(Opcodes.ASM7, classWriter) {

  private var className: String? = null
  private var methodNames: List<String>? = null

  override fun visit(
    version: Int,
    access: Int,
    name: String?,
    signature: String?,
    superName: String?,
    interfaces: Array<out String>?
  ) {
    println("try catch visit name=$name")
    className = name?.replace("/", ".")
    methodNames = extension.targetClassMethod[className]
    super.visit(version, access, name, signature, superName, interfaces)
  }

  override fun visitMethod(
    access: Int,
    name: String?,
    descriptor: String?,
    signature: String?,
    exceptions: Array<out String>?
  ): MethodVisitor {
    println("try catch visitMethod name=$name")
    var methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
    if (methodNames != null && methodNames!!.contains(name) && descriptor != null) {
      return TryCatchAdviceAdapter(extension, Opcodes.ASM7, methodVisitor, access, name, descriptor)
    }
    return methodVisitor
  }

}