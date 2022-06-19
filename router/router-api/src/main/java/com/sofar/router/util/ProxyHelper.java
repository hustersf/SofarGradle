package com.sofar.router.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyHelper {

  private static final InvocationHandler INVOCATION_HANDLER = new InvocationHandler() {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Class<?> retType = method.getReturnType();
      String retStr = "0";
      if (retType == Byte.class || retType == byte.class) {
        return Byte.valueOf(retStr);
      }
      if (retType == Short.class || retType == short.class) {
        return Short.valueOf(retStr);
      }
      if (retType == Integer.class || retType == int.class) {
        return Integer.valueOf(retStr);
      }
      if (retType == Long.class || retType == long.class) {
        return Long.valueOf(retStr);
      }
      if (retType == Float.class || retType == float.class) {
        return Float.valueOf(retStr);
      }
      if (retType == Double.class || retType == double.class) {
        return Double.valueOf(retStr);
      }
      if (retType == Boolean.class || retType == boolean.class) {
        return false;
      }
      if (retType == Character.class || retType == char.class) {
        return retStr.charAt(0);
      }
      if (retType == String.class) {
        return retStr;
      }
      return null;
    }
  };

  public static <T> T proxyObject(Class<T> service) {
    return (T) Proxy.newProxyInstance(service.getClassLoader(),
      new Class<?>[]{service}, INVOCATION_HANDLER);
  }

}
