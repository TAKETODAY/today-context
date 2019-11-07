/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2019 All Rights Reserved.
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
package cn.taketoday.context.loader;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Properties;

import cn.taketoday.context.Ordered;
import cn.taketoday.context.annotation.DefaultProps;
import cn.taketoday.context.annotation.Props;
import cn.taketoday.context.factory.BeanFactory;
import cn.taketoday.context.utils.ClassUtils;
import cn.taketoday.context.utils.ContextUtils;

/**
 * Resolve {@link Map}
 * 
 * @author TODAY <br>
 *         2019-10-28 20:27
 */
public class MapParameterResolver implements ExecutableParameterResolver, Ordered {

    @Override
    public boolean supports(Parameter parameter) {
        return Map.class.isAssignableFrom(parameter.getType());
    }

    @Override
    public Object resolve(Parameter parameter, BeanFactory beanFactory) {
        Props props = parameter.getAnnotation(Props.class);

        if (props == null) {
            props = new DefaultProps();
        }

        final Properties loadProps = ContextUtils.loadProps(props, System.getProperties());
        final Class<?> type = parameter.getType();
        
        if (type.isInterface()) { // extends or implements Map
            return loadProps;
        }
        
        @SuppressWarnings("unchecked")
        final Map<Object, Object> ret = (Map<Object, Object>) ClassUtils.newInstance(type);

        ret.putAll(loadProps);
        
        return ret;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}