package com.sofar.gradle;

import android.util.Log;

public class TryCatchExceptionUtil {

  public static void defaultExceptionHandler(Exception e) {
    Log.e("TryCatchExceptionUtil", "error", e);
  }

}
