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

package cn.taketoday.context.conversion;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cn.taketoday.context.Ordered;
import cn.taketoday.context.annotation.Order;
import cn.taketoday.context.exception.ConversionException;
import cn.taketoday.context.io.Resource;
import cn.taketoday.context.utils.Assert;
import cn.taketoday.context.utils.CollectionUtils;
import cn.taketoday.context.utils.ConvertUtils;
import cn.taketoday.context.utils.ObjectUtils;
import cn.taketoday.context.utils.ReflectionUtils;
import cn.taketoday.context.utils.ResourceUtils;
import cn.taketoday.context.utils.StringUtils;

import static cn.taketoday.context.utils.OrderUtils.reversedSort;

/**
 * @author TODAY 2021/3/20 22:42
 * @since 3.0
 */
public class DefaultConversionService implements ConfigurableConversionService {
  private static DefaultConversionService sharedInstance = new DefaultConversionService();

  static {
    registerDefaultConverters(sharedInstance);
  }

  private TypeConverter[] converters;

  private ConverterTypeConverter converterTypeConverter = ConverterTypeConverter.getSharedInstance();

  public static void registerDefaultConverters(ConverterRegistry registry) {
    registry.setConverters(
            new PrimitiveClassConverter(),
            registry.getConverterTypeConverter(),
            new StringSourceEnumConverter(),
            new StringSourceResourceConverter(),
            new StringSourceConstructorConverter(),
            new BooleanConverter(),
            new ArrayToCollectionConverter(),
            new ArrayStringArrayConverter(),
            new StringSourceArrayConverter(),
            new ArraySourceToSingleConverter()//
    );
  }

  /**
   * Get Target {@link TypeConverter}
   *
   * @param source
   *         input source
   * @param targetClass
   *         convert to target class
   *
   * @return TypeConverter
   */
  @Override
  public TypeConverter getConverter(Object source, Class<?> targetClass) {
    for (TypeConverter converter : getConverters()) {
      if (converter.supports(targetClass, source)) {
        return converter;
      }
    }
    return null;
  }

  public TypeConverter[] getConverters() {
    return converters;
  }

  @Override
  public void setConverters(TypeConverter... cts) {
    Assert.notNull(cts, "TypeConverter must not be null");
    converters = reversedSort(cts);
  }

