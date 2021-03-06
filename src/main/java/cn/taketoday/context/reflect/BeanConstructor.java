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
package cn.taketoday.context.reflect;

/**
 * Fast call bean's {@link java.lang.reflect.Constructor Constructor}
 *
 * @author TODAY <br>
 * 2020-08-13 19:31
 * @see java.lang.reflect.Constructor
 */
@FunctionalInterface
public interface BeanConstructor<T> {

  /**
   * Invoke default {@link java.lang.reflect.Constructor}
   *
   * @return returns T
   *
   * @throws cn.taketoday.context.exception.BeanInstantiationException
   *         cannot instantiate a bean
   */
  default T newInstance() {
    return newInstance(null);
  }

  /**
   * Invoke {@link java.lang.reflect.Constructor} with given args
   *
   * @return returns T
   *
   * @throws cn.taketoday.context.exception.BeanInstantiationException
   *         cannot instantiate a bean
   */
  T newInstance(Object[] args);
}
