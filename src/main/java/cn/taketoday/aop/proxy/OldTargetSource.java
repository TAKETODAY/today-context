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
package cn.taketoday.aop.proxy;

import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cn.taketoday.context.utils.CollectionUtils;

/**
 * @author TODAY <br>
 * 2018-11-10 11:47
 */
public class OldTargetSource {

  private Object target;
  private Class<?> targetClass;
  private Class<?>[] interfaces;
  private Map<Method, List<MethodInterceptor>> aspectMappings;

  public OldTargetSource(Object target, Class<?> targetClass) {
    this.target = target;
    this.targetClass = targetClass;
    this.interfaces = targetClass.getInterfaces();
  }

  public boolean contains(Method method) {
    final Map<Method, List<MethodInterceptor>> aspectMappings = getAspectMappings();
    return !CollectionUtils.isEmpty(aspectMappings) && aspectMappings.containsKey(method);
  }

  public Object getTarget() {
    return target;
  }

  public void setTarget(Object target) {
    this.target = target;
  }

  public Class<?> getTargetClass() {
    return targetClass;
  }

  public void setTargetClass(Class<?> targetClass) {
    this.targetClass = targetClass;
  }

  public Class<?>[] getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(Class<?>[] interfaces) {
    this.interfaces = interfaces;
  }

  public Map<Method, List<MethodInterceptor>> getAspectMappings() {
    return aspectMappings;
  }

  public void setAspectMappings(Map<Method, List<MethodInterceptor>> aspectMappings) {
    this.aspectMappings = aspectMappings;
  }

  @Override
  public String toString() {
    return String.format("{\"target\":\"%s\",\"targetClass\":\"%s\",\"interfaces\":\"%s\",\"aspectMappings\":\"%s\"}", //
                         target, targetClass, Arrays.toString(interfaces), aspectMappings);
  }
}