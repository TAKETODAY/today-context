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
package cn.taketoday.context.bean;

import java.lang.reflect.Field;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Bean property
 * 
 * @author TODAY <br>
 *         2018-06-23 11:28:01
 */
@Getter
@AllArgsConstructor
public class PropertyValue {

    /** property value */
    private final Object value;
    /** field info */
    private final Field field;

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PropertyValue) {
            PropertyValue other = ((PropertyValue) obj);
            if (!other.value.equals(this.value)) {
                return false;
            }
            Field otherField = other.field;
            return otherField.equals(field);
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder()//
                .append("{\"value\":\"").append(value)//
                .append("\",\"field\":\"").append(field)//
                .append("\"}")//
                .toString();
    }
}
