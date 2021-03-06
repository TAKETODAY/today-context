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
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */
package cn.taketoday.context.annotation.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

import cn.taketoday.context.ApplicationContext;
import cn.taketoday.context.Condition;
import cn.taketoday.context.ExpressionEvaluator;
import cn.taketoday.context.annotation.Conditional;

/**
 * annotation for a conditional element that depends on the value of a Java
 * Unified Expression Language
 *
 * @author TODAY <br>
 * 2019-06-18 15:11
 */
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnExpressionCondition.class)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface ConditionalOnExpression {

  /**
   * The Java Unified Expression Language expression to evaluate. Expression
   * should return {@code true} if the condition passes or {@code false} if it
   * fails.
   *
   * @return the El expression
   */
  String value() default "true";
}

class OnExpressionCondition implements Condition {

  @Override
  public boolean matches(final ApplicationContext context, final AnnotatedElement annotated) {
    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(context);
    final String expression = annotated.getAnnotation(ConditionalOnExpression.class).value();
    return expressionEvaluator.evaluate(expression, boolean.class);
  }
}
