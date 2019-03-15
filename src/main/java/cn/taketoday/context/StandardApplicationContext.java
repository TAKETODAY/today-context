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
package cn.taketoday.context;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import cn.taketoday.context.annotation.Component;
import cn.taketoday.context.annotation.Configuration;
import cn.taketoday.context.annotation.MissingBean;
import cn.taketoday.context.annotation.Props;
import cn.taketoday.context.aware.ApplicationContextAware;
import cn.taketoday.context.aware.Aware;
import cn.taketoday.context.aware.BeanFactoryAware;
import cn.taketoday.context.aware.BeanNameAware;
import cn.taketoday.context.aware.EnvironmentAware;
import cn.taketoday.context.bean.BeanDefinition;
import cn.taketoday.context.bean.DefaultBeanDefinition;
import cn.taketoday.context.bean.PropertyValue;
import cn.taketoday.context.bean.StandardBeanDefinition;
import cn.taketoday.context.event.LoadingMissingBeanEvent;
import cn.taketoday.context.exception.BeanDefinitionStoreException;
import cn.taketoday.context.exception.ContextException;
import cn.taketoday.context.factory.AbstractBeanFactory;
import cn.taketoday.context.factory.ConfigurableBeanFactory;
import cn.taketoday.context.loader.BeanDefinitionLoader;
import cn.taketoday.context.utils.ClassUtils;
import cn.taketoday.context.utils.ContextUtils;
import cn.taketoday.context.utils.StringUtils;

/**
 * Standard {@link ApplicationContext}
 * 
 * @author Today <br>
 *         <p>
 *         2018-09-06 13:47
 */
public class StandardApplicationContext extends AbstractApplicationContext implements ConfigurableApplicationContext {

	private final StandardBeanFactory beanFactory;

	/**
	 * Start with given class set
	 *
	 * @param classes
	 *            class set
	 */
	public StandardApplicationContext(Collection<Class<?>> classes) {
		this(Constant.BLANK);
		loadContext(classes);
	}

	/**
	 * Set given properties location
	 * 
	 * @param propertiesLocation
	 *            a file or a directory to scan
	 */
	public StandardApplicationContext(String propertiesLocation) {
		this();
		if (StringUtils.isNotEmpty(propertiesLocation)) {
			setPropertiesLocation(propertiesLocation);
		}
	}

	/**
	 * Start context with given properties location and base scan packages
	 * 
	 * @param propertiesLocation
	 *            a file or a directory contains
	 * @param locations
	 *            scan classes from packages
	 */
	public StandardApplicationContext(String propertiesLocation, String... locations) {
		this(propertiesLocation);
		loadContext(locations);
	}

	public StandardApplicationContext() {
		this.beanFactory = new StandardBeanFactory();
	}

	@Override
	public AbstractBeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	@Override
	protected void doLoadBeanDefinitions(AbstractBeanFactory beanFactory, Collection<Class<?>> beanClasses) {
		super.doLoadBeanDefinitions(beanFactory, beanClasses);

		this.beanFactory.loadConfigurationBeans();
		this.beanFactory.loadMissingBean(beanClasses);
	}

	/**
	 * @author TODAY <br>
	 */
	public class StandardBeanFactory extends AbstractBeanFactory implements ConfigurableBeanFactory {

		private final Collection<Method> missingMethods = new HashSet<>(32, 1.0f);

		/** resolve beanDefinition */
		private BeanDefinitionLoader beanDefinitionLoader;

		@Override
		protected void aware(Object bean, String name) {

			if (bean instanceof Aware) {
				// aware
				if (bean instanceof BeanNameAware) {
					((BeanNameAware) bean).setBeanName(name);
				}
				if (bean instanceof ApplicationContextAware) {
					((ApplicationContextAware) bean).setApplicationContext(StandardApplicationContext.this);
				}
				if (bean instanceof BeanFactoryAware) {
					((BeanFactoryAware) bean).setBeanFactory(this);
				}
				if (bean instanceof EnvironmentAware) {
					((EnvironmentAware) bean).setEnvironment(getEnvironment());
				}
			}
		}

