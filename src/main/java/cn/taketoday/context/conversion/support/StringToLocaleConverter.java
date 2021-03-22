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

import java.util.Locale;

import cn.taketoday.context.conversion.Converter;
import cn.taketoday.context.utils.StringUtils;

/**
 * Converts from a String to a {@link Locale}.
 *
 * <p>Accepts the classic {@link Locale} String format ({@link Locale#toString()})
 * as well as BCP 47 language tags ({@link Locale#forLanguageTag} on Java 7+).
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author TODAY
 * @since 3.0
 */
final class StringToLocaleConverter implements Converter<String, Locale> {

  @Override
  public Locale convert(String source) {
    if (source.isEmpty()) {
      return null;
    }

    final String[] items = source.split("_");
    if (items.length == 1) {
      return new Locale(items[0]);
    }
    if (items.length == 2) {
      return new Locale(items[0], items[1]);
    }
    return new Locale(items[0], items[1], items[2]);
  }

}
