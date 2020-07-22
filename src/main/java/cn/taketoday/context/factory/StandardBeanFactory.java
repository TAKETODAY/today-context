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
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */
package cn.taketoday.context.factory;

import static cn.taketoday.context.utils.ClassUtils.getAnnotationAttributes;
import static cn.taketoday.context.utils.ClassUtils.getAnnotationAttributesArray;
import static cn.taketoday.context.utils.ContextUtils.conditional;
import static cn.taketoday.context.utils.ContextUtils.findNames;
import static cn.taketoday.context.utils.ContextUtils.resolveInitMethod;
import static cn.taketoday.context.utils.ContextUtils.resolveProps;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import cn.taketoday.context.AnnotationAttributes;
import cn.taketoday.context.BeanNameCreator;
import cn.taketoday.context.ConfigurableApplicationContext;
import cn.taketoday.context.Constant;
import cn.taketoday.context.annotation.Component;
import cn.taketoday.context.annotation.ComponentScan;
import cn.taketoday.context.annotation.Configuration;
import cn.taketoday.context.annotation.Import;
import cn.taketoday.context.annotation.MissingBean;
import cn.taketoday.context.annotation.Props;
import cn.taketoday.context.aware.ApplicationContextAware;
import cn.taketoday.context.aware.EnvironmentAware;
import cn.taketoday.context.aware.ImportAware;
import cn.taketoday.context.env.ConfigurableEnvironment;
import cn.taketoday.context.event.LoadingMissingBeanEvent;
import cn.taketoday.context.exception.BeanDefinitionStoreException;
import cn.taketoday.context.exception.BeanInitializingException;
import cn.taketoday.context.listener.ApplicationListener;
import cn.taketoday.context.loader.BeanDefinitionImporter;
import cn.taketoday.context.loader.BeanDefinitionLoader;
import cn.taketoday.context.loader.CandidateComponentScanner;
import cn.taketoday.context.loader.ImportSelector;
import cn.taketoday.context.logger.Logger;
import cn.taketoday.context.logger.LoggerFactory;
import cn.taketoday.context.utils.ClassUtils;
import cn.taketoday.context.utils.ContextUtils;
import cn.taketoday.context.utils.ExceptionUtils;
import cn.taketoday.context.utils.ObjectUtils;
import cn.taketoday.context.utils.StringUtils;

/**
 * Standard {@link BeanFactory} implementation
 *
 * @author TODAY <br>
 *         2019-03-23 15:00
 */
public class StandardBeanFactory extends AbstractBeanFactory implements ConfigurableBeanFactory, BeanDefinitionLoader {

    private static final Logger log = LoggerFactory.getLogger(StandardBeanFactory.class);

    private final ConfigurableApplicationContext applicationContext;
    private final HashSet<Method> missingMethods = new HashSet<>(32);
    private final LinkedList<AnnotatedElement> componentScanned = new LinkedList<>();

    /**
     * @since 2.1.7 Preventing repeated initialization of beans(Prevent duplicate
     *        initialization) , Prevent Cycle Dependency
     */
    private final HashSet<String> currentInitializingBeanName = new HashSet<>();

    public StandardBeanFactory(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void awareInternal(final Object bean, final BeanDefinition def) {
        super.awareInternal(bean, def);

        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(getApplicationContext());
        }

        if (bean instanceof EnvironmentAware) {
            ((EnvironmentAware) bean).setEnvironment(getApplicationContext().getEnvironment());
        }
    }

    @Override
    protected Object initializeBean(final Object bean, final BeanDefinition def) throws BeanInitializingException {
        final String name = def.getName();
        if (currentInitializingBeanName.contains(name)) {
            return bean;
        }
        currentInitializingBeanName.add(name);
        final Object initializingBean = super.initializeBean(bean, def);
        currentInitializingBeanName.remove(name);
        return initializingBean;
    }

    // -----------------------------------------

