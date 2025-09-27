package com.sofar.plugin.transform

class SetDiff<T> {

  private val addedList: MutableList<T> = ArrayList()
  private val unchangedList: MutableList<T> = ArrayList()
  private val removedList: MutableList<T> = ArrayList()

  constructor(beforeList: Set<T>, afterList: Set<T>) {
    addedList.addAll(afterList)
    beforeList.forEach {
      if (addedList.remove(it)) {
        unchangedList.add(it)
      } else {
        removedList.add(it)
      }
    }
  }

  fun getAddedList(): List<T> {
    return addedList
  }

  fun getUnchangedList(): List<T> {
    return unchangedList
  }

  fun getRemovedList(): List<T> {
    return removedList
  }

}