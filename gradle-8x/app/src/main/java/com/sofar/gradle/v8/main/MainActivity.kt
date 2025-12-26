package com.sofar.gradle.v8.main

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.sofar.gradle.v8.R
import com.sofar.gradle.v8.base.BaseUIActivity

class MainActivity : BaseUIActivity() {

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: MainListAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    initData()
  }

  override fun layoutId(): Int {
    return R.layout.main_activity
  }

  private fun initView() {
    recyclerView = findViewById(R.id.main_list)
    adapter = MainListAdapter()
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)
    val padding = resources.getDimensionPixelSize(R.dimen.page_padding)
    recyclerView.addItemDecoration(object : ItemDecoration() {
      override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
      ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = padding
        outRect.bottom = padding
        outRect.left = padding
        outRect.right = padding
      }
    })
  }

  private fun initData() {
    adapter.setItems(pages())
    adapter.notifyDataSetChanged()
  }

  private fun pages(): List<PageData> {
    val list: MutableList<PageData> = mutableListOf()
    list.add(PageData("组件化", "sofar://router"))
    list.add(PageData("try-catch插件", "sofar://try_catch"))
    return list
  }
}