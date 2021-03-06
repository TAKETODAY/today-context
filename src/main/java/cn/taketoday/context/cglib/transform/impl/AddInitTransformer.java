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
package cn.taketoday.context.cglib.transform.impl;

import java.lang.reflect.Method;

import cn.taketoday.context.Constant;
import cn.taketoday.context.asm.Type;
import cn.taketoday.context.cglib.core.CglibReflectUtils;
import cn.taketoday.context.cglib.core.CodeEmitter;
import cn.taketoday.context.cglib.core.MethodInfo;
import cn.taketoday.context.cglib.core.Signature;
import cn.taketoday.context.cglib.transform.ClassEmitterTransformer;

/**
 * @author Mark Hobson
 */
public class AddInitTransformer extends ClassEmitterTransformer {

  private final MethodInfo info;

  public AddInitTransformer(Method method) {
    info = CglibReflectUtils.getMethodInfo(method);

    Type[] types = info.getSignature().getArgumentTypes();
    if (types.length != 1 || !types[0].equals(Constant.TYPE_OBJECT) || !info.getSignature().getReturnType().equals(Type.VOID_TYPE)) {
      throw new IllegalArgumentException(method + " illegal signature");
    }
  }

  @Override
  public CodeEmitter beginMethod(int access, Signature sig, Type... exceptions) {

    final CodeEmitter emitter = super.beginMethod(access, sig, exceptions);
    if (sig.getName().equals(Constant.CONSTRUCTOR_NAME)) {
      return new CodeEmitter(emitter) {
        @Override
        public void visitInsn(int opcode) {
          if (opcode == Constant.RETURN) {
            load_this();
            invoke(info);
          }
          super.visitInsn(opcode);
        }
      };
    }
    return emitter;
  }
}
