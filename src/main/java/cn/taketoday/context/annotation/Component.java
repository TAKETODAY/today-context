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
package cn.taketoday.context.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.taketoday.context.Scope;

/**
 * This annotation indicates that an annotated element is a bean component in
 * your application
 *
 * @author TODAY <br>
 * 2018-07-2 22:46:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Component {

  /**
   * The value may indicate a suggestion for a logical component name, to be
   * turned into a bean in case of an autodetected component.
   *
   * @return the suggested component name, if any (or empty String otherwise)
   */
  String[] value() default {};

  /**
   * Specifies the name of the scope to use for the annotated component/bean.
   * <p>
   * Defaults to an empty string ({@code ""}) which implies {@link Scope#SINGLETON
   * SINGLETON}.
   */
  String scope() default Scope.SINGLETON;

  /**
   * The optional name of a method to call on the bean instance during
   * initialization. Not commonly used, given that the method may be called
   * programmatically directly within the body of a Bean-annotated method.
   * <p>
   * The default value is {@code ""}, indicating no init method to be called.
   *
   * @see cn.taketoday.context.factory.InitializingBean
   * @see cn.taketoday.context.ConfigurableApplicationContext#refresh()
   */
  String[] initMethods() default {};

  /**
   * The optional names of a method to call on the bean instance upon closing the
   * application context, for example a {@code close()} method on a JDBC
   * {@code DataSource} implementation, or a Hibernate {@code SessionFactory}
   * object. The method must have no arguments but may throw any exception.
   * <p>
   * Note: Only invoked on beans whose lifecycle is under the full control of the
   * factory, which is always the case for singletons but not guaranteed for any
   * other scope.
   *
   * @see cn.taketoday.context.factory.DisposableBean
   * @see cn.taketoday.context.ConfigurableApplicationContext#close()
   */
  String[] destroyMethods() default {};
}
