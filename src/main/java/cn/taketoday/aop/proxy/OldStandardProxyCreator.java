/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2021 All Rights Reserved.
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.taketoday.aop.proxy;

import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.taketoday.context.Constant;
import cn.taketoday.context.asm.ClassVisitor;
import cn.taketoday.context.asm.Type;
import cn.taketoday.context.cglib.core.AbstractClassGenerator;
import cn.taketoday.context.cglib.core.CglibReflectUtils;
import cn.taketoday.context.cglib.core.ClassEmitter;
import cn.taketoday.context.cglib.core.CodeEmitter;
import cn.taketoday.context.cglib.core.CodeGenerationException;
import cn.taketoday.context.cglib.core.EmitUtils;
import cn.taketoday.context.cglib.core.KeyFactory;
import cn.taketoday.context.cglib.core.Local;
import cn.taketoday.context.cglib.core.MethodInfo;
import cn.taketoday.context.cglib.core.Signature;
import cn.taketoday.context.cglib.core.TypeUtils;
import cn.taketoday.context.factory.BeanFactory;
import cn.taketoday.context.logger.Logger;
import cn.taketoday.context.logger.LoggerFactory;
import cn.taketoday.context.utils.ClassUtils;
import cn.taketoday.context.utils.ContextUtils;
import cn.taketoday.context.utils.ObjectUtils;
import cn.taketoday.context.utils.OrderUtils;
import cn.taketoday.context.utils.StringUtils;

import static cn.taketoday.context.Constant.SOURCE_FILE;
import static cn.taketoday.context.asm.Opcodes.ACC_PUBLIC;
import static cn.taketoday.context.asm.Opcodes.JAVA_VERSION;

/**
 * @author TODAY <br>
 * 2019-09-07 10:44
 */
public class OldStandardProxyCreator {

  private static final Logger log = LoggerFactory.getLogger(OldStandardProxyCreator.class);

  public Object createProxy(OldTargetSource targetSource, BeanFactory beanFactory) {
    if (log.isDebugEnabled()) {
      log.debug("Creating Standard Proxy, target source is: [{}]", targetSource);
    }

    final OldStandardProxyGenerator proxyGenerator = new OldStandardProxyGenerator(beanFactory);
    proxyGenerator.setTargetSource(targetSource);

    return proxyGenerator.create();
  }

  // Aop standard proxy object generator
  // --------------------------------------------------------------

  private static final AopKey KEY_FACTORY = KeyFactory.create(AopKey.class, KeyFactory.CLASS_BY_NAME);

  interface AopKey {
    Object newInstance(Class<?> superClass);
  }

/**

public class AopTest$Bean$$AopByTODAY$$b059af0e extends Bean implements StandardProxy {
  private final Bean target;
  private static final Target testMR2B = InvocationRegistry.getTarget("testMR2B");
  private static final Target testReturnlQaU = InvocationRegistry.getTarget("testReturnlQaU");
  private static final Target test1rx8h = InvocationRegistry.getTarget("test1rx8h");

  public AopTest$Bean$$AopByTODAY$$b059af0e(Bean var1) {
    this.target = var1;
  }

  void test() {
    StandardMethodInvocation var1 = new StandardMethodInvocation(testMR2B, Constant.EMPTY_OBJECT_ARRAY);
    var1.proceed();
    var1 = null;
  }

  int testReturn() {
    StandardMethodInvocation var1 = new StandardMethodInvocation(testReturnlQaU, Constant.EMPTY_OBJECT_ARRAY);
    Object var2 = var1.proceed();
    var1 = null;
    return var2 == null ? 0 : ((Number)var2).intValue();
  }

  void test1() {
    StandardMethodInvocation var1 = new StandardMethodInvocation(test1rx8h, Constant.EMPTY_OBJECT_ARRAY);
    var1.proceed();
    var1 = null;
  }
}

*/

  public static class OldStandardProxyGenerator extends AbstractClassGenerator<Object> {

    private static final Signature proceed;
    private static final Signature getTarget;
    private static final Signature stdConstructorSignature;
    private static final Type stdProxy = Type.getType(StandardProxy.class);
    private static final Type stdType = Type.getType(StandardMethodInvocation.class);
    private static final Type invocationRegistryType = Type.getType(InvocationRegistry.class);
    private static final Type targetInvocationType = Type.getType(StandardMethodInvocation.Target.class);

