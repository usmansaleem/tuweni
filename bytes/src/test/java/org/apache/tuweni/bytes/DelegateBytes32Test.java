// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DelegateBytes32Test {

  @Test
  void failsWhenWrappingArraySmallerThan32() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> Bytes32.wrap(Bytes.wrap(new byte[31])));
    assertEquals("Expected 32 bytes but got 31", exception.getMessage());
  }

  @Test
  void failsWhenWrappingArrayLargerThan32() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> Bytes32.wrap(Bytes.wrap(new byte[33])));
    assertEquals("Expected 32 bytes but got 33", exception.getMessage());
  }

  @Test
  void failsWhenLeftPaddingValueLargerThan32() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class, () -> Bytes32.leftPad(MutableBytes.create(33)));
    assertEquals("Expected at most 32 bytes but got 33", exception.getMessage());
  }

  @Test
  void failsWhenRightPaddingValueLargerThan32() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class, () -> Bytes32.rightPad(MutableBytes.create(33)));
    assertEquals("Expected at most 32 bytes but got 33", exception.getMessage());
  }

  @Test
  void testSize() {
    assertEquals(32, new DelegatingBytes32(Bytes32.random()).size());
  }

  @Test
  void testCopy() {
    Bytes bytes = new DelegatingBytes32(Bytes32.random()).copy();
    assertEquals(bytes, bytes.copy());
    assertEquals(bytes, bytes.mutableCopy());
  }

  @Test
  void testSlice() {
    Bytes bytes = new DelegatingBytes32(Bytes32.random()).copy();
    assertEquals(
        Bytes.wrap(new byte[] {bytes.get(2), bytes.get(3), bytes.get(4)}), bytes.slice(2, 3));
  }
}
