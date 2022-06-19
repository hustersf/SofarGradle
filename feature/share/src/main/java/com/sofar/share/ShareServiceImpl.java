package com.sofar.share;

import android.content.Context;
import android.widget.Toast;

import com.sofar.router.annotation.RouterService;

@RouterService(interfaces = IShareService.class, singleton = true)
public class ShareServiceImpl implements IShareService {

  @Override
  public void share(Context context) {
    Toast.makeText(context, "分享成功", Toast.LENGTH_SHORT).show();
  }
}
