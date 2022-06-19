package com.sofar.router.compiler;

import java.io.IOException;
import java.io.Writer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public abstract class BaseProcessor extends AbstractProcessor {

  protected Filer filer;
  protected Elements elementUtils;
  protected Types typeUtils;
  protected Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    filer = processingEnv.getFiler();
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    messager = processingEnv.getMessager();
  }

  protected void warn(String msg) {
    messager.printMessage(Diagnostic.Kind.WARNING, msg);
  }

  protected void note(String msg) {
    messager.printMessage(Diagnostic.Kind.NOTE, msg);
  }

  protected void error(String msg) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg);
  }

  /**
   * 从字符串获取ClassName对象
   */
  public ClassName className(String className) {
    return ClassName.get(typeElement(className));
  }

  /**
   * 从字符串获取TypeElement对象
   */
  public TypeElement typeElement(String className) {
    return elementUtils.getTypeElement(className);
  }

  protected void writeClass(String pkg, String name, TypeSpec type) {
    try {
      Writer writer = filer.createSourceFile(pkg + "." + name).openWriter();
      JavaFile.builder(pkg, type).build().writeTo(writer);
      writer.flush();
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
