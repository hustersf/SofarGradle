package com.sofar.plugin.router;

/**
 * 此类中的类的路径不要修改
 * 修改需要同步 Router SDK 中对应 的 Const 类
 */
public class Const {

  public static final String ROUTER_SERVICE_INFO_FILE_NAME =
    "META-INF/services/router/service_info";

  public static final String PKG = "com.sofar.router.";

  //生成的代码
  public static final String GEN_PKG = PKG + "generated";
  public static final String GEN_PKG_SERVICE = GEN_PKG + ".service";

  /**
   * ServiceLoader初始化
   */
  public static final String SERVICE_LOADER_INIT = GEN_PKG + ".ServiceLoaderInit";
  public static final String INIT_METHOD = "init";

  public static final String SERVICE_IMPL_SUFFIX = "_Router_";  //实现类名的后缀
  public static final String SPLITTER = "_";
  public static final char DOT = '.';

}
