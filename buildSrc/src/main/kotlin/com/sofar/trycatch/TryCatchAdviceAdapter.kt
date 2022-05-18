package com.sofar.trycatch

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import java.util.function.Consumer

class TryCatchAdviceAdapter(
  extension: TryCatchExtension,
  api: Int,
  methodVisitor: MethodVisitor,
  access: Int,
  name: String?,
  descriptor: String
) : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

  private var exceptionHandleClass: String? = null
  private var exceptionHandleMethod: String? = null

  private val startLabel: Label = Label()   // 开头
  private val endLabel: Label = Label()// 结尾
  private val handlerLabel: Label = Label() // 处理
  private val returnLabel: Label = Label()// 返回

  init {
    val exceptionHandler: Map<String, String>? = extension.exceptionHandler
    if (exceptionHandler != null && exceptionHandler.isNotEmpty()) {
      exceptionHandler.entries.forEach(Consumer { entry: Map.Entry<String, String> ->
        exceptionHandleClass = entry.key.replace(".", "/")
        exceptionHandleMethod = entry.value
      })
    }
  }


  override fun onMethodEnter() {
    super.onMethodEnter()
    println("TryCatchAdviceAdapter onMethodEnter")
    // 1标志：try块开始位置
    mv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "java/lang/Exception")
    mv.visitLabel(startLabel)
  }

  override fun onMethodExit(opcode: Int) {
    super.onMethodExit(opcode)
    println("TryCatchAdviceAdapter onMethodExit")
  }

  override fun visitMaxs(maxStack: Int, maxLocals: Int) {
    println("TryCatchAdviceAdapter visitMaxs")
    // 2标志：try块结束
    mv.visitLabel(endLabel)

    // 3标志：catch块开始位置
    mv.visitLabel(handlerLabel)
    //mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, arrayOf<Any>("java/lang/Exception"))
    // 0代表this， 1 第一个参数，异常信息保存到局部变量
    mv.visitVarInsn(ASTORE, 1)
    // 从local variables取出局部变量到operand stack
    mv.visitVarInsn(ALOAD, 1)
    // 自定义异常处理
    if (exceptionHandleClass != null && exceptionHandleMethod != null) {
      mv.visitMethodInsn(
        INVOKESTATIC,
        exceptionHandleClass,
        exceptionHandleMethod,
        "(Ljava/lang/Exception;)V",
        false
      )
    } else {
      // 没提供处理类就直接抛出异常
      // mv.visitInsn(ATHROW)
    }

    // 顺序向下执行，可以不要GOTO
    //mv.visitJumpInsn(Opcodes.GOTO, returnLabel);
    // 返回label
    // mv.visitLabel(returnLabel);

    // catch结束，方法返回默认值收工
    val defaultVo: Pair<Int, Int> = getDefaultByDesc(methodDesc)
    val value: Int = defaultVo.first
    val opcode: Int = defaultVo.second
    if (value >= 0) {
      mv.visitInsn(value)
    }
    mv.visitInsn(opcode)
    super.visitMaxs(maxStack, maxLocals)
  }

  private fun getDefaultByDesc(methodDesc: String): Pair<Int, Int> {
    var pair: Pair<Int, Int>
    var value = -1
    var opcode = -1
    if (methodDesc.endsWith("[Z") ||
      methodDesc.endsWith("[I") ||
      methodDesc.endsWith("[S") ||
      methodDesc.endsWith("[B") ||
      methodDesc.endsWith("[C")
    ) {
      value = Opcodes.ACONST_NULL
      opcode = Opcodes.ARETURN
    } else if (methodDesc.endsWith("Z") ||
      methodDesc.endsWith("I") ||
      methodDesc.endsWith("S") ||
      methodDesc.endsWith("B") ||
      methodDesc.endsWith("C")
    ) {
      value = Opcodes.ICONST_0
      opcode = Opcodes.IRETURN
    } else if (methodDesc.endsWith("J")) {
      value = Opcodes.LCONST_0
      opcode = Opcodes.LRETURN
    } else if (methodDesc.endsWith("F")) {
      value = Opcodes.FCONST_0
      opcode = Opcodes.FRETURN
    } else if (methodDesc.endsWith("D")) {
      value = Opcodes.DCONST_0
      opcode = Opcodes.DRETURN
    } else if (methodDesc.endsWith("V")) {
      opcode = Opcodes.RETURN
    } else {
      value = Opcodes.ACONST_NULL
      opcode = Opcodes.ARETURN
    }
    pair = Pair(value, opcode)
    return pair
  }

}