		/**
		 * If {@link BeanDefinition} is {@link StandardBeanDefinition} will create bean
		 * from {@link StandardBeanDefinition#getFactoryMethod()}
		 */
		@Override
		protected Object createBeanInstance(BeanDefinition beanDefinition) throws Throwable {
			final Object bean = getSingleton(beanDefinition.getName());

			if (bean == null) {
				if (beanDefinition instanceof StandardBeanDefinition) {
					final StandardBeanDefinition standardBeanDefinition = (StandardBeanDefinition) beanDefinition;
					final Method factoryMethod = standardBeanDefinition.getFactoryMethod();

					return factoryMethod.invoke(getDeclaringInstance(standardBeanDefinition.getDeclaringName()), //
							ContextUtils.resolveParameter(factoryMethod, this));
				}
				return ClassUtils.newInstance(beanDefinition, this);
			}
			return bean;
		}

		// -----------------------------------------

		/**
		 * Get declaring instance
		 * 
		 * @param declaringName
		 *            declaring name
		 * @return
		 * @throws Throwable
		 */
		private Object getDeclaringInstance(String declaringName) throws Throwable {
			BeanDefinition beanDefinition = getBeanDefinition(declaringName);

			if (beanDefinition.isInitialized()) {
				return getSingleton(declaringName);
			}

			// fix: declaring bean not initialized
			final Object declaringSingleton = super.initializingBean(//
					createBeanInstance(beanDefinition), declaringName, beanDefinition//
			);

			// put declaring object
			if (beanDefinition.isSingleton()) {
				registerSingleton(declaringName, declaringSingleton);
				beanDefinition.setInitialized(true);
			}
			return declaringSingleton;
		}

		/**
		 * Resolve bean from a class which annotated with @{@link Configuration}
		 * 
		 * @throws Throwable
		 *             when exception occurred
		 */
		private void loadConfigurationBeans() {

			for (Entry<String, BeanDefinition> entry : getBeanDefinitionsMap().entrySet()) {

				final BeanDefinition beanDefinition = entry.getValue();

				final Class<? extends Object> beanClass = beanDefinition.getBeanClass();
				if (!beanClass.isAnnotationPresent(Configuration.class)) {
					continue;
				}

				for (Method method : beanClass.getDeclaredMethods()) {

					if (!ContextUtils.conditional(method, StandardApplicationContext.this)) {
						continue;
					}

					Collection<AnnotationAttributes> components = ClassUtils.getAnnotationAttributes(method, Component.class);

					if (components.isEmpty()) {
						if (method.isAnnotationPresent(MissingBean.class)) {
							missingMethods.add(method);
						}
						continue;
					}
					doRegisterDefinition(method, components);
				}
			}
		}

		/**
		 * Create bean definition, and register it
		 *
		 * @param method
		 * @param components
		 *            {@link AnnotationAttributes}
		 */
		private void doRegisterDefinition(Method method, Collection<AnnotationAttributes> components) throws BeanDefinitionStoreException {

			final AbstractBeanFactory beanFactory = getBeanFactory();

			final Class<?> returnType = method.getReturnType();
			final BeanNameCreator beanNameCreator = beanFactory.getBeanNameCreator();
			final BeanDefinitionLoader beanDefinitionLoader = beanFactory.getBeanDefinitionLoader();

			final String defaultBeanName = beanNameCreator.create(returnType);
			final String declaringBeanName = beanNameCreator.create(method.getDeclaringClass());

			for (final AnnotationAttributes component : components) {
				final Scope scope = component.getEnum(Constant.SCOPE);
				final String[] initMethods = component.getStringArray(Constant.INIT_METHODS);
				final String[] destroyMethods = component.getStringArray(Constant.DESTROY_METHODS);

				for (final String name : ContextUtils.findNames(defaultBeanName, component.getStringArray(Constant.VALUE))) {

					// register
					StandardBeanDefinition beanDefinition = new StandardBeanDefinition();
					beanDefinition.setName(name);//
					beanDefinition.setScope(scope);
					beanDefinition.setBeanClass(returnType);//
					beanDefinition.setDestroyMethods(destroyMethods);//
					beanDefinition.setInitMethods(ContextUtils.resolveInitMethod(returnType, initMethods));//
					beanDefinition.setPropertyValues(ContextUtils.resolvePropertyValue(returnType, StandardApplicationContext.this));

					beanDefinition.setDeclaringName(declaringBeanName)//
							.setFactoryMethod(method);

					ContextUtils.resolveProps(beanDefinition, getEnvironment());

					beanDefinitionLoader.register(name, beanDefinition);
				}
			}
		}