  @Override
  public boolean canConvert(Object source, Class<?> targetClass) {
    return getConverter(source, targetClass) != null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T convert(Object source, Class<T> targetClass) {
    if (source == null) {
      return convertNull(targetClass);
    }
    Assert.notNull(targetClass, "targetClass must not be null");
    if (targetClass.isInstance(source)) {
      return (T) source;
    }
    final TypeConverter typeConverter = getConverter(source, targetClass);
    if (typeConverter == null) {
      throw new ConversionException(
              "There isn't a 'cn.taketoday.context.conversion.TypeConverter' to convert: ["
                      + source + "] '" + source.getClass() + "' to target class: [" + targetClass + "]");
    }
    return (T) typeConverter.convert(targetClass, source);
  }

  protected <T> T convertNull(Class<T> targetClass) {
    return null;
  }

  /**
   * Add {@link TypeConverter} to {@link #converters}
   *
   * @param converters
   *         {@link TypeConverter} object
   *
   * @since 2.1.6
   */
  @Override
  public void addConverter(TypeConverter... converters) {
    if (ObjectUtils.isNotEmpty(converters)) {
      final List<TypeConverter> typeConverters = new ArrayList<>();
      Collections.addAll(typeConverters, converters);
      addConverter(typeConverters);
    }
  }

  /**
   * Add a list of {@link TypeConverter} to {@link #converters}
   *
   * @param converters
   *         {@link TypeConverter} object
   *
   * @since 2.1.6
   */
  @Override
  public void addConverter(List<TypeConverter> converters) {
    if (ObjectUtils.isNotEmpty(converters)) {
      if (getConverters() != null) {
        Collections.addAll(converters, getConverters());
      }
      setConverters(converters.toArray(new TypeConverter[converters.size()]));
    }
  }

  @Override
  public void addConverters(Converter<?, ?>... converters) {
    converterTypeConverter.addConverters(converters);
  }

  @Override
  public void addConverter(Converter<?, ?> converter) {
    converterTypeConverter.addConverter(converter);
  }

  @Override
  public void addConverter(Class<?> targetClass, Converter<?, ?> converter) {
    converterTypeConverter.addConverter(targetClass, converter);
  }

  @Override
  public void addConverter(Class<?> targetClass, Class<?> sourceClass, Converter<?, ?> converter) {
    converterTypeConverter.addConverter(targetClass, sourceClass, converter);
  }

  @Override
  public void setConverterTypeConverter(ConverterTypeConverter converterTypeConverter) {
    this.converterTypeConverter = converterTypeConverter;
  }

  @Override
  public ConverterTypeConverter getConverterTypeConverter() {
    return converterTypeConverter;
  }

  // static

  public static void setSharedInstance(DefaultConversionService sharedInstance) {
    DefaultConversionService.sharedInstance = sharedInstance;
  }

  public static DefaultConversionService getSharedInstance() {
    return sharedInstance;
  }

  // TypeConverter

  /**
   * @author TODAY 2019-06-06 15:50
   * @since 2.1.6
   */
  @Order(Ordered.HIGHEST_PRECEDENCE)
  static class StringSourceResourceConverter extends StringSourceTypeConverter {

    @Override
    public boolean supports(Class<?> targetClass) {
      return targetClass == Resource.class
              || targetClass == URI.class
              || targetClass == URL.class
              || targetClass == File.class
              || targetClass == Resource[].class;
    }

    @Override
    protected Object convertInternal(Class<?> targetClass, String source) {

      try {
        if (targetClass == Resource[].class) {
          return ResourceUtils.getResources(source);
        }
        final Resource resource = ResourceUtils.getResource(source);
        if (targetClass == File.class) {
          return resource.getFile();
        }
        if (targetClass == URL.class) {
          return resource.getLocation();
        }
        if (targetClass == URI.class) {
          return resource.getLocation().toURI();
        }
        return resource;
      }
      catch (Throwable e) {
        throw new ConversionException(e);
      }
    }
  }

  /**
   * @author TODAY <br>
   * 2019-06-06 15:50
   * @since 2.1.6
   */
  @Order(Ordered.HIGHEST_PRECEDENCE)
  static class StringSourceEnumConverter extends StringSourceTypeConverter {

    @Override
    public boolean supports(Class<?> targetClass) {
      return targetClass.isEnum();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object convertInternal(Class<?> targetClass, String source) {
      return Enum.valueOf((Class<Enum>) targetClass, source);
    }
  }

  /**
   * @author TODAY <br>
   * 2019-06-06 15:50
   * @since 2.1.6
   */
  @Order(Ordered.HIGHEST_PRECEDENCE)
  static class StringSourceArrayConverter extends StringSourceTypeConverter {

    @Override
    public boolean supports(Class<?> targetClass) {
      return targetClass.isArray();
    }

    @Override
    protected Object convertInternal(Class<?> targetClass, String source) {
      final Class<?> componentType = targetClass.getComponentType();

      final String[] split = StringUtils.split(source);

      final Object arrayValue = Array.newInstance(componentType, split.length);
      for (int i = 0; i < split.length; i++) {
        Array.set(arrayValue, i, ConvertUtils.convert(split[i], componentType));
      }
      return arrayValue;
    }
  }

  @Order(Ordered.HIGHEST_PRECEDENCE)
  static class ArrayToCollectionConverter implements TypeConverter {

    @Override
    public boolean supports(Class<?> targetClass, Object source) {
      return CollectionUtils.isCollection(targetClass) && source.getClass().isArray();
    }

    @Override
    public Object convert(Class<?> targetClass, Object source) {
      final int length = Array.getLength(source);
      final Collection<Object> ret = CollectionUtils.createCollection(targetClass, length);
      for (int i = 0; i < length; i++) {
        ret.add(Array.get(source, i));
      }
      return ret;
    }
  }

  /**
   * @since 3.0
   */
  @Order(Ordered.LOWEST_PRECEDENCE - Ordered.HIGHEST_PRECEDENCE)
  static class ArraySourceToSingleConverter implements TypeConverter {

    @Override
    public boolean supports(Class<?> targetClass, Object source) {
      return !targetClass.isArray() && source.getClass().isArray() && Array.getLength(source) > 0;
    }

    @Override
    public Object convert(Class<?> targetClass, Object source) {
      final Object content = Array.get(source, 0);
      return ConvertUtils.convert(content, targetClass);
    }
  }

  /**
   * @author TODAY <br>
   * 2019-07-20 00:54
   * @since 2.1.6
   */
  @Order(Ordered.HIGHEST_PRECEDENCE)
  static class ArrayStringArrayConverter implements TypeConverter {

    @Override
    public boolean supports(Class<?> targetClass, Object source) {
      return targetClass.isArray() && source.getClass().isArray();
    }

    @Override
    public Object convert(Class<?> targetClass, Object source) {
      final int length = Array.getLength(source);
      final Class<?> componentType = targetClass.getComponentType();
      final Object instance = Array.newInstance(componentType, length);
      for (int i = 0; i < length; i++) {
        final Object value = ConvertUtils.convert(Array.get(source, i), componentType);
        Array.set(instance, i, value);
      }
      return instance;
    }
  }

  /**
   * @author TODAY <br>
   * 2019-06-06 16:12
   */
  @Order(Ordered.LOWEST_PRECEDENCE)
  static class StringSourceConstructorConverter extends StringSourceTypeConverter {

    @Override
    public boolean supports(Class<?> targetClass) {
      try {
        targetClass.getDeclaredConstructor(String.class);
        return true;
      }
      catch (NoSuchMethodException e) {
        return false;
      }
    }

    @Override
    protected Object convertInternal(Class<?> targetClass, String source) {
      try {
        return ReflectionUtils.accessibleConstructor(targetClass, String.class)
                .newInstance(source);
      }
      catch (Throwable e) {
        throw new ConversionException(e);
      }
    }
  }

  /**
   * @author TODAY <br>
   * 2019-06-19 12:28
   */
  @Order(Ordered.LOWEST_PRECEDENCE)
  static class PrimitiveClassConverter implements TypeConverter {

    @Override
    public boolean supports(Class<?> targetClass, Object source) {

      if (targetClass.isArray()) {
        targetClass = targetClass.getComponentType();
      }

      return (targetClass == boolean.class && source instanceof Boolean) //
              || (targetClass == long.class && source instanceof Long)//
              || (targetClass == int.class && source instanceof Integer)//
              || (targetClass == float.class && source instanceof Float)//
              || (targetClass == short.class && source instanceof Short)//
              || (targetClass == double.class && source instanceof Double)//
              || (targetClass == char.class && source instanceof Character)//
              || (targetClass == byte.class && source instanceof Byte);
    }

    @Override
    public Object convert(Class<?> targetClass, Object source) {
      return source; // auto convert
    }
  }

}