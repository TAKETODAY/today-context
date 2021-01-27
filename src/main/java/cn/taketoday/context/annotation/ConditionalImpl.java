/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2020 All Rights Reserved.
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.taketoday.context.annotation;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import cn.taketoday.context.Condition;
import cn.taketoday.context.Constant;

/**
 * @author TODAY <br>
 * 2018-11-14 22:32
 */
@SuppressWarnings("all")
public class ConditionalImpl implements Conditional {

  private Class<? extends Condition>[] value;

  @Override
  public Class<? extends Annotation> annotationType() {
    return Conditional.class;
  }

  @Override
  public Class<? extends Condition>[] value() {
    return value == null ? (Class<? extends Condition>[]) Constant.EMPTY_CLASS_ARRAY : value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Conditional)) return false;
    final Conditional that = (Conditional) o;
    return Arrays.equals(value, that.value());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }
}