    /**
     * Resolve bean from a class which annotated with @{@link Configuration}
     */
    public void loadConfigurationBeans() {

        log.debug("Loading Configuration Beans");

        for (final Entry<String, BeanDefinition> entry : getBeanDefinitions().entrySet()) {
            if (entry.getValue().isAnnotationPresent(Configuration.class)) {
                // @Configuration bean
                loadConfigurationBeans(entry.getValue());
            }
        }
    }

    /**
     * Load {@link Configuration} beans from input bean class
     *
     * @param def
     *            current {@link Configuration} bean
     * @since 2.1.7
     */
    protected void loadConfigurationBeans(final BeanDefinition def) {

        final Collection<Method> missingMethods = this.missingMethods;
        final ConfigurableApplicationContext context = getApplicationContext();

        for (final Method method : def.getBeanClass().getDeclaredMethods()) {
            final AnnotationAttributes[] components = getAnnotationAttributesArray(method, Component.class);
            if (ObjectUtils.isEmpty(components)) {
                if (method.isAnnotationPresent(MissingBean.class) && conditional(method, context)) {
                    missingMethods.add(method);
                }
            }
            else if (conditional(method, context)) { // pass the condition
                registerConfigurationBean(def, method, components);
            }
        }
    }

    /**
     * Create {@link Configuration} bean definition, and register it
     *
     * @param method
     *            factory method
     * @param components
     *            {@link AnnotationAttributes}
     */
    protected void registerConfigurationBean(final BeanDefinition def, final Method method, final AnnotationAttributes[] components)
            throws BeanDefinitionStoreException //
    {
        final Class<?> returnType = method.getReturnType();

        final ConfigurableEnvironment environment = getApplicationContext().getEnvironment();
        final Properties properties = environment.getProperties();
        //final String defaultBeanName = beanNameCreator.create(returnType); // @Deprecated in v2.1.7, use method name instead
        final String defaultBeanName = method.getName(); // @since v2.1.7
        final String declaringBeanName = def.getName(); // @since v2.1.7

        for (final AnnotationAttributes component : components) {
            final String scope = component.getString(Constant.SCOPE);
            final String[] initMethods = component.getStringArray(Constant.INIT_METHODS);
            final String[] destroyMethods = component.getStringArray(Constant.DESTROY_METHODS);

            for (final String name : findNames(defaultBeanName, component.getStringArray(Constant.VALUE))) {

                // register
                final StandardBeanDefinition stdDef = new StandardBeanDefinition(name, returnType);

                stdDef.setScope(scope);
                stdDef.setDestroyMethods(destroyMethods);
                stdDef.setInitMethods(resolveInitMethod(initMethods, returnType));
                // fix Configuration bean shouldn't auto apply properties
                // def.setPropertyValues(ContextUtils.resolvePropertyValue(returnType)); 
                stdDef.setDeclaringName(declaringBeanName)
                        .setFactoryMethod(method);
                // resolve @Props on a bean
                stdDef.addPropertyValue(resolveProps(stdDef, properties));
                register(name, stdDef);
            }
        }
    }

    /**
     * Load missing beans, default beans
     *
     * @param beanClasses
     *            Class set
     */
    public void loadMissingBean(final Collection<Class<?>> beanClasses) {

        log.debug("Loading lost beans");

        final ConfigurableApplicationContext context = getApplicationContext();
        context.publishEvent(new LoadingMissingBeanEvent(context, beanClasses));

        for (final Class<?> beanClass : beanClasses) {

            final MissingBean missingBean = beanClass.getAnnotation(MissingBean.class);

            if (ContextUtils.isMissedBean(missingBean, beanClass, this)) {
                registerMissingBean(missingBean, new DefaultBeanDefinition(getBeanName(missingBean, beanClass), beanClass));
            }
        }

        final BeanNameCreator beanNameCreator = getBeanNameCreator();

        for (final Method method : missingMethods) {
            final MissingBean missingBean = method.getAnnotation(MissingBean.class);

            if (ContextUtils.isMissedBean(missingBean, method, this)) {

                final Class<?> beanClass = method.getReturnType();
                StandardBeanDefinition beanDefinition = // @Configuration use default bean name
                        new StandardBeanDefinition(getBeanName(missingBean, beanClass), beanClass)//
                                .setFactoryMethod(method)//
                                .setDeclaringName(beanNameCreator.create(method.getDeclaringClass()));

                if (method.isAnnotationPresent(Props.class)) {
                    // @Props on method
                    final List<PropertyValue> props = resolveProps(method, context.getEnvironment().getProperties());
                    beanDefinition.addPropertyValue(props);
                }
                registerMissingBean(missingBean, beanDefinition);
            }
        }
        missingMethods.clear();
    }

