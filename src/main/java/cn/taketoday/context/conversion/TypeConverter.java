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

import cn.taketoday.context.exception.ConversionException;
import cn.taketoday.context.utils.GenericDescriptor;

/**
 * Type converter
 *
 * @author TODAY 2019-06-06 14:17
 * @since 2.1.6
 */
public interface TypeConverter {

  /**
   * whether this {@link TypeConverter} supports to convert source object to
   * target class object
   *
   * @param targetType
   *         target class
   * @param sourceType
   *         source object never be null
   *
   * @return whether this {@link TypeConverter} supports to convert source object
   * to target class object
   */
//  boolean supports(Class<?> targetType, Class<?> sourceType);

  boolean supports(GenericDescriptor targetType, Class<?> sourceType);

  /**
   * Convert source object to target object
   *
   * @param targetType
   *         target type
   * @param source
   *         source object never be null
   *
   * @return a converted object
   *
   * @throws ConversionException
   *         if can't convert to target object
   */
//  Object convert(Class<?> targetType, Object source);

  Object convert(GenericDescriptor targetType, Object source);

}
