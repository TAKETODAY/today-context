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
package cn.taketoday.context.cglib.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

import cn.taketoday.context.Constant;
import cn.taketoday.context.asm.ClassVisitor;
import cn.taketoday.context.asm.Type;
import cn.taketoday.context.cglib.core.AbstractClassGenerator;
import cn.taketoday.context.cglib.core.CglibReflectUtils;
import cn.taketoday.context.cglib.core.ClassEmitter;
import cn.taketoday.context.cglib.core.CodeEmitter;
import cn.taketoday.context.cglib.core.EmitUtils;
import cn.taketoday.context.cglib.core.MethodInfo;
import cn.taketoday.context.cglib.core.Signature;
import cn.taketoday.context.cglib.core.TypeUtils;

/**
 * @author Chris Nokleberg
 */
@SuppressWarnings("all")
public abstract class ImmutableBean {

  private static final Type ILLEGAL_STATE_EXCEPTION = TypeUtils.parseType("IllegalStateException");
  private static final Signature CSTRUCT_OBJECT = TypeUtils.parseConstructor("Object");
  private static final Class[] OBJECT_CLASSES = { Object.class };
  private static final String FIELD_NAME = "TODAY$RWBean";

  public static Object create(Object bean) {
    Generator gen = new Generator();
    gen.setBean(bean);
    return gen.create();
  }

  public static class Generator extends AbstractClassGenerator {
    private Object bean;
    private Class target;

    public Generator() {
      super(ImmutableBean.class);
    }

    public void setBean(Object bean) {
      this.bean = bean;
      target = bean.getClass();
    }

    protected ClassLoader getDefaultClassLoader() {
      return target.getClassLoader();
    }

    protected ProtectionDomain getProtectionDomain() {
      return CglibReflectUtils.getProtectionDomain(target);
    }

    public Object create() {
      String name = target.getName();
      setNamePrefix(name);
      return super.create(name);
    }

    public void generateClass(ClassVisitor v) {
      Type targetType = Type.getType(target);
      ClassEmitter ce = new ClassEmitter(v);
      ce.beginClass(Constant.JAVA_VERSION, Constant.ACC_PUBLIC, getClassName(), targetType, null, Constant.SOURCE_FILE);

      ce.declare_field(Constant.ACC_FINAL | Constant.ACC_PRIVATE, FIELD_NAME, targetType, null);

      CodeEmitter e = ce.beginMethod(Constant.ACC_PUBLIC, CSTRUCT_OBJECT);
      e.load_this();
      e.super_invoke_constructor();
      e.load_this();
      e.load_arg(0);
      e.checkcast(targetType);
      e.putfield(FIELD_NAME);
      e.return_value();
      e.end_method();

      PropertyDescriptor[] descriptors = CglibReflectUtils.getBeanProperties(target);
      Method[] getters = CglibReflectUtils.getPropertyMethods(descriptors, true, false);
      Method[] setters = CglibReflectUtils.getPropertyMethods(descriptors, false, true);

      for (int i = 0; i < getters.length; i++) {
        MethodInfo getter = CglibReflectUtils.getMethodInfo(getters[i]);
        e = EmitUtils.beginMethod(ce, getter, Constant.ACC_PUBLIC);
        e.load_this();
        e.getfield(FIELD_NAME);
        e.invoke(getter);
        e.return_value();
        e.end_method();
      }

      for (int i = 0; i < setters.length; i++) {
        MethodInfo setter = CglibReflectUtils.getMethodInfo(setters[i]);
        e = EmitUtils.beginMethod(ce, setter, Constant.ACC_PUBLIC);
        e.throw_exception(ILLEGAL_STATE_EXCEPTION, "Bean is immutable");
        e.end_method();
      }

      ce.endClass();
    }

    protected Object firstInstance(Class type) {
      return CglibReflectUtils.newInstance(type, OBJECT_CLASSES, new Object[] { bean });
    }

    // TODO: optimize
    protected Object nextInstance(Object instance) {
      return firstInstance(instance.getClass());
    }
  }
}
