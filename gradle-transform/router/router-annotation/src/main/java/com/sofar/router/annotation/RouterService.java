package com.sofar.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RouterService {

  Class interfaces();

  /**
   * 获取Service实例时，是否是单例
   */
  boolean singleton() default false;
}