    /**
     * Register {@link MissingBean}
     *
     * @param missingBean
     *            {@link MissingBean} metadata
     * @param beanDefinition
     *            Target {@link BeanDefinition}
     */
    protected void registerMissingBean(final MissingBean missingBean, final BeanDefinition beanDefinition) {

        final Class<?> beanClass = beanDefinition.getBeanClass();

        beanDefinition.setScope(missingBean.scope())
                .setDestroyMethods(missingBean.destroyMethods())
                .setInitMethods(resolveInitMethod(missingBean.initMethods(), beanClass))
                .setPropertyValues(ContextUtils.resolvePropertyValue(beanClass));

        resolveProps(beanDefinition, getApplicationContext().getEnvironment());

        // register missed bean
        register(beanDefinition.getName(), beanDefinition);
    }

    /**
     * Get bean name
     *
     * @param missingBean
     *            {@link MissingBean}
     * @param beanClass
     *            Bean class
     * @return Bean name
     */
    protected String getBeanName(final MissingBean missingBean, final Class<?> beanClass) {
        String beanName = missingBean.value();
        if (StringUtils.isEmpty(beanName)) {
            beanName = getBeanNameCreator().create(beanClass);
        }
        return beanName;
    }

    /**
     * Resolve bean from META-INF/beans
     *
     * @since 2.1.6
     */
    public Set<Class<?>> loadMetaInfoBeans() {

        log.debug("Loading META-INF/beans");

        // Load the META-INF/beans @since 2.1.6
        // ---------------------------------------------------

        final Set<Class<?>> beans = ContextUtils.loadFromMetaInfo("META-INF/beans");

        final BeanNameCreator beanNameCreator = getBeanNameCreator();
        for (final Class<?> beanClass : beans) {

            if (conditional(beanClass) && !beanClass.isAnnotationPresent(MissingBean.class)) {
                // can't be a missed bean. MissingBean load after normal loading beans
                ContextUtils.createBeanDefinitions(beanNameCreator.create(beanClass), beanClass)
                        .forEach(this::register);
            }
        }
        return beans;
    }

    /**
     * Load {@link Import} beans from input bean classes
     *
     * @param beans
     *            Input bean classes
     * @since 2.1.7
     */
    public void importBeans(Class<?>... beans) {
        for (final Class<?> bean : requireNonNull(beans)) {
            importBeans(createBeanDefinition(bean));
        }
    }

    /**
     * Load {@link Import} beans from input {@link BeanDefinition}s
     *
     * @param defs
     *            Input {@link BeanDefinition}s
     * @since 2.1.7
     */
    public void importBeans(final Set<BeanDefinition> defs) {

        for (final BeanDefinition def : defs) {
            importBeans(def);
        }
    }

    /**
     * Load {@link Import} beans from input {@link BeanDefinition}
     *
     * @param def
     *            Input {@link BeanDefinition}
     * @since 2.1.7
     */
    public void importBeans(final BeanDefinition def) {

        for (final AnnotationAttributes attr : getAnnotationAttributesArray(def, Import.class)) {
            for (final Class<?> importClass : attr.getAttribute(Constant.VALUE, Class[].class)) {
                if (!containsBeanDefinition(importClass, true)) {
                    selectImport(def, importClass);
                }
            }
        }
    }

