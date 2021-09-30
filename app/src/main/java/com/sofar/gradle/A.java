package com.sofar.gradle;

import android.util.Log;

public class A {

  private static final String TAG = "A";

  int a;

  void run() {
    Log.d(TAG, "run");
    if (a == 1) {
      a = a * 1;
    } else {
      a = a - 1;
    }
  }

  void run1() {
    try {
      Log.d(TAG, "run1");
    } catch (Exception e) {
      TryCatchExceptionUtil.defaultExceptionHandler(e);
    }
  }
}
