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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.taketoday.aop.support.annotation;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author TODAY 2018-10-13 11:27
 * @see cn.taketoday.aop.annotation.Before
 */
public class BeforeMethodInterceptor extends AbstractAnnotationMethodInterceptor {
  public static final int DEFAULT_ORDER = 2;

  public BeforeMethodInterceptor(Method method, Object aspect) {
    super(method, aspect);
    setOrder(DEFAULT_ORDER);
  }

  @Override
  public Object invoke(MethodInvocation inv) throws Throwable {
    invokeAdviceMethod(inv, null, null);
    return inv.proceed();
  }

}