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
package cn.taketoday.context.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import cn.taketoday.context.Constant;

/**
 * @author TODAY <br>
 *         2019-07-08 00:11
 * @since 2.1.6
 */
@FunctionalInterface
public interface Writable {

    /**
     * Return an {@link OutputStream} for the underlying resource, allowing to
     * (over-)write its content.
     * 
     * @throws IOException
     *             if the stream could not be opened
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Get {@link Writer}
     * 
     * @throws IOException
     *             if the stream could not be opened
     */
    default Writer getWriter() throws IOException {
        return new OutputStreamWriter(getOutputStream(), Constant.DEFAULT_CHARSET);
    }
}