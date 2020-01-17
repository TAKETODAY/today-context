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
package cn.taketoday.context.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author TODAY <br>
 *         2019-12-29 23:39
 */
public abstract class CollectionUtils {

    /**
     * Create a hash set
     * 
     * @param elements
     *            Elements instance
     */
    @SafeVarargs
    public static <E> Set<E> newHashSet(E... elements) {
        return new HashSet<E>(Arrays.asList(elements));
    }

    public static <T> List<T> enumerationToList(final Enumeration<T> objs) {
        if (objs == null) {
            return Collections.emptyList();
        }
        final List<T> ret = new ArrayList<>();
        while (objs.hasMoreElements()) {
            ret.add(objs.nextElement());
        }
        return ret.isEmpty() ? Collections.emptyList() : ret;
    }

}