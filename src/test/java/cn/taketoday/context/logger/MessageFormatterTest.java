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
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */
package cn.taketoday.context.logger;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author TODAY <br>
 *         2019-12-06 23:12
 */
public class MessageFormatterTest {

    @Test
    public void testMessageFormatter() throws Exception {

        final String messagePattern = "message: [{}]";
        final String format = MessageFormatter.format(messagePattern, "TEST VALUE");

        assertTrue(format.equals("message: [TEST VALUE]"));

        assertNull(MessageFormatter.format(null, null));

        assertTrue(MessageFormatter.format(messagePattern, null).equals(messagePattern)); // no params

        assertTrue(MessageFormatter.format("message: []", "").equals("message: []")); // empty {}
        assertTrue(MessageFormatter.format("message: [\\{}]", "").equals("message: [{}]")); //isEscapedDelimeter
        assertTrue(MessageFormatter.format("message: [\\\\{}]", "").equals("message: [\\]")); // double Escaped Delimeter

        // TODO deep append parameters 

        Object[] params = new Object[] { //
            "TEST", 123.124D, 123.123F, 123
        };

        final String ret = "string: [TEST], double: [123.124], float: [123.123], int: [123]";

        final String format2 = MessageFormatter.format("string: [{}], double: [{}], float: [{}], int: [{}]", params);
        System.err.println(format2);
        assertTrue(format2.equals(ret));

    }

}