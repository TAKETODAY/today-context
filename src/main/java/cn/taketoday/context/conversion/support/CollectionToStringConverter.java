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

import java.util.Collection;
import java.util.StringJoiner;

import cn.taketoday.context.Constant;
import cn.taketoday.context.conversion.ConversionService;
import cn.taketoday.context.utils.GenericDescriptor;

/**
 * Converts a Collection to a comma-delimited String.
 *
 * @author Keith Donald
 * @author TODAY
 * @since 3.0
 */
final class CollectionToStringConverter extends CollectionSourceConverter {
  private static final String DELIMITER = ",";
  private final ConversionService conversionService;

  public CollectionToStringConverter(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @Override
  protected boolean supportsInternal(GenericDescriptor targetType, Class<?> sourceType) {
    // Collection.class -> String.class
    return targetType.is(String.class);
  }

  @Override
  protected Object convertInternal(final GenericDescriptor targetType, final Collection<?> sourceCollection) {
    if (sourceCollection.isEmpty()) {
      return Constant.BLANK;
    }
    final StringJoiner sj = new StringJoiner(DELIMITER);
    final ConversionService conversionService = this.conversionService;
    for (final Object sourceElement : sourceCollection) {
      Object targetElement = conversionService.convert(sourceElement, targetType);
      sj.add(String.valueOf(targetElement));
    }
    return sj.toString();
  }

}
