// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.toml;

import static org.apache.tuweni.toml.TomlType.typeNameFor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

/** An array of TOML values. */
public interface TomlArray {

  /**
   * Provides the size of the array.
   *
   * @return The size of the array.
   */
  int size();

  /**
   * Returns true if the array is empty.
   *
   * @return {@code true} if the array is empty.
   */
  boolean isEmpty();

  /**
   * Returns true if the array contains strings.
   *
   * @return {@code true} if the array contains strings.
   */
  boolean containsStrings();

  /**
   * Returns true if the array contains longs.
   *
   * @return {@code true} if the array contains longs.
   */
  boolean containsLongs();

  /**
   * Returns true if the array contains doubles.
   *
   * @return {@code true} if the array contains doubles.
   */
  boolean containsDoubles();

  /**
   * Returns true if the array contains booleans.
   *
   * @return {@code true} if the array contains booleans.
   */
  boolean containsBooleans();

  /**
   * Returns true if the array contains offset date times.
   *
   * @return {@code true} if the array contains {@link OffsetDateTime}s.
   */
  boolean containsOffsetDateTimes();

  /**
   * Returns true if the array contains local date times.
   *
   * @return {@code true} if the array contains {@link LocalDateTime}s.
   */
  boolean containsLocalDateTimes();

  /**
   * Returns true if the array contains local dates.
   *
   * @return {@code true} if the array contains {@link LocalDate}s.
   */
  boolean containsLocalDates();

  /**
   * Returns true if the array contains local time objects.
   *
   * @return {@code true} if the array contains {@link LocalTime}s.
   */
  boolean containsLocalTimes();

  /**
   * Returns true if the array contains arrays.
   *
   * @return {@code true} if the array contains arrays.
   */
  boolean containsArrays();

  /**
   * Returns true if the array contains tables.
   *
   * @return {@code true} if the array contains tables.
   */
  boolean containsTables();

  /**
   * Get a value at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   */
  Object get(int index);

  /**
   * Get the position where a value is defined in the TOML document.
   *
   * @param index The array index.
   * @return The input position.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   */
  TomlPosition inputPositionOf(int index);

  /**
   * Get a string at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   * @throws TomlInvalidTypeException If the value is not a long.
   */
  default String getString(int index) {
    Object value = get(index);
    if (!(value instanceof String)) {
      throw new TomlInvalidTypeException("key at index " + index + " is a " + typeNameFor(value));
    }
    return (String) value;
  }

  /**
   * Get a long at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   * @throws TomlInvalidTypeException If the value is not a long.
   */
  default long getLong(int index) {
    Object value = get(index);
    if (!(value instanceof Long)) {
      throw new TomlInvalidTypeException("key at index " + index + " is a " + typeNameFor(value));
    }
    return (Long) value;
  }

  /**
   * Get a double at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   * @throws TomlInvalidTypeException If the value is not a long.
   */
  default double getDouble(int index) {
    Object value = get(index);
    if (!(value instanceof Double)) {
      throw new TomlInvalidTypeException("key at index " + index + " is a " + typeNameFor(value));
    }
    return (Double) value;
  }

  /**
   * Get a boolean at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   * @throws TomlInvalidTypeException If the value is not a long.
   */
  default boolean getBoolean(int index) {
    Object value = get(index);
    if (!(value instanceof Boolean)) {
      throw new TomlInvalidTypeException("key at index " + index + " is a " + typeNameFor(value));
    }
    return (Boolean) value;
  }

  /**
   * Get an offset date time at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   * @throws TomlInvalidTypeException If the value is not an {@link OffsetDateTime}.
   */
  default OffsetDateTime getOffsetDateTime(int index) {
    Object value = get(index);
    if (!(value instanceof OffsetDateTime)) {
      throw new TomlInvalidTypeException("key at index " + index + " is a " + typeNameFor(value));
    }
    return (OffsetDateTime) value;
  }

  /**
   * Get a local date time at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   * @throws TomlInvalidTypeException If the value is not an {@link LocalDateTime}.
   */
  default LocalDateTime getLocalDateTime(int index) {
    Object value = get(index);
    if (!(value instanceof LocalDateTime)) {
      throw new TomlInvalidTypeException("key at index " + index + " is a " + typeNameFor(value));
    }
    return (LocalDateTime) value;
  }

  /**
   * Get a local date at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   * @throws TomlInvalidTypeException If the value is not an {@link LocalDate}.
   */
  default LocalDate getLocalDate(int index) {
    Object value = get(index);
    if (!(value instanceof LocalDate)) {
      throw new TomlInvalidTypeException("key at index " + index + " is a " + typeNameFor(value));
    }
    return (LocalDate) value;
  }

  /**
   * Get a local time at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   * @throws TomlInvalidTypeException If the value is not an {@link LocalTime}.
   */
  default LocalTime getLocalTime(int index) {
    Object value = get(index);
    if (!(value instanceof LocalTime)) {
      throw new TomlInvalidTypeException("key at index " + index + " is a " + typeNameFor(value));
    }
    return (LocalTime) value;
  }

  /**
   * Get an array at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   * @throws TomlInvalidTypeException If the value is not an array.
   */
  default TomlArray getArray(int index) {
    Object value = get(index);
    if (!(value instanceof TomlArray)) {
      throw new TomlInvalidTypeException("key at index " + index + " is a " + typeNameFor(value));
    }
    return (TomlArray) value;
  }

  /**
   * Get a table at a specified index.
   *
   * @param index The array index.
   * @return The value.
   * @throws IndexOutOfBoundsException If the index is out of bounds.
   * @throws TomlInvalidTypeException If the value is not a table.
   */
  default TomlTable getTable(int index) {
    Object value = get(index);
    if (!(value instanceof TomlTable)) {
      throw new TomlInvalidTypeException("key at index " + index + " is a " + typeNameFor(value));
    }
    return (TomlTable) value;
  }

  /**
   * Get the elements of this array as a {@link List}.
   *
   * <p>Note that this does not do a deep conversion. If this array contains tables or arrays, they
   * will be of type {@link TomlTable} or {@link TomlArray} respectively.
   *
   * @return The elements of this array as a {@link List}.
   */
  List<Object> toList();

  /**
   * Return a representation of this array using JSON.
   *
   * @return A JSON representation of this array.
   */
  default String toJson() {
    StringBuilder builder = new StringBuilder();
    try {
      toJson(builder);
    } catch (IOException e) {
      // not reachable
      throw new UncheckedIOException(e);
    }
    return builder.toString();
  }

  /**
   * Append a JSON representation of this array to the appendable output.
   *
   * @param appendable The appendable output.
   * @throws IOException If an IO error occurs.
   */
  default void toJson(Appendable appendable) throws IOException {
    JsonSerializer.toJson(this, appendable);
  }
}
