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

import java.lang.reflect.Field;

import cn.taketoday.context.utils.ReflectionUtils;

/**
 * @author TODAY 2020/9/19 22:38
 */
public final class FieldSetterMethod implements SetterMethod {
  private final Field field;

  public FieldSetterMethod(final Field field) {
    this.field = field;
  }

  @Override
  public void set(Object obj, Object value) {
    ReflectionUtils.setField(field, obj, value);
  }
}
