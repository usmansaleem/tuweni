// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.evm.impl

import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.bytes.MutableBytes
import org.apache.tuweni.units.bigints.UInt256

class Stack(private val maxSize: Int = 1025) {

  private val mutableStack = MutableBytes.create(maxSize * 32)
  private var size = 0
  private val stackElements = mutableListOf<Int>()

  fun get(i: Int): UInt256? {
    if (i >= size) {
      return null
    }
    return mutableStack.slice((size - i - 1) * 32, stackElements.get(size - i - 1))?.let { UInt256.fromBytes(it) }
  }

  fun getBytes(i: Int): Bytes? {
    if (i >= size) {
      return null
    }
    return mutableStack.slice((size - i - 1) * 32, stackElements.get(size - i - 1))
  }

  fun pop(): UInt256? {
    if (size <= 0) {
      return null
    }
    size--
    val elementSize = stackElements.get(stackElements.size - 1)
    stackElements.removeAt(stackElements.size - 1)
    return mutableStack.slice(size * 32, elementSize)?.let { UInt256.fromBytes(it) }
  }

  fun popBytes(): Bytes? {
    if (size <= 0) {
      return null
    }
    size--
    val elementSize = stackElements.get(stackElements.size - 1)
    stackElements.removeAt(stackElements.size - 1)
    return mutableStack.slice(size * 32, elementSize)
  }

  fun push(value: Bytes): Boolean {
    if (size >= maxSize) {
      return false
    }
    mutableStack.set((size * 32), value)
    size++
    stackElements.add(value.size())
    return true
  }

  fun size(): Int = this.size

  fun overflowed(): Boolean = size >= maxSize

  fun set(i: Int, elt: UInt256) {
    stackElements.set((size - i - 1), elt.size())
    mutableStack.set((size - i - 1) * 32, elt)
  }
}
