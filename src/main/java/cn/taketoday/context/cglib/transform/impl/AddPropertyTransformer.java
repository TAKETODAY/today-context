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
package cn.taketoday.context.cglib.transform.impl;

import java.lang.reflect.Modifier;
import java.util.Map;

import cn.taketoday.context.asm.Type;
import cn.taketoday.context.cglib.core.EmitUtils;
import cn.taketoday.context.cglib.transform.ClassEmitterTransformer;

@SuppressWarnings("all")
public class AddPropertyTransformer extends ClassEmitterTransformer {
  private final String[] names;
  private final Type[] types;

  public AddPropertyTransformer(Map props) {
    int size = props.size();
    names = (String[]) props.keySet().toArray(new String[size]);
    types = new Type[size];
    for (int i = 0; i < size; i++) {
      types[i] = (Type) props.get(names[i]);
    }
  }

  public AddPropertyTransformer(String[] names, Type[] types) {
    this.names = names;
    this.types = types;
  }

  public void endClass() {
    if (!Modifier.isAbstract(getAccess())) {
      EmitUtils.addProperties(this, names, types);
    }
    super.endClass();
  }
}
