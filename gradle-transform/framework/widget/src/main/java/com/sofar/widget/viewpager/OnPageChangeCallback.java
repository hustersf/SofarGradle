package com.sofar.widget.viewpager;

import androidx.annotation.Px;

public abstract class OnPageChangeCallback {

  public void onPageScrolled(int position, float positionOffset, @Px int positionOffsetPixels) {}

  public void onPageSelected(int position) {}

  public void onPageScrollStateChanged(@ScrollEventAdapter.ScrollState int state) {}
}
