package com.sofar.gradle;

import android.util.Log;

public class B {

  private static final String TAG = "B";

  int b;

  int run() {
    Log.d(TAG, "run");
    return 1/0;
  }

  int run2() {
    Log.d(TAG, "run");
    return 1/0;
  }

  int run1() {
    try {
      Log.d(TAG, "run1");
      return 1;
    } catch (Exception e) {
      TryCatchExceptionUtil.defaultExceptionHandler(e);
      return 0;
    }
  }
}
