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
package cn.taketoday.context.loader;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Map.Entry;

import cn.taketoday.context.ApplicationContext;
import cn.taketoday.context.Constant;
import cn.taketoday.context.Ordered;
import cn.taketoday.context.annotation.Autowired;
import cn.taketoday.context.aware.OrderedApplicationContextSupport;
import cn.taketoday.context.factory.BeanDefinition;
import cn.taketoday.context.factory.BeanFactory;
import cn.taketoday.context.factory.BeanReferencePropertySetter;
import cn.taketoday.context.factory.PropertySetter;
import cn.taketoday.context.utils.ClassUtils;
import cn.taketoday.context.utils.StringUtils;

import static cn.taketoday.context.utils.ClassUtils.isAnnotationPresent;

/**
 * This {@link PropertyValueResolver} supports field that annotated
 * {@link Autowired}, {@link javax.annotation.Resource Resource},
 * {@link javax.inject.Inject Inject} or {@link javax.inject.Named Named}
 *
 * @author TODAY <br>
 * 2018-08-04 15:56
 */
public class AutowiredPropertyResolver
        extends OrderedApplicationContextSupport implements PropertyValueResolver {

  private static final Class<? extends Annotation> NAMED_CLASS = ClassUtils.loadClass("javax.inject.Named");
  private static final Class<? extends Annotation> INJECT_CLASS = ClassUtils.loadClass("javax.inject.Inject");
  private static final Class<? extends Annotation> RESOURCE_CLASS = ClassUtils.loadClass("javax.annotation.Resource");

  public AutowiredPropertyResolver(ApplicationContext context) {
    this(context, Ordered.HIGHEST_PRECEDENCE);
  }

  public AutowiredPropertyResolver(ApplicationContext context, int order) {
    super(order);
    setApplicationContext(context);
  }

  @Override
  public boolean supportsProperty(final Field field) {
    return isInjectable(field);
  }

  public static boolean isInjectable(final AnnotatedElement element) {
    return isAnnotationPresent(element, Autowired.class)
            || isAnnotationPresent(element, RESOURCE_CLASS)
            || isAnnotationPresent(element, NAMED_CLASS)
            || isAnnotationPresent(element, INJECT_CLASS);
  }

  @Override
  public PropertySetter resolveProperty(final Field field) {
    final Autowired autowired = field.getAnnotation(Autowired.class); // auto wired

    String name = null;
    final Class<?> propertyClass = field.getType();
    if (autowired != null) {
      name = autowired.value();
    }
    else if (isAnnotationPresent(field, RESOURCE_CLASS)) { // @Resource
      name = ClassUtils.getAnnotationAttributes(RESOURCE_CLASS, field).getString("name");
    }
    else if (isAnnotationPresent(field, NAMED_CLASS)) {// @Named
      name = ClassUtils.getAnnotationAttributes(NAMED_CLASS, field).getString(Constant.VALUE);
    } // @Inject or name is empty

    if (StringUtils.isEmpty(name)) {
      name = byType(propertyClass);
    }
    // @since 3.0
    final boolean required = AutowiredParameterResolver.isRequired(field, autowired);
    return new BeanReferencePropertySetter(name, required, field);
  }

  /**
   * Create bean name by type
   *
   * @param targetClass
   *         target property class
   *
   * @return a bean name none null
   */
  protected String byType(final Class<?> targetClass) {
    final ApplicationContext context = obtainApplicationContext();

    if (context.hasStarted()) {
      final String name = findName(context, targetClass);
      if (StringUtils.isNotEmpty(name)) {
        return name;
      }
    }

    return context.getEnvironment().getBeanNameCreator().create(targetClass);
  }

  /**
   * Find bean name in the {@link BeanFactory}
   *
   * @param applicationContext
   *         factory
   * @param propertyClass
   *         property class
   *
   * @return a name found in {@link BeanFactory} if not found will returns null
   */
  protected String findName(ApplicationContext applicationContext, Class<?> propertyClass) {
    for (Entry<String, BeanDefinition> entry : applicationContext.getBeanDefinitions().entrySet()) {
      if (propertyClass.isAssignableFrom(entry.getValue().getBeanClass())) {
        return entry.getKey();
      }
    }
    return null;
  }

}
