package com.sofar.router.util;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import com.sofar.router.log.Debugger;

public class SingletonPool {

  private static final Map<Class, Object> CACHE = new HashMap<>();

  public static <I, T extends I> T get(Class<I> clazz) throws Exception {
    if (clazz == null) {
      return null;
    }
    Object instance = getInstance(clazz);
    Debugger.i("[SingletonPool]  get instance of class = %s, result = %s", clazz, instance);
    return (T) instance;
  }

  private static Object getInstance(@NonNull Class clazz) throws Exception {
    Object t = CACHE.get(clazz);
    if (t != null) {
      return t;
    }

    synchronized (CACHE) {
      t = CACHE.get(clazz);
      if (t == null) {
        t = clazz.newInstance();
        CACHE.put(clazz, t);
        Debugger.i("[SingletonPool] >>> create instance: %s", clazz);
      }
    }
    return t;
  }

}
