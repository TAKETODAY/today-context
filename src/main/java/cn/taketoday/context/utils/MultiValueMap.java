/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2021 All Rights Reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */
package cn.taketoday.context.utils;

import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * Extension of the {@code Map} interface that stores multiple values.
 *
 * <p>
 * From Spring
 *
 * @param <K>
 *         the key type
 * @param <V>
 *         the value element type
 *
 * @author Arjen Poutsma
 * @author TODAY <br>
 * 2020-01-27 13:06
 * @since 2.1.7
 */
public interface MultiValueMap<K, V> extends Map<K, List<V>> {

  /**
   * Return the first value for the given key.
   *
   * @param key
   *         the key
   *
   * @return the first value for the specified key, or {@code null} if none
   */
  V getFirst(K key);

  /**
   * Add the given single value to the current list of values for the given key.
   *
   * @param key
   *         the key
   * @param value
   *         the value to be added
   */
  void add(K key, V value);

  /**
   * Add all the values of the given list to the current list of values for the
   * given key.
   *
   * @param key
   *         they key
   * @param values
   *         the values to be added
   */
  void addAll(K key, List<? extends V> values);

  /**
   * Add all the values of the given {@code MultiValueMap} to the current values.
   *
   * @param values
   *         the values to be added
   */
  void addAll(MultiValueMap<K, V> values);

  /**
   * {@link #add(Object, Object) Add} the given value, only when the map does not
   * {@link #containsKey(Object) contain} the given key.
   *
   * @param key
   *         the key
   * @param value
   *         the value to be added
   */
  default void addIfAbsent(K key, V value) {
    if (!containsKey(key)) {
      add(key, value);
    }
  }

  /**
   * Set the given single value under the given key.
   *
   * @param key
   *         the key
   * @param value
   *         the value to set
   */
  void set(K key, V value);

  /**
   * Set the given values under.
   *
   * @param values
   *         the values.
   */
  void setAll(Map<K, V> values);

  /**
   * Return a {@code Map} with the first values contained in this
   * {@code MultiValueMap}.
   *
   * @return a single value representation of this map
   */
  Map<K, V> toSingleValueMap();

  /**
   * @since 3.0
   */
  Map<K, V[]> toArrayMap(IntFunction<V[]> mappingFunction);

  /**
   * @since 3.0
   */
  void copyToArrayMap(Map<K, V[]> newMap, IntFunction<V[]> function);

}
