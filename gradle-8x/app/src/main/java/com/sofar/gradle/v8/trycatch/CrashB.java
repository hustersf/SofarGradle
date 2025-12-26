package com.sofar.gradle.v8.trycatch;

public class CrashB {
  public void run() {
    int a = 1 / 0;
  }

  public int run2() {
    int a = 1 / 0;
    return a;
  }

  public boolean run3() {
    int a = 1 / 0;
    return true;
  }
}
