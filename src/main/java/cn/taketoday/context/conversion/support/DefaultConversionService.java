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

package cn.taketoday.context.conversion.support;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import cn.taketoday.context.GenericDescriptor;
import cn.taketoday.context.Ordered;
import cn.taketoday.context.annotation.Order;
import cn.taketoday.context.conversion.BigDecimalConverter;
import cn.taketoday.context.conversion.ByteConverter;
import cn.taketoday.context.conversion.CharsetConverter;
import cn.taketoday.context.conversion.ClassConverter;
import cn.taketoday.context.conversion.ConfigurableConversionService;
import cn.taketoday.context.conversion.ConversionService;
import cn.taketoday.context.conversion.Converter;
import cn.taketoday.context.conversion.ConverterNotFoundException;
import cn.taketoday.context.conversion.ConverterRegistry;
import cn.taketoday.context.conversion.DataSizeConverter;
import cn.taketoday.context.conversion.DoubleConverter;
import cn.taketoday.context.conversion.DurationConverter;
import cn.taketoday.context.conversion.FloatConverter;
import cn.taketoday.context.conversion.IntegerConverter;
import cn.taketoday.context.conversion.LongConverter;
import cn.taketoday.context.conversion.MediaTypeConverter;
import cn.taketoday.context.conversion.MimeTypeConverter;
import cn.taketoday.context.conversion.ShortConverter;
import cn.taketoday.context.conversion.StringSourceTypeConverter;
import cn.taketoday.context.conversion.TypeCapable;
import cn.taketoday.context.conversion.TypeConverter;
import cn.taketoday.context.exception.ConfigurationException;
import cn.taketoday.context.exception.ConversionException;
import cn.taketoday.context.utils.Assert;
import cn.taketoday.context.utils.GenericTypeResolver;
import cn.taketoday.context.utils.Mappings;
import cn.taketoday.context.utils.ObjectUtils;
import cn.taketoday.context.utils.OrderUtils;
import cn.taketoday.context.utils.ReflectionUtils;

import static cn.taketoday.context.utils.OrderUtils.reversedSort;

/**
 * <p>Designed for direct instantiation but also exposes the static
 * {@link #addDefaultConverters(ConverterRegistry)} utility method for ad-hoc
 * use against any {@code ConverterRegistry} instance.
 *
 * @author TODAY 2021/3/20 22:42
 * @since 3.0
 */
public class DefaultConversionService implements ConfigurableConversionService {

  private static final NopTypeConverter NO_MATCH = new NopTypeConverter();

  private static DefaultConversionService sharedInstance = new DefaultConversionService();

  static {
    addDefaultConverters(sharedInstance);
  }

  private final LinkedList<TypeConverter> converters = new LinkedList<>();
  private final ConverterMappings converterMappings = new ConverterMappings();

