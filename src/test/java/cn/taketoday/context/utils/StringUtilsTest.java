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
package cn.taketoday.context.utils;

import org.junit.Before;
import org.junit.Test;

import cn.taketoday.context.Constant;
import cn.taketoday.context.utils.StringUtils;

/**
 * @author Today <br>
 * 
 *         2018-12-10 19:06
 */
public class StringUtilsTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void test_IsEmpty() {
        assert !StringUtils.isEmpty("1234");
        assert !StringUtils.isEmpty(" ");
        assert StringUtils.isEmpty("");
        assert StringUtils.isEmpty(null);
    }

    @Test
    public void test_IsNotEmpty() {
        assert !StringUtils.isArrayNotEmpty((String[]) null);
        assert !StringUtils.isNotEmpty("");
        assert StringUtils.isNotEmpty(" ");
        assert StringUtils.isNotEmpty("1333r");
    }

    @Test
    public void test_Split() {

        String split[] = StringUtils.split("today;yhj,take");
        assert split.length == 3;
        assert split[0].equals("today");
        assert split[1].equals("yhj");
        assert split[2].equals("take");

        String split_[] = StringUtils.split("todayyhjtake");
        assert split_.length == 1;
        assert split_[0].equals("todayyhjtake");

        assert StringUtils.split(null) == null;

        assert !StringUtils.isArrayEmpty(split_);
        assert StringUtils.isArrayNotEmpty(split_);
        assert !StringUtils.isArrayNotEmpty();
        assert StringUtils.isArrayEmpty();
    }

    @Test
    public void testArrayToString() {

        String split[] = StringUtils.split("today;yhj,take");
        assert StringUtils.arrayToString(split).equals("today,yhj,take");
        assert StringUtils.arrayToString(null) == null;

        assert StringUtils.arrayToString(new String[] { "today" }).equals("today");
    }

    @Test
    public void testCheckPropertiesName() {
        assert StringUtils.checkPropertiesName("info").equals("info.properties");
        assert StringUtils.checkPropertiesName("info.properties").equals("info.properties");
        StringUtils.getUUIDString();
    }

    @Test
    public void testCleanPath() {

        assert StringUtils.cleanPath(null) == (null);
        assert StringUtils.cleanPath("").equals("");
        assert StringUtils.cleanPath("C:\\test\\").equals("C:/test/");
    }

    @Test
    public void testDecodeUrl() {
        assert "四川".equals(StringUtils.decodeUrl("%e5%9b%9b%e5%b7%9d"));
    }

    @Test
    public void testEncodeUrl() {

        assert StringUtils.encodeUrl("四川").equalsIgnoreCase("%e5%9b%9b%e5%b7%9d");
    }

    @Test
    public void testTokenizeToStringArray() {

        final String[] tokenizeToStringArray = StringUtils.tokenizeToStringArray("i,take,today", ",");
        assert tokenizeToStringArray.length == 3;

        final String[] tokenizeToStringArray2 = StringUtils.tokenizeToStringArray("i;take;today", ";");
        assert tokenizeToStringArray2.length == 3;
        assert tokenizeToStringArray.length == tokenizeToStringArray2.length;

        assert StringUtils.tokenizeToStringArray(null, null) == Constant.EMPTY_STRING_ARRAY;

    }
}