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

import java.io.File;
import java.net.URI;
import java.net.URL;

import cn.taketoday.context.conversion.ConversionFailedException;
import cn.taketoday.context.io.Resource;
import cn.taketoday.context.utils.GenericDescriptor;
import cn.taketoday.context.utils.ResourceUtils;

/**
 * @author TODAY 2021/3/22 17:43
 * @since 2.1.6
 */
final class StringToResourceConverter extends StringSourceTypeConverter {

  @Override
  public boolean supportsInternal(final GenericDescriptor targetType, final Class<?> source) {
    final Class<?> targetClass = targetType.getType();
    return targetClass == Resource.class
            || targetClass == URI.class
            || targetClass == URL.class
            || targetClass == File.class
            || targetClass == Resource[].class;
  }

  @Override
  protected Object convertInternal(GenericDescriptor targetType, String source) {
    final Class<?> targetClass = targetType.getType();
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
      throw new ConversionFailedException(e, source, targetType);
    }
  }
}
