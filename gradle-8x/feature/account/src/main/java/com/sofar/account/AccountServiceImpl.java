package com.sofar.account;

import android.content.Context;
import android.widget.Toast;

import com.sofar.router.annotation.RouterService;

@RouterService(interfaces = IAccountService.class)
public class AccountServiceImpl implements IAccountService, Runnable {

  @Override
  public boolean isLogin() {
    return false;
  }

  @Override
  public void login(Context context) {
    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void run() {

  }
}