    static {
      try {
        proceed = new Signature(StandardMethodInvocation.class.getMethod("proceed"));
        getTarget = new Signature(InvocationRegistry.class.getDeclaredMethod("getTarget", String.class));
        stdConstructorSignature = new Signature(StandardMethodInvocation.class.getDeclaredConstructor(
                StandardMethodInvocation.Target.class, Object[].class
        ));
      }
      catch (NoSuchMethodException e) {
        throw new CodeGenerationException(e);
      }
    }

    private Object target;
    private Class<?> targetClass;
    private OldTargetSource targetSource;
    private Class<?>[] parameterTypes;
    private Constructor<?> targetConstructor;
    private final BeanFactory beanFactory;

    public OldStandardProxyGenerator(BeanFactory beanFactory) {
      super("Aop");
      this.beanFactory = beanFactory;
    }

    public void setTarget(Object target) {
      this.target = target;
    }

    public void setTargetClass(Class<?> targetClass) {
      this.targetClass = ClassUtils.getUserClass(targetClass);
    }

    public OldTargetSource getTargetSource() {
      return targetSource;
    }

    public void setTargetSource(final OldTargetSource targetSource) {
      this.targetSource = targetSource;
      setTarget(targetSource.getTarget());
      setTargetClass(targetSource.getTargetClass());
    }

    @Override
    protected ClassLoader getDefaultClassLoader() {
      return targetClass.getClassLoader();
    }

    @Override
    protected ProtectionDomain getProtectionDomain() {
      return CglibReflectUtils.getProtectionDomain(targetClass);
    }

    public Object create() {
      setNamePrefix(targetClass.getName());
      Object key = KEY_FACTORY.newInstance(targetClass);
      return super.create(key);
    }

    public Class<?>[] getParameterTypes() {
      if (parameterTypes == null) {
        if (targetConstructor == null) {
          targetConstructor = ClassUtils.getSuitableConstructor(targetClass);
          if (targetConstructor == null) {
            throw new CodeGenerationException("No suitable constructor found in class :[" + targetClass + "]");
          }
        }
        this.parameterTypes = targetConstructor.getParameterTypes();
      }
      return parameterTypes;
    }

    @Override
    protected Object firstInstance(Class<Object> type) {

      final Class<?>[] parameterTypes = getParameterTypes();
      if (ObjectUtils.isEmpty(parameterTypes)) {
        return CglibReflectUtils.newInstance(type, new Class[] { targetClass }, new Object[] { target });
      }
      Class<?>[] types = this.parameterTypes;
      final Class<?>[] copy = new Class[types.length + 1];
      System.arraycopy(types, 0, copy, 0, types.length);
      copy[types.length] = targetClass;

      final Object[] arg = ContextUtils.resolveParameter(targetConstructor, beanFactory);
      Object[] args = new Object[parameterTypes.length + 1];
      System.arraycopy(arg, 0, args, 0, arg.length);
      args[types.length] = target;

      return CglibReflectUtils.newInstance(type, copy, args);
    }

    @Override
    protected Object nextInstance(Object instance) {
      return instance;
    }

    @Override
    public void generateClass(ClassVisitor v) {

      final ClassEmitter ce = new ClassEmitter(v);
      final Type targetType = TypeUtils.parseType(targetClass);

      final Type[] interfaces = TypeUtils.add(TypeUtils.getTypes(targetClass.getInterfaces()), stdProxy);
      ce.beginClass(JAVA_VERSION, ACC_PUBLIC, getClassName(), targetType, interfaces, SOURCE_FILE);

      ce.declare_field(Constant.ACC_PRIVATE | Constant.ACC_FINAL, "target", targetType, null);
      // 父类构造器参数
      constructor(ce, targetType);

      List<String> fields = new ArrayList<>();
      for (Method method : targetClass.getDeclaredMethods()) {

        final int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)
                || Modifier.isFinal(modifiers)
                || Modifier.isPrivate(modifiers)) {
          continue;
        }

        if (!targetSource.contains(method)) {
          invokeTarget(ce, targetType, method);
          continue;
        }

        final String targetField = putTarget(method, fields);
        fields.add(targetField);

        ce.declare_field(getStaticAccess(), targetField, targetInvocationType, null);

        MethodInfo methodInfo = CglibReflectUtils.getMethodInfo(method);
        final CodeEmitter codeEmitter = EmitUtils.beginMethod(ce, methodInfo, modifiers);

