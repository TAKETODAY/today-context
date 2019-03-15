/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2019 All Rights Reserved.
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

import java.util.Set;

import cn.taketoday.context.bean.BeanDefinition;
import cn.taketoday.context.event.ObjectRefreshedEvent;
import cn.taketoday.context.exception.BeanDefinitionStoreException;
import cn.taketoday.context.exception.NoSuchBeanDefinitionException;

/**
 * @author Today <br>
 * 
 *         2018-11-14 19:40
 */
public interface ConfigurableBeanFactory extends BeanFactory, SingletonBeanRegistry, BeanDefinitionRegistry {

	/**
	 * Register a bean with the given name and bean definition
	 * 
	 * @param beanDefinition
	 *            bean definition
	 * @throws BeanDefinitionStoreException
	 * @since 1.2.0
	 */
	void registerBean(String name, BeanDefinition beanDefinition) throws BeanDefinitionStoreException;

	/**
	 * Remove bean with the given name
	 * 
	 * @param name
	 *            bean name
	 * @throws NoSuchBeanDefinitionException
	 */
	void removeBean(String name) throws BeanDefinitionStoreException;

	/**
	 * Register a bean with the given name and type
	 * 
	 * @param name
	 *            bean name
	 * @param clazz
	 *            bean class
	 * @throws BeanDefinitionStoreException
	 */
	void registerBean(String name, Class<?> clazz) throws BeanDefinitionStoreException;

	/**
	 * Register a bean with the given type
	 * 
	 * @param clazz
	 *            bean class
	 * @throws BeanDefinitionStoreException
	 */
	void registerBean(Class<?> clazz) throws BeanDefinitionStoreException;

	/**
	 * Register a bean with the given types
	 * 
	 * @param classes
	 *            bean classes
	 * @throws BeanDefinitionStoreException
	 */
	void registerBean(Set<Class<?>> classes) throws BeanDefinitionStoreException;

	/**
	 * Destroy bean with given name
	 * 
	 * @param name
	 *            the bean name
	 * @since 2.1.0
	 */
	void destroyBean(String name);

	/**
	 * Refresh bean with given name, and publish {@link ObjectRefreshedEvent}.
	 * 
	 * @param name
	 *            bean name
	 * @since 1.2.0
	 */
	void refresh(String name);

	/**
	 * Refresh bean definition, and publish {@link ObjectRefreshedEvent}.
	 * 
	 * @param beanDefinition
	 *            bean definition
	 * @since 2.0.0
	 * @return initialized object
	 */
	Object refresh(BeanDefinition beanDefinition);

	/**
	 * Initialize singletons
	 * 
	 * @throws Throwable
	 *             when could not initialize singletons
	 * @since 2.1.2
	 */
	void initializeSingletons() throws Throwable;

	/**
	 * Add a {@link BeanPostProcessor}
	 * 
	 * @param beanPostProcessor
	 *            bean post processor instance
	 * @since 2.1.2
	 */
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * Remove a {@link BeanPostProcessor}
	 * 
	 * @param beanPostProcessor
	 *            bean post processor instance
	 * @since 2.1.2
	 */
	void removeBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * Refresh a bean which has already in context
	 * 
	 * @param previousClass
	 *            Previous Class
	 * @param currentClass
	 *            Swap {@link Class}
	 * @since 2.1.6
	 */
	void refresh(Class<?> previousClass, Class<?> currentClass);

}