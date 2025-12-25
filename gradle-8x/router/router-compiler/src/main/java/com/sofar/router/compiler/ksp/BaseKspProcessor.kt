package com.sofar.router.compiler.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import kotlin.reflect.KProperty1

abstract class BaseKspProcessor(
  protected val codeGenerator: CodeGenerator,
  protected val logger: KSPLogger
) : SymbolProcessor {

  // 1. 封装日志，支持关联源码位置
  protected fun note(message: String, symbol: KSNode? = null) {
    logger.info(message, symbol)
  }

  protected fun warn(message: String, symbol: KSNode? = null) {
    logger.warn(message, symbol)
  }

  protected fun error(message: String, symbol: KSNode? = null) {
    logger.error(message, symbol)
  }

  // 2. 封装注解获取逻辑
  protected fun KSAnnotated.getAnnotation(clazz: kotlin.reflect.KClass<out Annotation>): KSAnnotation? {
    return annotations.find {
      it.shortName.asString() == clazz.simpleName &&
          it.annotationType.resolve().declaration.qualifiedName?.asString() == clazz.qualifiedName
    }
  }

  // 3. 封装参数获取 (类型安全)
  protected fun <T> KSAnnotation.getArg(prop: KProperty1<out Annotation, T>): T? {
    @Suppress("UNCHECKED_CAST")
    return arguments.find { it.name?.asString() == prop.name }?.value as? T
  }

  // 4. 封装 kotlin 文件生成
  protected fun createKotlinFile(packageName: String, fileName: String, content: String, originatingFile: KSFile?) {
    val dependencies = if (originatingFile != null) Dependencies(false, originatingFile) else Dependencies(true)
    codeGenerator.createNewFile(dependencies, packageName, fileName, "kt").use { output ->
      output.write(content.toByteArray())
    }
  }
}