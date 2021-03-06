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

import cn.taketoday.context.conversion.TypeConverter;
import cn.taketoday.context.utils.ClassUtils;
import cn.taketoday.context.utils.GenericDescriptor;

/**
 * @author TODAY 2021/3/22 16:43
 * @since 3.0
 */
final class IntegerToEnumConverter implements TypeConverter {

  @Override
  public boolean supports(final GenericDescriptor targetType, final Class<?> sourceType) {
    return sourceType == Integer.class
            && Enum.class.isAssignableFrom(targetType.getType());
  }

  @Override
  public Object convert(final GenericDescriptor targetType, final Object source) {
    return ClassUtils.getEnumType(targetType.getType()).getEnumConstants()[(int) source];
  }
}
