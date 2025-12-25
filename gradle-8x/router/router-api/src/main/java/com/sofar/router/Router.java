package com.sofar.router;

import com.sofar.router.service.ServiceLoader;

public class Router {

  private static class Inner {
    private static Router INSTANCE = new Router();
  }

  public static Router get() {
    return Inner.INSTANCE;
  }

  public <T> T getService(Class<T> clazz) {
    return ServiceLoader.get(clazz);
  }

  public void init(){
    loadRouterMap();
  }

  private static void loadRouterMap(){

  }
}
