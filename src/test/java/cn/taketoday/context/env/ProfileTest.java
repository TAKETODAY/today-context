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
package cn.taketoday.context.env;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cn.taketoday.context.ApplicationContext;
import cn.taketoday.context.StandardApplicationContext;
import test.demo.config.User;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Today <br>
 *
 *         2018-11-15 19:59
 */
public class ProfileTest {

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
    public void testProfile() {

        try (ApplicationContext applicationContext = new StandardApplicationContext("", "test.demo.config")) {

            User user = applicationContext.getBean("user", User.class);
            System.out.println(user);
            assert "TEST".equals(user.getUserName());
        }
    }

    @Test
    public void testConditional() {

        try (ApplicationContext context = new StandardApplicationContext("", "test.demo.config")) {
            User yhj = context.getBean("yhj", User.class);
            Assert.assertNull(yhj);

            String system = context.getEnvironment().getProperty("os.name");
            if (system != null && system.contains("Windows")) {
                User user = context.getBean("user_windows", User.class);
                assert "Windows".equals(user.getUserName());
            }
            assertThat(context.getEnvironment().getActiveProfiles()).hasSize(2);
        }
    }

}
