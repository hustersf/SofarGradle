package com.sofar.router.service;

import java.util.HashMap;

import com.sofar.router.Const;
import com.sofar.router.log.Debugger;
import com.sofar.router.util.LazyInitHelper;
import com.sofar.router.util.ProxyHelper;
import com.sofar.router.util.SingletonPool;

public class ServiceLoader {

  private static HashMap<Class, ServiceLoader> SERVICES = new HashMap<>(100);

  private HashMap<String, ServiceParams> mParamsMap = new HashMap<>();

  private ServiceLoader() {}

  private static final LazyInitHelper sInitHelper = new LazyInitHelper("ServiceLoader") {

    @Override
    protected void doInit() {
      try {
        //反射调用Init类，避免引用的类过多，导致main dex capacity exceeded问题
        Class.forName(Const.SERVICE_LOADER_INIT)
          .getMethod(Const.INIT_METHOD)
          .invoke(null);
        Debugger.i("[ServiceLoader] init class invoked");
      } catch (Exception e) {
        Debugger.i("[ServiceLoader] init failed=" + e);
      }
    }
  };

  /**
   * 此方法可以放在子线程加载
   */
  public static void init() {
    sInitHelper.init();
  }

  /**
   * 根据接口，返回实现类的对象
   */
  public static <T> T get(Class<T> interfaceCls) {
    sInitHelper.ensureInit();
    ServiceLoader loader = SERVICES.get(interfaceCls);
    if (loader == null) {
      //未注册，返回一个代理对象
      Debugger.i("[ServiceLoader] 未建立接口->实现类的映射关系");
      return ProxyHelper.proxyObject(interfaceCls);
    }
    return loader.getImpl(interfaceCls);
  }

  private <T> T getImpl(Class<T> interfaceCls) {
    String key = buildKey(interfaceCls);
    ServiceParams params = mParamsMap.get(key);

    try {
      //每次都返回同一个对象
      Class<T> implCls = params.implCls;
      if (params.singleton) {
        return SingletonPool.get(implCls);
      } else {
        T t = (T) params.implCls.newInstance();
        Debugger.i("[ServiceLoader] create instance: %s, result = %s", implCls, t);
        return t;
      }
    } catch (Exception e) {
      Debugger.fatal(e);
    }
    return null;
  }

  /**
   * 注册接口->实现类映射关系
   * APT阶段生成的模版代码会调用此方法
   */
  public static void put(Class interfaceCls, Class implCls, boolean singleton) {
    ServiceLoader loader = SERVICES.get(interfaceCls);
    if (loader == null) {
      loader = new ServiceLoader();
      SERVICES.put(interfaceCls, loader);
    }
    loader.putImpl(interfaceCls, implCls, singleton);
  }

  private void putImpl(Class interfaceCls, Class implCls, boolean singleton) {
    ServiceParams params = new ServiceParams(interfaceCls, implCls, singleton);
    mParamsMap.put(buildKey(interfaceCls), params);
  }

  private String buildKey(Class interfaceCls) {
    return interfaceCls.getName();
  }

}
