package com.sofar.gradle.v8.trycatch

import android.os.Bundle
import android.widget.Button
import com.sofar.gradle.v8.R
import com.sofar.gradle.v8.base.BaseUIActivity

class TryCatchActivity : BaseUIActivity() {

  private lateinit var aBtn: Button
  private lateinit var bBtn: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    initData()
  }

  override fun layoutId(): Int {
    return R.layout.trycatch_activity
  }

  private fun initView() {
    aBtn = findViewById(R.id.a_btn)
    bBtn = findViewById(R.id.b_btn)

    aBtn.setOnClickListener {
      CrashA().run()
      CrashA().run2()
      CrashA().run3()
    }

    bBtn.setOnClickListener {
      CrashB().run()
      CrashB().run2()
      CrashB().run3()
    }
  }

  private fun initData() {}
}