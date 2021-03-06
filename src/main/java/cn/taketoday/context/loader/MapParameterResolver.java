/*
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
package cn.taketoday.context.loader;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Properties;

import cn.taketoday.context.Ordered;
import cn.taketoday.context.OrderedSupport;
import cn.taketoday.context.annotation.DefaultProps;
import cn.taketoday.context.annotation.Props;
import cn.taketoday.context.factory.BeanFactory;
import cn.taketoday.context.utils.CollectionUtils;
import cn.taketoday.context.utils.ContextUtils;
import cn.taketoday.context.utils.ResolvableType;

/**
 * Resolve {@link Map}
 *
 * @author TODAY <br>
 * 2019-10-28 20:27
 */
public class MapParameterResolver
        extends OrderedSupport implements ExecutableParameterResolver, Ordered {

  public MapParameterResolver() {
    this(Integer.MAX_VALUE);
  }

  public MapParameterResolver(int order) {
    super(order);
  }

  @Override
  public boolean supports(Parameter parameter) {
    return Map.class.isAssignableFrom(parameter.getType());
  }

  /**
   * 处理所有Map参数
   * <p>
   * 有 Props 就注入Properties
   * </p>
   *
   * @param parameter
   *         Target method {@link Parameter}
   * @param beanFactory
   *         {@link BeanFactory}
   */
  @Override
  public Object resolve(Parameter parameter, BeanFactory beanFactory) {
    final Class<?> type = parameter.getType();
    final Map beansOfType = getBeansOfType(parameter, beanFactory);
    return convert(beansOfType, type);
  }

  protected Map getBeansOfType(Parameter parameter, BeanFactory beanFactory) {
    final Props props = getProps(parameter);
    if (props != null) { // 处理 Properties
      return ContextUtils.loadProps(props, System.getProperties());
    }

    final ResolvableType parameterType = ResolvableType.forParameter(parameter);
    final ResolvableType generic = parameterType.asMap().getGeneric(1);
    Class<?> beanClass = generic.toClass();
    return beanFactory.getBeansOfType(beanClass);
  }

  protected Map convert(Map map, final Class<?> type) {
    if (type != Map.class) {
      Map newMap = CollectionUtils.createMap(type, map.size());
      newMap.putAll(map);
      map = newMap;
    }
    return map;
  }

  private Props getProps(Parameter parameter) {
    Props props = parameter.getAnnotation(Props.class);
    if (props == null && Properties.class.isAssignableFrom(parameter.getType())) {
      return new DefaultProps();
    }
    return props;
  }

}
