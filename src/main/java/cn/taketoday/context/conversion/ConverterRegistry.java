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

import java.util.List;

/**
 * @author TODAY 2021/3/21 17:55
 * @since 3.0
 */
public interface ConverterRegistry {

  void setConverters(TypeConverter... cts);

  /**
   * Add {@link TypeConverter}s
   *
   * @param converters
   *         {@link TypeConverter} object
   */
  void addConverter(TypeConverter... converters);

  /**
   * Add a list of {@link TypeConverter}
   *
   * @param converters
   *         {@link TypeConverter} object
   */
  void addConverter(List<TypeConverter> converters);

  // Converter

  void addConverter(Converter<?, ?> converter);

  void addConverters(Converter<?, ?>... converters);

  void addConverter(Class<?> targetClass, Converter<?, ?> converter);

  void addConverter(Class<?> targetClass, Class<?> sourceClass, Converter<?, ?> converter);

  void setConverterTypeConverter(ConverterTypeConverter converterTypeConverter);

  ConverterTypeConverter getConverterTypeConverter();
}