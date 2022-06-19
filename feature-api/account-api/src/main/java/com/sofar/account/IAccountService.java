package com.sofar.account;

import android.content.Context;

public interface IAccountService {

  boolean isLogin();

  void login(Context context);
}
