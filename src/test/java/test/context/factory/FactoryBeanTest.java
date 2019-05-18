/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2019 All Rights Reserved.
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
package test.context.factory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.taketoday.context.ApplicationContext;
import cn.taketoday.context.StandardApplicationContext;
import cn.taketoday.context.exception.NoSuchBeanDefinitionException;

/**
 * @author Today <br>
 * 
 *         2018-12-25 19:09
 */
public class FactoryBeanTest {

    private long start;

    @Before
    public void start() {
        start = System.currentTimeMillis();
    }

    @After
    public void end() {
        System.out.println("process takes " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Test
    public void test_PrototypeFactoryBean() throws NoSuchBeanDefinitionException {

        try (ApplicationContext applicationContext = new StandardApplicationContext("")) {
            applicationContext.loadContext("");

            Object bean = applicationContext.getBean("FactoryBean-Config");
            Object bean2 = applicationContext.getBean("FactoryBean-Config");

            System.err.println(bean);
            System.err.println(bean2);
            assert bean != bean2;
            applicationContext.close();

        }
    }

}
