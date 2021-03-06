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

import cn.taketoday.context.asm.Type;

/**
 * @author TODAY <br>
 * 2019-09-03 19:33
 */
public abstract class ClassInfo {

  protected ClassInfo() {}

  public abstract Type getType();

  public abstract Type getSuperType();

  public abstract Type[] getInterfaces();

  public abstract int getModifiers();

  public boolean equals(Object o) {
    return (o == this) || ((o instanceof ClassInfo) && getType().equals(((ClassInfo) o).getType()));
  }

  public int hashCode() {
    return getType().hashCode();
  }

  public String toString() {
    // TODO: include modifiers, superType, interfaces
    return getType().getClassName();
  }
}
