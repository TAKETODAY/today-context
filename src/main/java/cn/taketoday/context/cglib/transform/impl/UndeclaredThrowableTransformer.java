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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import cn.taketoday.context.Constant;
import cn.taketoday.context.asm.Type;
import cn.taketoday.context.cglib.core.Block;
import cn.taketoday.context.cglib.core.CodeEmitter;
import cn.taketoday.context.cglib.core.EmitUtils;
import cn.taketoday.context.cglib.core.Signature;
import cn.taketoday.context.cglib.transform.ClassEmitterTransformer;
import cn.taketoday.context.utils.Assert;

/**
 * @author Today <br>
 * 2018-11-08 15:07
 */
@SuppressWarnings("all")
public class UndeclaredThrowableTransformer extends ClassEmitterTransformer {

  private final Type wrapper;

  public UndeclaredThrowableTransformer(Class wrapper) {
    this.wrapper = Type.getType(wrapper);
    boolean found = false;
    Constructor[] cstructs = wrapper.getConstructors();
    for (final Constructor cstruct : cstructs) {
      Class[] types = cstruct.getParameterTypes();
      if (types.length == 1 && types[0].equals(Throwable.class)) {
        found = true;
        break;
      }
    }
    Assert.isTrue(found, () -> wrapper + " does not have a single-arg constructor that takes a Throwable");
  }

  @Override
  public CodeEmitter beginMethod(int access, final Signature sig, final Type... exceptions) {

    final CodeEmitter e = super.beginMethod(access, sig, exceptions);
    if (Modifier.isAbstract(access) || sig.equals(Constant.SIG_STATIC)) {
      return e;
    }
    return new CodeEmitter(e) {

      private final Block handler = begin_block();

      @Override
      public void visitMaxs(int maxStack, int maxLocals) {
        handler.end();
        EmitUtils.wrapUndeclaredThrowable(this, handler, exceptions, wrapper);
        super.visitMaxs(maxStack, maxLocals);
      }
    };
  }
}
