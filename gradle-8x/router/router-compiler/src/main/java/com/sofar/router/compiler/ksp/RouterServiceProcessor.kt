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
    const val GENERATED_PACKAGE = "com.sofar.router.generated" // 汇总类统一包名
  }

  // 记录生成代码所需的元数据
  private data class RouterMetadata(
    val impl: ClassName,
    val interf: ClassName,
    val singleton: Boolean
  )

  private val routerMetadataList = mutableListOf<RouterMetadata>()
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

      // 3.记录有哪些注解类
      interfaceType?.let {
        routerMetadataList.add(
          RouterMetadata(classSymbol.toClassName(), it.toClassName(), singleton)
        )
      }
    }
    return emptyList()
  }

  override fun finish() {
    super.finish()
    warn("KSP finish class size:${routerMetadataList.size}")

    if (routerMetadataList.isNotEmpty()) {
      // 计算模块指纹：包名 + 类名 + 路径，通过排序保证 Hash 稳定性
      val moduleFingerprint = originatingFiles.flatMap { file ->
        file.declarations.filterIsInstance<KSClassDeclaration>()
          .map { "${it.qualifiedName?.asString() ?: ""}:${file.filePath}" }
      }.filter { it.isNotEmpty() }.distinct().sorted().joinToString().hashCode()

      val hexHash = Integer.toHexString(moduleFingerprint).uppercase()
      val targetClassName = "Router_$hexHash$SUFFIX"

      // 4.在 finish 中计算 Hash 并生成唯一的汇总类
      generateCombinedFile(targetClassName)

      // 5.配置文件记录这个生产的汇总类,后续 AGP 插件会使用
      generateConfigFile("$GENERATED_PACKAGE.$targetClassName")
    }
  }

  private fun generateCombinedFile(targetClassName: String) {
    // 定义目标 ServiceLoader 类的包名类名，用于后续生成代码时的 Import 引用
    val serviceLoaderClass = ClassName("com.sofar.router.service", "ServiceLoader")

    // 构建 load 函数：遍历收集到的元数据，生成 ServiceLoader.put(接口, 实现类, 是否单例) 的调用代码
    val loadFunc = FunSpec.builder(METHOD)
      .addAnnotation(JvmStatic::class)
      .addModifiers(KModifier.PUBLIC)
      .apply {
        routerMetadataList.forEach { data ->
          addCode(
            // %T 自动处理类引用和 Import，%L 处理布尔值字面量
            "%T.put(%T::class.java, %T::class.java, %L)\n",
            serviceLoaderClass, data.interf, data.impl, data.singleton
          )
        }
      }.build()

    // 构建类结构：创建一个类，并将上述 load 函数放入其伴生对象（companion object）中
    val classSpec = TypeSpec.classBuilder(targetClassName)
      .addModifiers(KModifier.PUBLIC)
      .addType(TypeSpec.companionObjectBuilder().addFunction(loadFunc).build())
      .build()

    // 生成文件并写入：指定包名和文件名，通过 KSP 的 codeGenerator 物理输出文件，并关联源文件以支持增量编译
    FileSpec.builder(GENERATED_PACKAGE, targetClassName)
      .addType(classSpec)
      .build()
      .writeTo(
        codeGenerator,
        Dependencies(aggregating = true, sources = originatingFiles.toTypedArray())
      )

    // 打印编译日志，输出生成的类名和处理的条目数量
    warn("KSP RouterServiceProcessor generateCombinedFile: targetClassName = $targetClassName, size = ${routerMetadataList.size}")
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