        final Local stdInvocationLocal = codeEmitter.make_local(stdType);

        codeEmitter.new_instance(stdType);
        codeEmitter.dup();

        codeEmitter.getfield(targetField);
        // 准备new StandardMethodInvocation()参数
        if (method.getParameterCount() == 0) {
          codeEmitter.getstatic(Type.getType(Constant.class), "EMPTY_OBJECT_ARRAY", Constant.TYPE_OBJECT_ARRAY);
        }
        else {
          codeEmitter.create_arg_array(); // args
        }

        codeEmitter.invoke_constructor(stdType, stdConstructorSignature);
        codeEmitter.store_local(stdInvocationLocal);

        // 调用之前先加载变量
        codeEmitter.load_local(stdInvocationLocal);
        codeEmitter.invoke_virtual(stdType, proceed);

        Local returnLocal = null;
        if (method.getReturnType() != void.class) {
          returnLocal = codeEmitter.make_local();
          codeEmitter.store_local(returnLocal);
        }
        // 清理
        codeEmitter.aconst_null();
        codeEmitter.store_local(stdInvocationLocal);

        if (returnLocal != null) {
          codeEmitter.load_local(returnLocal);
          codeEmitter.unbox_or_zero(Type.getType(method.getReturnType()));
        }

        codeEmitter.return_value();
        codeEmitter.end_method();
      }

      if (!fields.isEmpty()) {
        final CodeEmitter staticBlock = ce.begin_static(false); // 静态代码块
        for (final String target : fields) {
          staticBlock.visitLdcInsn(target);
          staticBlock.invoke_static(invocationRegistryType, getTarget);
          staticBlock.putfield(target);
        }
      }

      ce.endClass();
    }

    /**
     * @param method
     *         current method
     * @param fields
     *         Target keys in {@link #targetClass}
     *
     * @return Target key
     */
    protected String putTarget(final Method method, final List<String> fields) {
      final String field = method.getName() + StringUtils.getRandomString(4);
      if (fields.contains(field)) {
        return putTarget(method, fields);
      }
      final StandardMethodInvocation.Target target = InvocationRegistry.getTarget(field);
      if (target != null) {
        return putTarget(method, fields);
      }
      InvocationRegistry.putTarget(field, getTargetMethodInvocation(method));
      return field;
    }

    protected int getStaticAccess() {
      return Constant.PRIVATE_FINAL_STATIC;
    }

    protected StandardMethodInvocation.Target getTargetMethodInvocation(final Method method) {
      Map<Method, List<MethodInterceptor>> aspectMappings = targetSource.getAspectMappings();
      List<MethodInterceptor> interceptors = aspectMappings.get(method);
      OrderUtils.reversedSort(interceptors);
      MethodInterceptor[] advices = interceptors.toArray(new MethodInterceptor[interceptors.size()]);
      return new StandardMethodInvocation.Target(method, advices);
    }

    protected void constructor(final ClassEmitter ce, final Type targetType) {
      // 构造器
      final Type[] types = TypeUtils.getTypes(getParameterTypes());

      final Type[] add = TypeUtils.add(types, targetType, true); // 子类构造器参数
      final Signature parseConstructor = TypeUtils.parseConstructor(add);

      final CodeEmitter code = ce.beginMethod(ACC_PUBLIC, parseConstructor);

      code.load_this();
      code.dup();

      final int length = types.length;
      if (length > 0) {
        code.load_args(0, length);
      }
      code.super_invoke_constructor(TypeUtils.parseConstructor(types));

      code.load_arg(length);
      code.putfield("target");

      code.return_value();
      code.end_method();
    }

    protected void invokeTarget(final ClassEmitter ce, final Type targetType, final Method method) {
      MethodInfo methodInfo = CglibReflectUtils.getMethodInfo(method);
      final CodeEmitter codeEmitter = EmitUtils.beginMethod(ce, methodInfo, method.getModifiers());

      codeEmitter.load_this();

      codeEmitter.getfield(ce.getClassInfo().getType(), "target", targetType);

      codeEmitter.load_args();
      codeEmitter.invoke(methodInfo);
      codeEmitter.return_value();

      codeEmitter.unbox_or_zero(Type.getType(method.getReturnType()));
      codeEmitter.end_method();
    }

  }

}