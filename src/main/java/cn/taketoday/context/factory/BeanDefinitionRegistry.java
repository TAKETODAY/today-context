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
package cn.taketoday.context.factory;

import java.util.Map;
import java.util.Set;

import cn.taketoday.context.exception.BeanDefinitionStoreException;

/**
 * Store bean definitions.
 *
 * @author TODAY <br>
 *
 * 2018-07-08 19:56:53 2018-08-06 11:07
 */
public interface BeanDefinitionRegistry {

  /**
   * Get {@link BeanDefinition}s map
   */
  Map<String, BeanDefinition> getBeanDefinitions();

  /**
   * register a bean with the given name and {@link BeanDefinition}
   *
   * @param def
   *         Bean definition
   *
   * @since 1.2.0
   */
  void registerBeanDefinition(String name, BeanDefinition def);

  /**
   * Remove the BeanDefinition for the given name.
   *
   * @param beanName
   *         The name of the bean instance to register
   */
  void removeBeanDefinition(String beanName);

  /**
   * Return the BeanDefinition for the given bean name. Return the BeanDefinition
   * for the given bean name.
   *
   * @param beanName
   *         Name of the bean to find a definition for
   *
   * @return the BeanDefinition for the given name (never {@code null})
   */
  BeanDefinition getBeanDefinition(String beanName);

  /**
   * Return the BeanDefinition for the given bean class.
   *
   * @param beanClass
   *         Bean definition bean class
   */
  BeanDefinition getBeanDefinition(Class<?> beanClass);

  /**
   * Check if this registry contains a bean definition with the given name.
   *
   * @param beanName
   *         The name of the bean to look for
   *
   * @return If this registry contains a bean definition with the given name
   */
  boolean containsBeanDefinition(String beanName);

  /**
   * Whether there is a bean with the given name and type.
   *
   * @param beanName
   *         The name of the bean to look for
   * @param type
   *         Bean type
   *
   * @return If exist a bean with given name and type
   *
   * @since 2.1.7
   */
  default boolean containsBeanDefinition(String beanName, Class<?> type) {
    return containsBeanDefinition(beanName) && containsBeanDefinition(type);
  }

  /**
   * Whether there is a bean with the given type.
   *
   * @param type
   *         The bean class of the bean to look for
   *
   * @return If exist a bean with given type
   */
  boolean containsBeanDefinition(Class<?> type);

  /**
   * Whether there is a bean with the given type.
   *
   * @param type
   *         Target type
   * @param equals
   *         Must equals type
   *
   * @return If exist a bean with given type
   */
  boolean containsBeanDefinition(Class<?> type, boolean equals);

  /**
   * Return the names of all beans defined in this registry.
   *
   * @return the names of all beans defined in this registry, or an empty set if
   * none defined
   */
  Set<String> getBeanDefinitionNames();

  /**
   * Return the number of beans defined in the registry.
   *
   * @return the number of beans defined in the registry
   */
  int getBeanDefinitionCount();

  /**
   * Register {@link BeanDefinition} with {@link BeanDefinition#getName()}
   *
   * @param def
   *         Target {@link BeanDefinition}
   *
   * @since 2.1.6
   */
  default void registerBeanDefinition(BeanDefinition def) {
    registerBeanDefinition(def.getName(), def);
  }

  /**
   * register a bean with the given bean class
   *
   * @since 3.0
   */
  void registerBean(Class<?> beanClass) throws BeanDefinitionStoreException;

  /**
   * register a bean with the given name and bean class
   *
   * @param beanClass
   *         bean class
   *
   * @since 3.0
   */
  void registerBean(String name, Class<?> beanClass) throws BeanDefinitionStoreException;
}
