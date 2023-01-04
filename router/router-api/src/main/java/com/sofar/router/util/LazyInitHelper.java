package com.sofar.router.util;

import android.os.SystemClock;

import com.sofar.router.log.Debugger;

public abstract class LazyInitHelper {

  private boolean hasInit = false;
  private final String TAG;

  public LazyInitHelper(String tag) {
    this.TAG = tag;
  }

  public void init() {
    performInit();
  }

  public void ensureInit() {
    performInit();
  }

  public void performInit() {
    if (!hasInit) {
      synchronized (this) {
        if (!hasInit) {
          long ts = SystemClock.elapsedRealtime();
          doInit();
          hasInit = true;
          Debugger.i("[" + TAG + "] init cost time %s ms", SystemClock.elapsedRealtime() - ts);
        }
      }
    }
  }

  protected abstract void doInit();
}
