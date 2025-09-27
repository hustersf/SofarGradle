package com.sofar.account;

import android.content.Context;
import android.widget.Toast;

import com.sofar.router.annotation.RouterService;

@RouterService(interfaces = IAccountServiceV2.class)
public class AccountServiceImplV2 implements IAccountServiceV2 {

  @Override
  public void login(Context context) {
    Toast.makeText(context, "登录成功V2", Toast.LENGTH_SHORT).show();
  }

}
