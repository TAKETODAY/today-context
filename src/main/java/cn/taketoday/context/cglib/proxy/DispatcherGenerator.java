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

import java.lang.reflect.Modifier;
import java.util.List;

import cn.taketoday.context.asm.Type;
import cn.taketoday.context.cglib.core.ClassEmitter;
import cn.taketoday.context.cglib.core.CodeEmitter;
import cn.taketoday.context.cglib.core.MethodInfo;
import cn.taketoday.context.cglib.core.Signature;
import cn.taketoday.context.cglib.core.TypeUtils;

/**
 * @author TODAY <br>
 * 2018-11-08 15:09
 */
class DispatcherGenerator implements CallbackGenerator {

  public static final DispatcherGenerator INSTANCE = new DispatcherGenerator(false);
  public static final DispatcherGenerator PROXY_REF_INSTANCE = new DispatcherGenerator(true);

  private static final Type DISPATCHER = TypeUtils.parseType(Dispatcher.class);
  private static final Type PROXY_REF_DISPATCHER = TypeUtils.parseType(ProxyRefDispatcher.class);
  private static final Signature LOAD_OBJECT = TypeUtils.parseSignature("Object loadObject()");
  private static final Signature PROXY_REF_LOAD_OBJECT = TypeUtils.parseSignature("Object loadObject(Object)");

  private final boolean proxyRef;

  private DispatcherGenerator(boolean proxyRef) {
    this.proxyRef = proxyRef;
  }

  @Override
  public void generate(ClassEmitter ce, Context context, List<MethodInfo> methods) {

    for (final MethodInfo method : methods) {

      if (!Modifier.isProtected(method.getModifiers())) {

        final CodeEmitter e = context.beginMethod(ce, method);
        context.emitCallback(e, context.getIndex(method));
        if (proxyRef) {
          e.load_this();
          e.invoke_interface(PROXY_REF_DISPATCHER, PROXY_REF_LOAD_OBJECT);
        }
        else {
          e.invoke_interface(DISPATCHER, LOAD_OBJECT);
        }
        e.checkcast(method.getClassInfo().getType());
        e.load_args();
        e.invoke(method);
        e.return_value();
        e.end_method();
      }
    }
  }

  @Override
  public void generateStatic(CodeEmitter e, Context context, List<MethodInfo> methods) throws Exception {

  }

}
