package com.sofar.plugin.demo

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

open class DemoClassVisitor(nextVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM9, nextVisitor) {
}