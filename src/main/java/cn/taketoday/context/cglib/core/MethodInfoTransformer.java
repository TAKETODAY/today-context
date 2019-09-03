/*
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.taketoday.context.cglib.core;

import java.lang.reflect.Member;

@SuppressWarnings("all")
public class MethodInfoTransformer implements Transformer {

    private static final MethodInfoTransformer INSTANCE = new MethodInfoTransformer();

    public static MethodInfoTransformer getInstance() {
        return INSTANCE;
    }

    public Object transform(Object value) {
        if (value instanceof Member) {
            return ReflectUtils.getMethodInfo((Member) value);
        }
        throw new IllegalArgumentException("cannot get method info for " + value);
    }
}