/*
 * Copyright 2003 The Apache Software Foundation
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

import cn.taketoday.context.asm.Type;

/**
 * Customizes key types for {@link KeyFactory} when building equals, hashCode,
 * and toString. For customization of field types, use
 * {@link FieldTypeCustomizer}
 *
 * @author TODAY <br>
 * 2019-09-03 13:03
 * @see KeyFactory#CLASS_BY_NAME
 */
@FunctionalInterface
public interface Customizer extends KeyFactoryCustomizer {

  void customize(CodeEmitter e, Type type);
}
