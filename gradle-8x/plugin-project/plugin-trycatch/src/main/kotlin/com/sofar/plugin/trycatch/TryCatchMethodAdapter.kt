package com.sofar.plugin.trycatch

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

class TryCatchMethodAdapter(
  api: Int, mv: MethodVisitor, access: Int, name: String, descriptor: String,
  private val params: TryCatchParams
) : AdviceAdapter(api, mv, access, name, descriptor) {

  private val startLabel = Label()
  private val endLabel = Label()
  private val handlerLabel = Label()

  override fun onMethodEnter() {
    // 在方法开始位置插入 try 开始标记
    mv.visitLabel(startLabel)
  }

  override fun visitMaxs(maxStack: Int, maxLocals: Int) {
    // 在方法末尾（visitMaxs 调用前）闭合 try-catch
    mv.visitLabel(endLabel)

    // 注册 try-catch 块：范围从 start 到 end，异常跳转到 handlerLabel
    mv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "java/lang/Exception")

    // --- Catch 块逻辑开始 ---
    mv.visitLabel(handlerLabel)

    // 1. 调用自定义 ExceptionHandler
    val handlerMap = params.exceptionHandler.get()
    if (handlerMap.isNotEmpty()) {
      val handlerClass = handlerMap.keys.first().replace(".", "/")
      val handlerMethod = handlerMap.values.first()

      // 此时栈顶是异常对象 Exception，复制一份作为参数传递给静态方法
      mv.visitInsn(DUP)
      mv.visitMethodInsn(
        INVOKESTATIC,
        handlerClass,
        handlerMethod,
        "(Ljava/lang/Exception;)V",
        false
      )
    }

    // 2. 根据方法返回类型，压入默认返回值并 Return
    val returnType = Type.getReturnType(methodDesc)
    when (returnType.sort) {
      Type.VOID -> mv.visitInsn(RETURN)
      Type.BOOLEAN, Type.INT, Type.SHORT, Type.CHAR, Type.BYTE -> {
        mv.visitInsn(ICONST_0)
        mv.visitInsn(IRETURN)
      }

      Type.FLOAT -> {
        mv.visitInsn(FCONST_0)
        mv.visitInsn(FRETURN)
      }

      Type.LONG -> {
        mv.visitInsn(LCONST_0)
        mv.visitInsn(LRETURN)
      }

      Type.DOUBLE -> {
        mv.visitInsn(DCONST_0)
        mv.visitInsn(DRETURN)
      }

      else -> { // 对象类型返回 null
        mv.visitInsn(ACONST_NULL)
        mv.visitInsn(ARETURN)
      }
    }

    super.visitMaxs(maxStack, maxLocals)
  }
}