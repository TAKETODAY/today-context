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

import java.lang.reflect.Array;

import cn.taketoday.context.conversion.ConversionService;
import cn.taketoday.context.conversion.TypeConverter;
import cn.taketoday.context.utils.GenericDescriptor;

/**
 * Converts an array to another array.
 *
 * @author Keith Donald
 * @author Phillip Webb
 * @author TODAY
 * @since 3.0
 */
final class ArrayToArrayConverter extends ArraySourceConverter implements TypeConverter {

  private final ConversionService conversionService;

  public ArrayToArrayConverter(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @Override
  protected boolean supportsInternal(final GenericDescriptor targetType, Class<?> sourceType) {
    return targetType.isArray();
  }

  @Override
  public Object convert(final GenericDescriptor targetType, final Object source) {
    final Class<?> elementType = targetType.getComponentType();
    final int length = Array.getLength(source);
    final Object array = Array.newInstance(elementType, length);
    final ConversionService conversionService = this.conversionService;
    for (int i = 0; i < length; i++) {
      Object sourceElement = Array.get(source, i);
      Object targetElement = conversionService.convert(sourceElement, elementType);
      Array.set(array, i, targetElement);
    }

    return array;
  }

}