		/**
		 * Load missing beans, default beans
		 * 
		 * @param beanClasses
		 */
		private void loadMissingBean(Collection<Class<?>> beanClasses) {

			publishEvent(new LoadingMissingBeanEvent(StandardApplicationContext.this, beanClasses));

			final BeanNameCreator beanNameCreator = getBeanNameCreator();
			final BeanDefinitionLoader beanDefinitionLoader = getBeanDefinitionLoader();

			for (final Class<?> beanClass : beanClasses) {

				final MissingBean missingBean = beanClass.getAnnotation(MissingBean.class);

				if (isMissedBean(missingBean, beanClass)) {
					registerMissingBean(beanDefinitionLoader, beanNameCreator, missingBean, beanClass, new DefaultBeanDefinition());
				}
			}

			for (final Method method : missingMethods) {

				final Class<?> beanClass = method.getReturnType();
				final MissingBean missingBean = method.getAnnotation(MissingBean.class);

				if (isMissedBean(missingBean, beanClass)) {

					// @Configuration use default bean name
					StandardBeanDefinition beanDefinition = new StandardBeanDefinition()//
							.setFactoryMethod(method)//
							.setDeclaringName(beanNameCreator.create(method.getDeclaringClass()));

					if (method.isAnnotationPresent(Props.class)) {
						// @Props on method
						final List<PropertyValue> resolvedProps = //
								ContextUtils.resolveProps(method, getEnvironment().getProperties());

						if (!resolvedProps.isEmpty()) {
							beanDefinition.addPropertyValue(resolvedProps);
						}
					}
					registerMissingBean(beanDefinitionLoader, beanNameCreator, missingBean, beanClass, beanDefinition);
				}
			}
			missingMethods.clear();
		}

		private boolean isMissedBean(final MissingBean missingBean, final Class<?> beanClass) {

			if (missingBean == null) {
				return false;
			}

			final String beanName = missingBean.value();
			if (StringUtils.isNotEmpty(beanName) && containsBeanDefinition(beanName)) {
				return false;
			}
			final Class<?> type = missingBean.type();

			if ((type != void.class && containsBeanDefinition(type, true)) || containsBeanDefinition(beanClass)) {
				// not default type
				return false;
			}
			return true;
		}

		void registerMissingBean(BeanDefinitionLoader beanDefinitionLoader, BeanNameCreator beanNameCreator, //
				final MissingBean missingBean, Class<?> beanClass, final BeanDefinition beanDefinition) //
		{
			String beanName = missingBean.value();
			if (StringUtils.isEmpty(beanName)) {
				beanName = beanNameCreator.create(beanClass);
			}

			beanDefinition.setName(beanName);
			beanDefinition.setBeanClass(beanClass)//
					.setScope(missingBean.scope())//
					.setDestroyMethods(missingBean.destroyMethods())//
					.setInitMethods(ContextUtils.resolveInitMethod(beanClass, missingBean.initMethods()))//
					.setPropertyValues(ContextUtils.resolvePropertyValue(beanClass, StandardApplicationContext.this));

			ContextUtils.resolveProps(beanDefinition, getEnvironment());

			// register missed bean
			beanDefinitionLoader.register(beanName, beanDefinition);
		}

		@Override
		public BeanDefinitionLoader getBeanDefinitionLoader() {
			if (beanDefinitionLoader == null) {
				try {
					// fix: when manually load context some properties can't be loaded
					// not initialize
					loadContext(new HashSet<>());
				}
				catch (Throwable e) {
					throw new ContextException(e);
				}
			}
			return beanDefinitionLoader;
		}

		public void setBeanDefinitionLoader(BeanDefinitionLoader beanDefinitionLoader) {
			this.beanDefinitionLoader = beanDefinitionLoader;
		}

	}

}