    /**
     * Select import
     *
     * @since 2.1.7
     */
    protected void selectImport(final BeanDefinition def, final Class<?> importClass) {
        log.debug("Importing: [{}]", importClass);

        BeanDefinition importDef = createBeanDefinition(importClass);
        register(importDef);
        loadConfigurationBeans(importDef); // scan config bean
        if (ImportSelector.class.isAssignableFrom(importClass)) {
            for (final String select : createImporter(importDef, ImportSelector.class).selectImports(def)) {
                register(createBeanDefinition(ClassUtils.loadClass(select)));
            }
        }
        if (BeanDefinitionImporter.class.isAssignableFrom(importClass)) {
            createImporter(importDef, BeanDefinitionImporter.class).registerBeanDefinitions(def, this);
        }
        if (ApplicationListener.class.isAssignableFrom(importClass)) {
            getApplicationContext().addApplicationListener(createImporter(importDef, ApplicationListener.class));
        }
    }

    /**
     * Create {@link ImportSelector} ,or {@link BeanDefinitionImporter},
     * {@link ApplicationListener} object
     *
     * @param target
     *            Must be {@link ImportSelector} ,or {@link BeanDefinitionImporter}
     * @return {@link ImportSelector} object
     */
    protected <T> T createImporter(BeanDefinition importDef, Class<T> target) {
        try {
            final Object bean = getBean(importDef);
            if (bean instanceof ImportAware) {
                ((ImportAware) bean).setImportBeanDefinition(importDef);
            }
            return target.cast(bean);
        }
        catch (Throwable e) {
            throw new BeanDefinitionStoreException("Can't initialize a target: [" + importDef + "]");
        }
    }

    @Override
    protected BeanNameCreator createBeanNameCreator() {
        return getApplicationContext().getEnvironment().getBeanNameCreator();
    }

    @Override
    public final BeanDefinitionLoader getBeanDefinitionLoader() {
        return this;
    }

    // BeanDefinitionLoader @since 2.1.7
    // ---------------------------------------------

    @Override
    public void loadBeanDefinition(final Class<?> candidate) throws BeanDefinitionStoreException {

        // don't load abstract class
        if (!Modifier.isAbstract(candidate.getModifiers()) && conditional(candidate, applicationContext)) {
            register(candidate);
        }
    }

    @Override
    public void loadBeanDefinitions(final Collection<Class<?>> beans) throws BeanDefinitionStoreException {
        for (Class<?> clazz : beans) {
            loadBeanDefinition(clazz);
        }
    }

    @Override
    public void loadBeanDefinition(final String name, final Class<?> beanClass) throws BeanDefinitionStoreException {

        final AnnotationAttributes[] annotationAttributes = getAnnotationAttributesArray(beanClass, Component.class);

        if (ObjectUtils.isEmpty(annotationAttributes)) {
            register(name, build(name, beanClass, null));
        }
        else {
            for (final AnnotationAttributes attributes : annotationAttributes) {
                register(name, build(name, beanClass, attributes));
            }
        }
    }

    @Override
    public void loadBeanDefinition(final String... locations) throws BeanDefinitionStoreException {
        loadBeanDefinitions(new CandidateComponentScanner().scan(locations));
    }

    @Override
    public void register(final Class<?> candidate) throws BeanDefinitionStoreException {
        final AnnotationAttributes[] annotationAttributes = getAnnotationAttributesArray(candidate, Component.class);
        if (ObjectUtils.isNotEmpty(annotationAttributes)) {
            final String defaultBeanName = getBeanNameCreator().create(candidate);
            for (final AnnotationAttributes attributes : annotationAttributes) {
                for (final String name : findNames(defaultBeanName, attributes.getStringArray(Constant.VALUE))) {
                    register(name, build(name, candidate, attributes));
                }
            }
        }
    }

    /**
     * Build a bean definition
     *
     * @param beanClass
     *            Given bean class
     * @param attributes
     *            {@link AnnotationAttributes}
     * @param beanName
     *            Bean name
     * @return A default {@link BeanDefinition}
     */
    protected BeanDefinition build(final String beanName,
                                   final Class<?> beanClass,
                                   final AnnotationAttributes attributes) {
        return ContextUtils.createBeanDefinition(beanName, beanClass, attributes, applicationContext);
    }

