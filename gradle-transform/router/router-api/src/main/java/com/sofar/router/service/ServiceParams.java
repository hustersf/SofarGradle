package com.sofar.router.service;

public class ServiceParams {

  public Class interfaceCls;
  public Class implCls;
  public boolean singleton;

  public ServiceParams(Class interfaceCls, Class implCls, boolean singleton) {
    this.interfaceCls = interfaceCls;
    this.implCls = implCls;
    this.singleton = singleton;
  }
}
