package com.sofar.router

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class RouterVisitor(
  var addClasses: MutableSet<String>,
  var deleteClasses: MutableSet<String>,
  classWriter: ClassWriter,
) :
  ClassVisitor(Opcodes.ASM7, classWriter) {

  override fun visit(
    version: Int,
    access: Int,
    name: String?,
    signature: String?,
    superName: String?,
    interfaces: Array<out String>?,
  ) {
    super.visit(version, access, name, signature, superName, interfaces)
  }

  override fun visitMethod(
    access: Int,
    name: String?,
    descriptor: String?,
    signature: String?,
    exceptions: Array<out String>?,
  ): MethodVisitor {
    var methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
    if (Const.INIT_METHOD == name) {
      return RouterMethodVisitor(addClasses, deleteClasses, Opcodes.ASM7, methodVisitor)
    }
    return methodVisitor
  }

  class RouterMethodVisitor(
    private val addClasses: MutableSet<String>,
    private val deleteClasses: MutableSet<String>,
    api: Int,
    methodVisitor: MethodVisitor,
  ) : MethodVisitor(api, methodVisitor) {

    override fun visitMethodInsn(
      opcode: Int,
      owner: String?,
      name: String?,
      descriptor: String?,
      isInterface: Boolean,
    ) {
      if (!deleteClasses.contains(owner)) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
      }
    }

    override fun visitCode() {
      super.visitCode()
      addClasses.forEach {
        var owner = it.replace(".", "/")
        println("visitCode $owner")
        deleteClasses.add(owner)
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner, "init", "()V", false)
      }
    }
  }
}