    /**
     * Register bean definition with given name
     *
     * @param name
     *            Bean name
     * @param def
     *            Bean definition
     * @throws BeanDefinitionStoreException
     *             If can't store bean
     */
    @Override
    public void register(final String name, final BeanDefinition def) throws BeanDefinitionStoreException {

        ContextUtils.validateBeanDefinition(def);

        String nameToUse = name;
        final Class<?> beanClass = def.getBeanClass();

        try {
            if (containsBeanDefinition(name)) {
                final BeanDefinition existBeanDefinition = getBeanDefinition(name);
                Class<?> existClass = existBeanDefinition.getBeanClass();
                log.info("=====================|START|=====================");
                log.info("There is already a bean called: [{}], its bean class: [{}].", name, existClass);

                if (beanClass.equals(existClass)) {
                    log.warn("They have same bean class: [{}]. We will override it.", beanClass);
                }
                else {
                    nameToUse = beanClass.getName();
                    def.setName(nameToUse);
                    log.warn("Current bean class: [{}]. You are supposed to change your bean name creater or bean name.", beanClass);
                    log.warn("Current bean definition: [{}] will be registed as: [{}].", def, nameToUse);
                }
                log.info("======================|END|======================");
            }

            if (FactoryBean.class.isAssignableFrom(beanClass)) { // process FactoryBean
                registerFactoryBean(nameToUse, def);
            }
            else {
                registerBeanDefinition(nameToUse, def);
            }
            postProcessRegisterBeanDefinition(def);
        }
        catch (Throwable ex) {
            ex = ExceptionUtils.unwrapThrowable(ex);
            throw new BeanDefinitionStoreException("An Exception Occurred When Register Bean Definition: [" + //
                    name + "], With Msg: [" + ex + "]", ex);
        }
    }

    /**
     * Process after register {@link BeanDefinition}
     *
     * @param targetDef
     *            Target {@link BeanDefinition}
     */
    protected void postProcessRegisterBeanDefinition(final BeanDefinition targetDef) throws Throwable {
        if (targetDef.isAnnotationPresent(Import.class)) { // @since 2.1.7
            importBeans(targetDef);
        }
        if (targetDef.isAnnotationPresent(ComponentScan.class)) {
            componentScan(targetDef);
        }
        // application listener @since 2.1.7
        if (ApplicationListener.class.isAssignableFrom(targetDef.getBeanClass())) {
            Object listener = getSingleton(targetDef.getName());
            if (listener == null) {
                listener = createBeanIfNecessary(targetDef);
                applicationContext.addApplicationListener((ApplicationListener<?>) listener);
            }
        }
    }

    /**
     * Import beans from given package locations
     *
     * @param source
     *            {@link BeanDefinition} that annotated {@link ComponentScan}
     */
    protected void componentScan(final AnnotatedElement source) {
        if (!componentScanned.contains(source)) {
            componentScanned.add(source);
            for (final AnnotationAttributes attribute : getAnnotationAttributesArray(source, ComponentScan.class)) {
                loadBeanDefinition(attribute.getStringArray(Constant.VALUE));
            }
        }
    }

    /**
     * Register {@link FactoryBeanDefinition} to the {@link BeanFactory}
     *
     * @param oldBeanName
     *            Target old bean name
     * @param factoryDef
     *            {@link FactoryBean} Bean definition
     * @throws Throwable
     *             If any {@link Throwable} occurred
     */
    protected void registerFactoryBean(final String oldBeanName, final BeanDefinition factoryDef) throws Throwable {

        final FactoryBeanDefinition<?> def =
                factoryDef instanceof FactoryBeanDefinition
                        ? (FactoryBeanDefinition<?>) factoryDef
                        : new FactoryBeanDefinition<>(factoryDef, this);

        registerBeanDefinition(oldBeanName, def);
    }

    @Override
    public BeanDefinitionRegistry getRegistry() {
        return this;
    }

    @Override
    public BeanDefinition createBeanDefinition(final Class<?> beanClass) {
        return build(getBeanNameCreator().create(beanClass), beanClass,
                     getAnnotationAttributes(Component.class, beanClass));
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