  @Override
  public boolean canConvert(Class<?> sourceType, GenericDescriptor targetType) {
    return getConverter(sourceType, targetType) != null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T convert(final Object source, final GenericDescriptor targetType) {
    if (source == null) {
      return convertNull(targetType);
    }
    Assert.notNull(targetType, "targetType must not be null");
    if (targetType.isInstance(source)) {
      return (T) source;
    }
    final TypeConverter typeConverter = getConverter(source.getClass(), targetType);
    if (typeConverter == null) {
      return handleConverterNotFound(source, targetType);
    }
    return (T) typeConverter.convert(targetType, source);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T convert(Object source, GenericDescriptor sourceDescriptor, GenericDescriptor targetType) {
    if (source == null) {
      return convertNull(targetType);
    }
    Assert.notNull(targetType, "targetType must not be null");
    if (sourceDescriptor.equals(targetType)) {
      return (T) source;
    }
    final TypeConverter typeConverter = getConverter(sourceDescriptor.getType(), targetType);
    if (typeConverter == null) {
      return handleConverterNotFound(source, targetType);
    }
    return (T) typeConverter.convert(targetType, source);
  }

  protected <T> T handleConverterNotFound(Object source, GenericDescriptor targetType) {
    throw new ConverterNotFoundException(
            "There isn't a converter to convert: ["
                    + source + "] '" + source.getClass() + "' to target class: [" + targetType + "]",
            source, targetType);
  }

  @SuppressWarnings("unchecked")
  protected <T> T convertNull(final GenericDescriptor targetType) {
    if (targetType.is(Optional.class)) {
      return (T) Optional.empty();
    }
    return null;
  }

  /**
   * Get Target {@link TypeConverter}
   *
   * @param sourceType
   *         input sourceType
   * @param targetType
   *         convert to target class
   *
   * @return TypeConverter
   */
  @Override
  public TypeConverter getConverter(final Class<?> sourceType, final GenericDescriptor targetType) {
    final ConverterKey key = new ConverterKey(targetType, sourceType);
    final TypeConverter typeConverter = converterMappings.get(key, targetType);
    if (typeConverter != NO_MATCH) {
      return typeConverter;
    }
    return null;
  }

  class ConverterMappings extends Mappings<TypeConverter, GenericDescriptor> {

    @Override
    protected TypeConverter createValue(final Object key, final GenericDescriptor targetType) {
      final Class<?> sourceType = ((ConverterKey) key).sourceType;
      for (final TypeConverter converter : converters) {
        if (converter.supports(targetType, sourceType)) {
          return converter;
        }
      }

      return NO_MATCH;
    }
  }

  static final class ConverterKey {
    final Class<?> sourceType;
    final GenericDescriptor targetType;

    ConverterKey(GenericDescriptor targetType, Class<?> sourceType) {
      this.targetType = targetType;
      this.sourceType = sourceType;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ConverterKey)) return false;
      final ConverterKey that = (ConverterKey) o;
      return Objects.equals(targetType, that.targetType)
              && sourceType == that.sourceType;
    }

    @Override
    public int hashCode() {
      return Objects.hash(targetType, sourceType);
    }
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
  public void addConverters(final TypeConverter... converters) {
    if (ObjectUtils.isNotEmpty(converters)) {
      Collections.addAll(this.converters, converters);
      reversedSort(this.converters);
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
  public void addConverters(final List<TypeConverter> converters) {
    if (ObjectUtils.isNotEmpty(converters)) {
      this.converters.addAll(converters);
      reversedSort(this.converters);
    }
  }

  public List<TypeConverter> getConverters() {
    return converters;
  }

  @Override
  public void setConverters(final TypeConverter... converters) {
    Assert.notNull(converters, "TypeConverter must not be null");

    this.converters.clear();
    Collections.addAll(this.converters, reversedSort(converters));
  }

  @Override
  public void addConverters(final Converter<?, ?>... converters) {
    if (ObjectUtils.isNotEmpty(converters)) {
      for (final Converter<?, ?> converter : converters) {
        addConverter(converter);
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <S, T> void addConverter(final Converter<S, T> converter) {
    if (converter instanceof TypeCapable) {
      final Class<T> targetType = (Class<T>) ((TypeCapable) converter).getType();
      addConverter(targetType, converter);
    }
    else {
      Assert.notNull(converter, "converter must not be null");
      final Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(converter.getClass(), Converter.class);
      if (ObjectUtils.isNotEmpty(generics)) {
        final Class<T> targetType = (Class<T>) generics[1];
        final Class<S> sourceType = (Class<S>) generics[0];
        addConverter(targetType, sourceType, converter);
      }
      else {
        throw new ConfigurationException("can't register get converter's target class");
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <S, T> void addConverter(Class<T> targetType, Converter<? super S, ? extends T> converter) {
    Assert.notNull(converter, "converter must not be null");
    final Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(converter.getClass(), Converter.class);
    if (ObjectUtils.isNotEmpty(generics)) {
      addConverter(targetType, (Class<S>) generics[0], converter);
    }
    else
      throw new ConfigurationException("can't register get converter's source class");
  }

  @Override
  public <S, T> void addConverter(
          Class<T> targetType, Class<S> sourceType, Converter<? super S, ? extends T> converter) {

    Assert.notNull(converter, "converter must not be null");
    Assert.notNull(targetType, "targetType must not be null");
    Assert.notNull(sourceType, "sourceType must not be null");

    this.converters.add(new GenericConverter(targetType, sourceType, converter));

    // order support
    OrderUtils.reversedSort(this.converters);
  }

  // static

  public static void setSharedInstance(DefaultConversionService sharedInstance) {
    DefaultConversionService.sharedInstance = sharedInstance;
  }

  public static DefaultConversionService getSharedInstance() {
    return sharedInstance;
  }

  /**
   * Add converters appropriate for most environments.
   *
   * @param registry
   *         the registry of converters to add to
   *         (must also be castable to ConversionService, e.g. being a {@link ConfigurableConversionService})
   *
   * @throws ClassCastException
   *         if the given ConverterRegistry could not be cast to a ConversionService
   */
  public static void addDefaultConverters(ConverterRegistry registry) {
    addScalarConverters(registry);
    addCollectionConverters(registry);

    registry.addConverters(new StringToTimeZoneConverter(),
                           new ZoneIdToTimeZoneConverter(),
                           new ZonedDateTimeToCalendarConverter());

    registry.addConverters(new ByteBufferConverter((ConversionService) registry),
                           new ObjectToObjectConverter(),
                           new IdToEntityConverter((ConversionService) registry),
                           new FallbackObjectToStringConverter(),
                           new ObjectToOptionalConverter((ConversionService) registry),

                           new PrimitiveClassConverter(),
                           new StringToResourceConverter(),
                           new StringSourceConstructorConverter()

    );

  }

  /**
   * Add common collection converters.
   *
   * @param registry
   *         the registry of converters to add to
   *         (must also be castable to ConversionService, e.g. being a {@link ConfigurableConversionService})
   *
   * @throws ClassCastException
   *         if the given ConverterRegistry could not be cast to a ConversionService
   * @since 4.2.3
   */
  public static void addCollectionConverters(ConverterRegistry registry) {
    ConversionService conversionService = (ConversionService) registry;

    registry.addConverters(new ArrayToCollectionConverter(conversionService));
    registry.addConverters(new CollectionToArrayConverter(conversionService));

    registry.addConverters(new ArrayToArrayConverter(conversionService));
    registry.addConverters(new CollectionToCollectionConverter(conversionService));
    registry.addConverters(new MapToMapConverter(conversionService));

    registry.addConverters(new ArrayToStringConverter(conversionService));
    registry.addConverters(new StringToArrayConverter(conversionService));

    registry.addConverters(new ArrayToObjectConverter(conversionService));
    registry.addConverters(new ObjectToArrayConverter(conversionService));

    registry.addConverters(new CollectionToStringConverter(conversionService));
    registry.addConverters(new StringToCollectionConverter(conversionService));

    registry.addConverters(new CollectionToObjectConverter(conversionService));
    registry.addConverters(new ObjectToCollectionConverter(conversionService));

    registry.addConverters(new StreamConverter(conversionService));
  }

  private static void addScalarConverters(ConverterRegistry registry) {

    registry.addConverters(
            new IntegerConverter(int.class),
            new IntegerConverter(Integer.class),
            new LongConverter(Long.class),
            new LongConverter(long.class),
            new DoubleConverter(Double.class),
            new DoubleConverter(double.class),
            new FloatConverter(float.class),
            new FloatConverter(Float.class),
            new ByteConverter(Byte.class),
            new ByteConverter(byte.class),
            new ShortConverter(short.class),
            new ShortConverter(Short.class),
            new BigDecimalConverter(BigDecimal.class),

            new ClassConverter(),
            new CharsetConverter(),
            new DurationConverter(),
            new DataSizeConverter(),
            new MimeTypeConverter(),
            new MediaTypeConverter()

    );

    registry.addConverter(String.class, Number.class, ObjectToStringConverter.INSTANCE);

    registry.addConverter(new StringToCharacterConverter());
    registry.addConverter(String.class, Character.class, ObjectToStringConverter.INSTANCE);

    registry.addConverter(new NumberToCharacterConverter());

    registry.addConverters(new CharacterToNumberConverter((ConversionService) registry));
    registry.addConverter(new StringToBooleanConverter());
    registry.addConverter(String.class, Boolean.class, ObjectToStringConverter.INSTANCE);

    registry.addConverters(new StringToEnumConverter());
    registry.addConverter(new EnumToStringConverter());

    registry.addConverters(new IntegerToEnumConverter());
    registry.addConverter(new EnumToIntegerConverter());

    registry.addConverter(new StringToLocaleConverter());
    registry.addConverter(String.class, Locale.class, ObjectToStringConverter.INSTANCE);

    registry.addConverter(new StringToCharsetConverter());
    registry.addConverter(String.class, Charset.class, ObjectToStringConverter.INSTANCE);

    registry.addConverter(new StringToCurrencyConverter());
    registry.addConverter(String.class, Currency.class, ObjectToStringConverter.INSTANCE);

    registry.addConverter(new StringToPropertiesConverter());
    registry.addConverter(new PropertiesToStringConverter());

    registry.addConverter(new StringToUUIDConverter());
    registry.addConverter(String.class, UUID.class, ObjectToStringConverter.INSTANCE);
  }

  // TypeConverter

  /**
   * @author TODAY <br>
   * 2019-06-06 16:12
   */
  @Order(Ordered.LOWEST_PRECEDENCE)
  static class StringSourceConstructorConverter extends StringSourceTypeConverter {

    @Override
    public boolean supportsInternal(GenericDescriptor targetClass, final Class<?> source) {
      try {
        targetClass.getType().getDeclaredConstructor(String.class);
        return true;
      }
      catch (NoSuchMethodException e) {
        return false;
      }
    }

    @Override
    protected Object convertInternal(GenericDescriptor targetType, String source) {
      try {
        return ReflectionUtils.accessibleConstructor(targetType.getType(), String.class)
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
    public boolean supports(GenericDescriptor targetType, Class<?> source) {

      final Class<?> targetClass;
      if (targetType.isArray()) {
        targetClass = targetType.getComponentType();
      }
      else {
        targetClass = targetType.getType();
      }

      return (targetClass == boolean.class && source == Boolean.class) //
              || (targetClass == long.class && source == Long.class)//
              || (targetClass == int.class && source == Integer.class)//
              || (targetClass == float.class && source == Float.class)//
              || (targetClass == short.class && source == Short.class)//
              || (targetClass == double.class && source == Double.class)//
              || (targetClass == char.class && source == Character.class)//
              || (targetClass == byte.class && source == Byte.class);
    }

    @Override
    public Object convert(GenericDescriptor targetClass, Object source) {
      return source; // auto convert
    }
  }

  static class NopTypeConverter implements TypeConverter {

    @Override
    public boolean supports(GenericDescriptor targetType, Class<?> sourceType) {
      return false;
    }

    @Override
    public Object convert(GenericDescriptor targetType, Object source) {
      return null;
    }
  }

  static final class GenericConverter implements TypeConverter {
    final Class<?> targetType;
    final Class<?> sourceType;
    final Converter converter;

    GenericConverter(Class<?> targetType, Class<?> sourceType, Converter converter) {
      this.converter = converter;
      this.targetType = targetType;
      this.sourceType = sourceType;
    }

    @Override
    public boolean supports(GenericDescriptor targetType, Class<?> sourceType) {
      return targetType.is(this.targetType)
              && this.sourceType.isAssignableFrom(sourceType); // can be a sub-class
    }

    @Override
    public Object convert(GenericDescriptor targetType, Object source) {
      return converter.convert(source);
    }
  }

}