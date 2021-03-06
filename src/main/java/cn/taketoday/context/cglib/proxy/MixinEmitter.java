/*
 * Copyright 2003,2004 The Apache Software Foundation
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
package cn.taketoday.context.cglib.proxy;

import java.lang.reflect.Method;
import java.util.HashSet;

import cn.taketoday.context.Constant;
import cn.taketoday.context.asm.ClassVisitor;
import cn.taketoday.context.asm.Type;
import cn.taketoday.context.cglib.core.CglibReflectUtils;
import cn.taketoday.context.cglib.core.ClassEmitter;
import cn.taketoday.context.cglib.core.CodeEmitter;
import cn.taketoday.context.cglib.core.EmitUtils;
import cn.taketoday.context.cglib.core.MethodInfo;
import cn.taketoday.context.cglib.core.MethodWrapper;
import cn.taketoday.context.cglib.core.Signature;
import cn.taketoday.context.cglib.core.TypeUtils;

import static cn.taketoday.context.Constant.SOURCE_FILE;
import static cn.taketoday.context.Constant.TYPE_OBJECT_ARRAY;
import static cn.taketoday.context.asm.Opcodes.ACC_PUBLIC;
import static cn.taketoday.context.asm.Opcodes.JAVA_VERSION;
import static cn.taketoday.context.asm.Type.array;

/**
 * @author Chris Nokleberg
 * @version $Id: MixinEmitter.java,v 1.9 2006/08/27 21:04:37 herbyderby Exp $
 */
class MixinEmitter extends ClassEmitter {

  private static final String FIELD_NAME = "TODAY$DELEGATES";
  private static final Type MIXIN = TypeUtils.parseType(Mixin.class);
  private static final Signature CSTRUCT_OBJECT_ARRAY = TypeUtils.parseConstructor("Object[]");

  private static final Signature NEW_INSTANCE = new Signature("newInstance", MIXIN, array(TYPE_OBJECT_ARRAY));

  public MixinEmitter(ClassVisitor v, String className, Class<?>[] classes, int[] route) {
    super(v);

    beginClass(JAVA_VERSION, ACC_PUBLIC, className, MIXIN, TypeUtils.getTypes(getInterfaces(classes)), SOURCE_FILE);
    EmitUtils.nullConstructor(this);
    EmitUtils.factoryMethod(this, NEW_INSTANCE);

    declare_field(Constant.ACC_PRIVATE, FIELD_NAME, TYPE_OBJECT_ARRAY, null);

    CodeEmitter e = beginMethod(ACC_PUBLIC, CSTRUCT_OBJECT_ARRAY);
    e.load_this();
    e.super_invoke_constructor();
    e.load_this();
    e.load_arg(0);
    e.putfield(FIELD_NAME);
    e.return_value();
    e.end_method();

    final HashSet<Object> unique = new HashSet<>();
    final int accVarargs = Constant.ACC_VARARGS;

    for (int i = 0; i < classes.length; i++) {
      Method[] methods = getMethods(classes[i]);
      for (final Method method : methods) {
        if (unique.add(MethodWrapper.create(method))) {
          MethodInfo methodInfo = CglibReflectUtils.getMethodInfo(method);
          int modifiers = ACC_PUBLIC;
          if ((methodInfo.getModifiers() & accVarargs) == accVarargs) {
            modifiers |= accVarargs;
          }
          e = EmitUtils.beginMethod(this, methodInfo, modifiers);
          e.load_this();
          e.getfield(FIELD_NAME);
          e.aaload((route != null) ? route[i] : i);
          e.checkcast(methodInfo.getClassInfo().getType());
          e.load_args();
          e.invoke(methodInfo);
          e.return_value();
          e.end_method();
        }
      }
    }

    endClass();
  }

  protected Class<?>[] getInterfaces(Class<?>[] classes) {
    return classes;
  }

  protected Method[] getMethods(Class<?> type) {
    return type.getMethods();
  }
}
