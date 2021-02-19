/*
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
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.aop.proxy;

import cn.taketoday.aop.TargetSource;

/**
 * @author TODAY 2021/2/16 22:58
 */
public interface StandardProxyInvoker {

  static Object proceed(Object target, StandardMethodInvocation.Target targetInv, Object[] args) throws Throwable {
    return new StandardMethodInvocation(target, targetInv, args).proceed();
  }

  static Object staticExposeProceed(Object proxy, Object target,
                                    StandardMethodInvocation.Target targetInv, Object[] args) throws Throwable {

    Object oldProxy = null;
    try {
      oldProxy = AopContext.setCurrentProxy(proxy);
      return proceed(target, targetInv, args);
    }
    finally {
      AopContext.setCurrentProxy(oldProxy);
    }
  }

  static Object dynamicExposeProceed(Object proxy, TargetSource targetSource,
                                     StandardMethodInvocation.Target targetInv, Object[] args) throws Throwable {

    Object oldProxy = null;
    final Object target = targetSource.getTarget();
    try {
      oldProxy = AopContext.setCurrentProxy(proxy);
      return proceed(target, targetInv, args);
    }
    finally {
      AopContext.setCurrentProxy(oldProxy);
      if (target != null && !targetSource.isStatic()) {
        targetSource.releaseTarget(target);
      }
    }
  }

  static Object dynamicProceed(TargetSource targetSource,
                               StandardMethodInvocation.Target targetInv, Object[] args) throws Throwable {

    final Object target = targetSource.getTarget();
    try {
      return proceed(target, targetInv, args);
    }
    finally {
      if (target != null && !targetSource.isStatic()) {
        targetSource.releaseTarget(target);
      }
    }
  }

  static Object dynamicProceed(Object proxy, AdvisedSupport advised,
                               StandardMethodInvocation.Target targetInv, Object[] args) throws Throwable {

    Object target = null;
    Object oldProxy = null;
    boolean restore = false;

    final TargetSource targetSource = advised.getTargetSource();
    try {
      if (advised.isExposeProxy()) {
        // Make invocation available if necessary.
        oldProxy = AopContext.setCurrentProxy(proxy);
        restore = true;
      }
      // Get as late as possible to minimize the time we "own" the target, in case it comes from a pool...
      target = targetSource.getTarget();

      return proceed(target, targetInv, args);
    }
    finally {
      if (target != null && !targetSource.isStatic()) {
        targetSource.releaseTarget(target);
      }
      if (restore) {
        // Restore old proxy.
        AopContext.setCurrentProxy(oldProxy);
      }
    }
  }

}