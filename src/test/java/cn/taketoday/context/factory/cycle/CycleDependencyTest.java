/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2020 All Rights Reserved.
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
package cn.taketoday.context.factory.cycle;

import org.junit.Test;

import cn.taketoday.context.ApplicationContext;
import cn.taketoday.context.StandardApplicationContext;
import cn.taketoday.context.annotation.Autowired;
import cn.taketoday.context.annotation.Configuration;
import cn.taketoday.context.annotation.Singleton;
import cn.taketoday.context.loader.CandidateComponentScanner;

import static org.junit.Assert.assertEquals;

/**
 * @author TODAY <br>
 *         2019-12-12 09:50
 */
public class CycleDependencyTest {

    @Test
    public void testCycleDependency() {

        CandidateComponentScanner.getSharedInstance().clear();

        try (ApplicationContext applicationContext = new StandardApplicationContext()) {
            applicationContext.loadContext("cn.taketoday.context.factory.cycle");
            assertEquals(3, applicationContext.getBeanDefinitionCount());

            final BeanA beanA = applicationContext.getBean(BeanA.class);
            final BeanB beanB = applicationContext.getBean(BeanB.class);

            assertEquals(beanA, beanB.beanA);
            assertEquals(beanB, beanA.beanB);
            assertEquals(beanB, beanB.beanB);

//            final ConstructorCycleDependency1 one = applicationContext.getBean(ConstructorCycleDependency1.class);
//            final ConstructorCycleDependency2 two = applicationContext.getBean(ConstructorCycleDependency2.class);
//
//            assertEquals(two, one.two);
//            assertEquals(one, two.one);
        }
    }

    @Singleton
    public static class BeanA {

        @Autowired
        BeanB beanB;
    }

    @Singleton
    public static class BeanB {

        @Autowired
        BeanA beanA;

        @Autowired
        BeanB beanB;
    }

    @Configuration
    public static class ConfigBean {

    }

//    @Singleton
//    public static class ConstructorCycleDependency1 {
//        ConstructorCycleDependency2 two;
//
//        ConstructorCycleDependency1(ConstructorCycleDependency2 two) {
//            this.two = two;
//        }
//    }
//
//    @Singleton
//    public static class ConstructorCycleDependency2 {
//        ConstructorCycleDependency1 one;
//
//        ConstructorCycleDependency2(ConstructorCycleDependency1 one) {
//            this.one = one;
//        }
//    }

}
