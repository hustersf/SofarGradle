package com.sofar.gradle.v8.router

import android.os.Bundle
import android.widget.Button
import com.sofar.account.IAccountService
import com.sofar.gradle.v8.R
import com.sofar.gradle.v8.base.BaseUIActivity
import com.sofar.router.service.ServiceLoader
import com.sofar.share.IShareService

class RouterActivity : BaseUIActivity() {

  private lateinit var loginBtn: Button
  private lateinit var shareBtn: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    initData()
  }

  override fun layoutId(): Int {
    return R.layout.router_activity
  }

  private fun initView() {
    loginBtn = findViewById(R.id.login_btn)
    shareBtn = findViewById(R.id.share_btn)

    loginBtn.setOnClickListener {
      ServiceLoader.get(IAccountService::class.java).login(this)
    }

    shareBtn.setOnClickListener {
      ServiceLoader.get(IShareService::class.java).share(this)
    }
  }

  private fun initData() {}

}