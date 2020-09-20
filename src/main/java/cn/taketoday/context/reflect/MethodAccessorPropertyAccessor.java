/*
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
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.context.reflect;

import java.lang.reflect.Method;

import cn.taketoday.context.utils.Assert;
import cn.taketoday.context.utils.ReflectionUtils;

/**
 * @author TODAY
 * @date 2020/9/11 15:54
 */
public class MethodAccessorPropertyAccessor implements PropertyAccessor {

  private final MethodAccessor setMethodAccessor;
  private final MethodAccessor getMethodAccessor;

  public MethodAccessorPropertyAccessor(Method setMethod, Method getMethod) {
      Assert.notNull(setMethod, "setMethod must not be null");
      Assert.notNull(getMethod, "getMethod must not be null");
      
      this.setMethodAccessor = ReflectionUtils.newMethodAccessor(setMethod);
      this.getMethodAccessor = ReflectionUtils.newMethodAccessor(getMethod);
  }
  
  public MethodAccessorPropertyAccessor(MethodAccessor setMethodAccessor,
                                        MethodAccessor getMethodAccessor) {
    Assert.notNull(setMethodAccessor, "setMethodAccessor must not be null");
    Assert.notNull(getMethodAccessor, "getMethodAccessor must not be null");

    this.setMethodAccessor = setMethodAccessor;
    this.getMethodAccessor = getMethodAccessor;
  }


  @Override
  public Object get(final Object obj) {
    return getMethodAccessor.invoke(obj, null);
  }

  @Override
  public void set(final Object obj, final Object value) {
    setMethodAccessor.invoke(obj, new Object[] { value });
  }
}