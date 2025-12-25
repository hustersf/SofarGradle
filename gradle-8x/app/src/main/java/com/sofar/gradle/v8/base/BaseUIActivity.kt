package com.sofar.gradle.v8.base

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sofar.gradle.v8.R

abstract class BaseUIActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.base_ui_activity)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }
    val contentView: FrameLayout = findViewById(R.id.content_layout)
    LayoutInflater.from(this).inflate(layoutId(), contentView, true)
  }

  @LayoutRes
  abstract fun layoutId(): Int
}