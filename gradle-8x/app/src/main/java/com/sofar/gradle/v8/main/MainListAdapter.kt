package com.sofar.gradle.v8.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sofar.gradle.v8.R

class MainListAdapter : RecyclerView.Adapter<MainListAdapter.ItemViewHolder>() {

  private var dataList: MutableList<PageData> = mutableListOf()

  fun setItems(list: List<PageData>) {
    dataList.clear()
    dataList.addAll(list)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
    val itemView: View = LayoutInflater.from(parent.context).inflate(
      R.layout.main_list_item,
      parent, false
    )
    return ItemViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return dataList.size
  }

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
    val item = dataList[position]
    holder.nameTv.text = item.name
    holder.itemView.setOnClickListener { jump(holder.itemView.context, item) }
  }

  class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var nameTv: TextView = itemView.findViewById(R.id.name)
  }

  private fun jump(context: Context, pageData: PageData) {
    try {
      val intent = Intent()
      intent.setData(Uri.parse(pageData.uri))
      context.startActivity(intent)
    } catch (e: Exception) {
      Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
    }
  }
}