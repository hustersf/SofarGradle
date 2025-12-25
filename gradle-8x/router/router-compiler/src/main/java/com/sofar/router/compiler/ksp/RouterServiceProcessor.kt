package com.sofar.router.compiler.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.sofar.router.annotation.RouterService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class RouterServiceProcessor(
  codeGenerator: CodeGenerator,
  logger: KSPLogger,
) : BaseKspProcessor(codeGenerator, logger) {

  companion object {
    const val SERVICE_FILE_NAME = "META-INF/services/router/service_info.list"
    const val SUFFIX = "__Router__"
    const val METHOD = "load"
  }

  // 用于收集生成的类名，最后统一写入配置文件
  private val classSet = mutableSetOf<String>()

  // 关键：用于收集所有相关的源文件引用，实现精确增量
  private val originatingFiles = mutableSetOf<KSFile>()

  override fun process(resolver: Resolver): List<KSAnnotated> {
    warn("KSP RouterServiceProcessor 开始工作")

    // 1.获取所有标记了 @RouterService 的类
    val name = RouterService::class.qualifiedName!!
    val symbols = resolver.getSymbolsWithAnnotation(name)
      .filterIsInstance<KSClassDeclaration>()
    for (classSymbol in symbols) {
      // 收集源文件：告知 KSP 这些文件参与了逻辑，后续增量时需重新扫描它们
      classSymbol.containingFile?.let { originatingFiles.add(it) }
      // 2.获取注解参数
      val annotation = classSymbol.getAnnotation(RouterService::class) ?: continue
      val singleton = annotation.getArg(RouterService::singleton) ?: false
      val interfaceType = annotation.getArg(RouterService::interfaces) as? KSType

      // 3.生成 Kotlin 辅助类文件
      interfaceType?.let {
        generateKotlinFile(classSymbol, it, singleton)
      }

    }
    return emptyList()
  }

  private fun generateKotlinFile(
    classDeclaration: KSClassDeclaration,
    interfaceType: KSType,
    singleton: Boolean,
  ) {
    // 1. 动态提取类信息
    val implClassName = classDeclaration.toClassName()      // 自动获取包名和类名
    val interfaceClassName = interfaceType.toClassName()   // 自动获取接口名
    val targetClassName = "${implClassName.simpleName}$SUFFIX"

    // 2. 固定引用
    val serviceLoaderClass = ClassName("com.sofar.router.service", "ServiceLoader")

    // 3. 构建 init 函数 (带 @JvmStatic 确保 Java 可直接调用)
    val initFunc = FunSpec.builder(METHOD)
      .addAnnotation(JvmStatic::class)
      .addModifiers(KModifier.PUBLIC)
      .addCode(
        // %T 会自动处理 Import 并加上 ::class.java
        // %L 处理布尔字面量
        "%T.put(%T::class.java, %T::class.java, %L)",
        serviceLoaderClass,
        interfaceClassName,
        implClassName,
        singleton
      )
      .build()

    // 4. 构建 Companion Object (容纳静态方法)
    val companionObj = TypeSpec.companionObjectBuilder()
      .addFunction(initFunc)
      .build()

    // 5. 构建主类
    val classSpec = TypeSpec.classBuilder(targetClassName)
      .addModifiers(KModifier.PUBLIC)
      .addType(companionObj)
      .build()

    // 6. 生成文件并自动处理所有 import
    val fileSpec = FileSpec.builder(implClassName.packageName, targetClassName)
      .addType(classSpec)
      .build()

    // 7. 写入 KSP 生成目录，并配置增量编译依赖
    fileSpec.writeTo(
      codeGenerator = codeGenerator,
      dependencies = Dependencies(
        aggregating = false,
        classDeclaration.containingFile!! // 动态关联原始文件
      )
    )
    classSet.add("${implClassName.packageName}.$targetClassName")
    warn("KSP RouterServiceProcessor generateKotlinFile: targetClassName = ${targetClassName}")
  }

  override fun finish() {
    super.finish()
    warn("KSP finish class size:${classSet.size}")
    if (classSet.isNotEmpty()) {
      generateConfigFile(classSet.joinToString("\n"))
    }
  }

  private fun generateConfigFile(content: String) {
    try {
      codeGenerator.createNewFile(
        dependencies = Dependencies(
          aggregating = true,
          sources = originatingFiles.toTypedArray()
        ),
        packageName = "",
        fileName = SERVICE_FILE_NAME,
        extensionName = ""
      ).use { it.write(content.toByteArray()) }
    } catch (e: Exception) {
      // 忽略文件已存在的异常
    }
  }

}