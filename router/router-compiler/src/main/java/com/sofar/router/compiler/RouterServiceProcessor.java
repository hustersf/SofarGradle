package com.sofar.router.compiler;

import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;
import com.sofar.router.annotation.RouterService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.sofar.router.annotation.RouterService"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RouterServiceProcessor extends BaseProcessor {

  public static final String DEFAULT_FILE_NAME = "META-INF/services/router/service_info";
  public static final String SUFFIX = "_Router_";
  public static final String INIT_METHOD = "init";

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    note("RouterServiceProcessor 开始工作");

    HashSet<String> classSet = new HashSet<>();

    Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(RouterService.class);
    for (Element element : elements) {
      note(element.toString());

      ElementKind kind = element.getKind();
      RouterService annotation = element.getAnnotation(RouterService.class);
      boolean singleton = annotation.singleton();
      if (kind == ElementKind.CLASS) {
        TypeElement typeElement = (TypeElement) element;
        String implClsName = typeElement.getQualifiedName().toString();
        TypeMirror typeMirror = getInterface(annotation);
        String interfaceClsName = typeMirror.toString();
        note("interfaceClsName=" + interfaceClsName + " implCls=" + implClsName + " singleton=" +
          singleton);
        generateJavaFile(interfaceClsName, implClsName, singleton, classSet);
      }
    }
    if (!classSet.isEmpty()) {
      generateConfigFile(classSet);
      return true;
    }
    return false;
  }

  private static TypeMirror getInterface(RouterService service) {
    try {
      service.interfaces();
    } catch (MirroredTypesException mte) {
      return mte.getTypeMirrors().get(0);
    }
    return null;
  }

  private void generateJavaFile(String interfaceClsName, String implClsName, boolean singleton,
    HashSet<String> classSet) {
    //记录生成的类
    classSet.add(implClsName + SUFFIX);

    //类相关参数
    String pkgName = className(implClsName).packageName();
    String simpleName = className(implClsName).simpleName() + SUFFIX;
    ClassName className = ClassName.get(pkgName, simpleName);

    ClassName implCls = className(implClsName);
    ClassName interfaceCls = className(interfaceClsName);
    //这里会调用 ServiceLoader.put 方法
    ClassName serviceLoader = ClassName.get("com.sofar.router.service", "ServiceLoader");
    //生成方法
    MethodSpec methodSpec = MethodSpec.methodBuilder(INIT_METHOD)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addStatement("$T.put($T.class,$T.class,$L)", serviceLoader, interfaceCls, implCls, singleton)
      .build();
    //生成类
    TypeSpec typeSpec = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC)
      .addMethod(methodSpec)
      .build();

    note("pkgName=" + pkgName + " className=" + className.packageName() + " " +
      className.simpleName());
    //生成java文件
    writeClass(pkgName, simpleName, typeSpec);
  }


  private void generateConfigFile(HashSet<String> classSet) {
    StringBuffer sb = new StringBuffer();
    for (String name : classSet) {
      sb.append(name);
      sb.append("\n");
    }

    writeConfigFile(sb.toString());
  }

  private void writeConfigFile(String content) {
    try {
      FileObject source =
        filer.createResource(StandardLocation.CLASS_OUTPUT, "", DEFAULT_FILE_NAME);
      Writer writer = source.openWriter();
      writer.append(content);
      writer.flush();
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
